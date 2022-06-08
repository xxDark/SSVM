package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Initializes java/lang/ClassLoader.
 */
@UtilityClass
public class ClassLoaderNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass classLoader = symbols.java_lang_ClassLoader();
		vmi.setInvoker(classLoader, "registerNatives", "()V", MethodInvoker.noop());
		MethodInvoker initHook = ctx -> {
			vm.getClassLoaders().setClassLoaderData(ctx.getLocals().load(0));
			return Result.CONTINUE;
		};
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", initHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", initHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		Function<ExecutionContext, InstanceJavaClass> defineClassWithSource = makeClassDefiner(true);
		MethodInvoker defineClass1 = ctx -> {
			ctx.setResult(defineClassWithSource.apply(ctx).getOop());
			return Result.ABORT;
		};
		if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
			if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
				throw new IllegalStateException("Could not locate ClassLoader#defineClass1");
			}
		}
		Function<ExecutionContext, InstanceJavaClass> defineClass0Old = makeClassDefiner(false);
		if (!vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			ctx.setResult(defineClass0Old.apply(ctx).getOop());
			return Result.ABORT;
		})) {
			BiFunction<ExecutionContext, Boolean, InstanceJavaClass> defineClass0New = makeClassDefiner(1, false);
			vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/Class;Ljava/lang/String;[BIILjava/security/ProtectionDomain;ZILjava/lang/Object;)Ljava/lang/Class;", ctx -> {
				Locals locals = ctx.getLocals();
				int flags = locals.load(8).asInt();
				boolean hidden = (flags & 0x2) != 0;
				InstanceJavaClass jc = defineClass0New.apply(ctx, !hidden);
				ObjectValue classData = locals.<ObjectValue>load(9);
				if (hidden) {
					jc.getNode().access |= Modifier.ACC_VM_HIDDEN;
				}
				vm.getClassLoaders().setClassData(jc, classData);
				if (locals.load(7).asBoolean()) {
					jc.initialize();
				}
				ctx.setResult(jc.getOop());
				return Result.ABORT;
			});
		}
		vmi.setInvoker(classLoader, "findLoadedClass0", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			Value name = locals.load(1);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(name);
			InstanceValue loader = locals.<InstanceValue>load(0);
			ClassLoaderData data = vm.getClassLoaders().getClassLoaderData(loader);
			InstanceJavaClass loadedClass = data.getClass(helper.readUtf8(name).replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			int idx = (ctx.getMethod().getAccess() & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
			Value name = locals.load(idx);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(name);
			String s = helper.readUtf8(name);
			InstanceJavaClass loadedClass = (InstanceJavaClass) vm.findBootstrapClass(s.replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "resolveClass0", "(Ljava/lang/Class;)V", ctx -> {
			Value c = ctx.getLocals().load(1);
			VMHelper helper = vm.getHelper();
			helper.<JavaValue<JavaClass>>checkNotNull(c).getValue().initialize();
			return Result.ABORT;
		});
	}

	private static BiFunction<ExecutionContext, Boolean, InstanceJavaClass> makeClassDefiner(int argOffset, boolean withSource) {
		return (ctx, link) -> {
			VMHelper helper = ctx.getHelper();
			Locals locals = ctx.getLocals();
			ObjectValue loader = locals.<ObjectValue>load(0);
			ObjectValue name = locals.<ObjectValue>load(argOffset + 1);
			ArrayValue b = helper.checkNotNull(locals.load(argOffset + 2));
			int off = locals.load(argOffset + 3).asInt();
			int length = locals.load(argOffset + 4).asInt();
			ObjectValue pd = locals.<ObjectValue>load(argOffset + 5);
			ObjectValue source = withSource ? locals.<ObjectValue>load(argOffset + 6) : NullValue.INSTANCE;
			byte[] bytes = helper.toJavaBytes(b);
			return helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source), link);
		};
	}

	private static Function<ExecutionContext, InstanceJavaClass> makeClassDefiner(boolean withSource) {
		BiFunction<ExecutionContext, Boolean, InstanceJavaClass> definer = makeClassDefiner(0, withSource);
		return ctx -> definer.apply(ctx, Boolean.TRUE);
	}
}
