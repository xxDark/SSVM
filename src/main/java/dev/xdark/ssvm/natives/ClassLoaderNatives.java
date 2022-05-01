package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import lombok.val;
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val classLoader = symbols.java_lang_ClassLoader;
		vmi.setInvoker(classLoader, "registerNatives", "()V", MethodInvoker.noop());
		val initHook = (MethodInvoker) ctx -> {
			vm.getClassLoaders().setClassLoaderData(ctx.getLocals().load(0));
			return Result.CONTINUE;
		};
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", initHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", initHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		val defineClassWithSource = makeClassDefiner(true);
		val defineClass1 = (MethodInvoker) ctx -> {
			ctx.setResult(defineClassWithSource.apply(ctx).getOop());
			return Result.ABORT;
		};
		if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
			if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
				throw new IllegalStateException("Could not locate ClassLoader#defineClass1");
			}
		}
		val defineClass0Old = makeClassDefiner(false);
		if (!vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			ctx.setResult(defineClass0Old.apply(ctx).getOop());
			return Result.ABORT;
		})) {
			val defineClass0New = makeClassDefiner(1, false);
			vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/Class;Ljava/lang/String;[BIILjava/security/ProtectionDomain;ZILjava/lang/Object;)Ljava/lang/Class;", ctx -> {
				val locals = ctx.getLocals();
				int flags = locals.load(8).asInt();
				val hidden = (flags & 0x2) != 0;
				val jc = defineClass0New.apply(ctx, !hidden);
				val classData = locals.<ObjectValue>load(9);
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
			val locals = ctx.getLocals();
			val name = locals.load(1);
			val helper = vm.getHelper();
			helper.checkNotNull(name);
			val loader = locals.<InstanceValue>load(0);
			val data = vm.getClassLoaders().getClassLoaderData(loader);
			InstanceJavaClass loadedClass = data.getClass(helper.readUtf8(name).replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			int idx = (ctx.getMethod().getAccess() & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
			val name = locals.load(idx);
			val helper = vm.getHelper();
			helper.checkNotNull(name);
			val s = helper.readUtf8(name);
			InstanceJavaClass loadedClass = (InstanceJavaClass) vm.findBootstrapClass(s.replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBuiltinLib", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(ctx.getLocals().load(0));
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "resolveClass0", "(Ljava/lang/Class;)V", ctx -> {
			val c = ctx.getLocals().load(1);
			val helper = vm.getHelper();
			helper.<JavaValue<JavaClass>>checkNotNull(c).getValue().initialize();
			return Result.ABORT;
		});
	}

	private static BiFunction<ExecutionContext, Boolean, InstanceJavaClass> makeClassDefiner(int argOffset, boolean withSource) {
		return (ctx, link) -> {
			val helper = ctx.getHelper();
			val locals = ctx.getLocals();
			val loader = locals.<ObjectValue>load(0);
			val name = locals.<ObjectValue>load(argOffset + 1);
			val b = helper.checkArray(locals.load(argOffset + 2));
			val off = locals.load(argOffset + 3).asInt();
			val length = locals.load(argOffset + 4).asInt();
			val pd = locals.<ObjectValue>load(argOffset + 5);
			val source = withSource ? locals.<ObjectValue>load(argOffset + 6) : NullValue.INSTANCE;
			val bytes = helper.toJavaBytes(b);
			return helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source), link);
		};
	}

	private static Function<ExecutionContext, InstanceJavaClass> makeClassDefiner(boolean withSource) {
		val definer = makeClassDefiner(0, withSource);
		return ctx -> definer.apply(ctx, Boolean.TRUE);
	}
}
