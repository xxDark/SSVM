package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassDefinitionOption;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass classLoader = symbols.java_lang_ClassLoader();
		vmi.setInvoker(classLoader, "registerNatives", "()V", MethodInvoker.noop());
		MethodInvoker initHook = MethodInvoker.interpreted(ctx -> {
			vm.getClassLoaders().createClassLoaderData(ctx.getLocals().loadReference(0));
			return Result.CONTINUE;
		});
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", initHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", initHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		Function<ExecutionContext<?>, InstanceClass> defineClassWithSource = makeClassDefiner(vm, true);
		MethodInvoker defineClass1 = ctx -> {
			ctx.setResult(defineClassWithSource.apply(ctx).getOop());
			return Result.ABORT;
		};
		if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
			if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
				throw new IllegalStateException("Could not locate ClassLoader#defineClass1");
			}
		}
		Function<ExecutionContext<?>, InstanceClass> defineClass0Old = makeClassDefiner(vm, false);
		if (!vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			ctx.setResult(defineClass0Old.apply(ctx).getOop());
			return Result.ABORT;
		})) {
			BiFunction<ExecutionContext<?>, Boolean, InstanceClass> defineClass0New = makeClassDefiner(vm, 1, false);
			vmi.setInvoker(classLoader, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/Class;Ljava/lang/String;[BIILjava/security/ProtectionDomain;ZILjava/lang/Object;)Ljava/lang/Class;", ctx -> {
				Locals locals = ctx.getLocals();
				int flags = locals.loadInt(8);
				boolean hidden = (flags & 0x2) != 0;
				InstanceClass jc = defineClass0New.apply(ctx, hidden);
				ObjectValue classData = locals.loadReference(9);
				if (hidden) {
					jc.getNode().access |= Modifier.ACC_VM_HIDDEN;
				}
				vm.getClassLoaders().setClassData(jc, classData);
				if (locals.loadInt(7) != 0) {
					vm.getOperations().initialize(jc);
				}
				ctx.setResult(jc.getOop());
				return Result.ABORT;
			});
		}
		vmi.setInvoker(classLoader, "findLoadedClass0", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue name = locals.loadReference(1);
			VMOperations ops = vm.getOperations();
			ops.checkNotNull(name);
			InstanceValue loader = locals.loadReference(0);
			ClassLoaderData data = vm.getClassLoaders().getClassLoaderData(loader);
			InstanceClass loadedClass = data.getClass(ops.readUtf8(name).replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? vm.getMemoryManager().nullValue() : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			int idx = (ctx.getMethod().getModifiers() & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
			ObjectValue name = locals.loadReference(idx);
			VMOperations ops = vm.getOperations();
			ops.checkNotNull(name);
			String s = ops.readUtf8(name);
			InstanceClass loadedClass = (InstanceClass) vm.findBootstrapClass(s.replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? vm.getMemoryManager().nullValue() : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "resolveClass0", "(Ljava/lang/Class;)V", ctx -> {
			ObjectValue c = ctx.getLocals().loadReference(1);
			VMOperations ops = vm.getOperations();
			JavaClass klass = vm.getClassStorage().lookup(ops.checkNotNull(c));
			if (klass instanceof InstanceClass) {
				ops.initialize((InstanceClass) klass);
			}
			return Result.ABORT;
		});
	}

	private static BiFunction<ExecutionContext<?>, Boolean, InstanceClass> makeClassDefiner(VirtualMachine vm, int argOffset, boolean withSource) {
		return (ctx, hidden) -> {
			VMOperations ops = vm.getOperations();
			Locals locals = ctx.getLocals();
			ObjectValue loader = locals.loadReference(0);
			ObjectValue name = locals.loadReference(argOffset + 1);
			ArrayValue b = ops.checkNotNull(locals.loadReference(argOffset + 2));
			int off = locals.loadInt(argOffset + 3);
			int length = locals.loadInt(argOffset + 4);
			ObjectValue pd = locals.loadReference(argOffset + 5);
			ObjectValue source = withSource ? locals.loadReference(argOffset + 6) : vm.getMemoryManager().nullValue();
			byte[] bytes = ops.toJavaBytes(b);
			return ops.defineClass(loader, ops.readUtf8(name), bytes, off, length, pd, ops.readUtf8(source), hidden ? ClassDefinitionOption.ANONYMOUS : 0);
		};
	}

	private static Function<ExecutionContext<?>, InstanceClass> makeClassDefiner(VirtualMachine vm, boolean withSource) {
		BiFunction<ExecutionContext<?>, Boolean, InstanceClass> definer = makeClassDefiner(vm, 0, withSource);
		return ctx -> definer.apply(ctx, Boolean.FALSE);
	}
}
