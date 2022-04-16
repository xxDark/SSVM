package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import lombok.val;

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
		val defineClass1 = (MethodInvoker) ctx -> {
			val helper = ctx.getHelper();
			val locals = ctx.getLocals();
			val loader = locals.<ObjectValue>load(0);
			val name = locals.<ObjectValue>load(1);
			val b = helper.checkArray(locals.load(2));
			val off = locals.load(3).asInt();
			val length = locals.load(4).asInt();
			val pd = locals.<ObjectValue>load(5);
			val source = locals.<ObjectValue>load(6);
			val bytes = helper.toJavaBytes(b);
			val defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source));
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		};
		if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
			if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
				throw new IllegalStateException("Could not locate ClassLoader#defineClass1");
			}
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
			val name = locals.load(1);
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
}
