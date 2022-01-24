package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes reflect/NativeConstructorAccessorImpl.
 *
 * @author xDark
 */
@UtilityClass
public class ConstructorAccessorNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		InstanceJavaClass accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeConstructorAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeConstructorAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeConstructorAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "newInstance0", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val c = locals.<InstanceValue>load(0);
			val slot = c.getInt("slot");
			val declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) c.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val helper = vm.getHelper();
			val methods = declaringClass.getDeclaredConstructors(false);
			JavaMethod mn = null;
			for (val m : methods) {
				if (slot == m.getSlot()) {
					mn = m;
					break;
				}
			}
			if (mn == null || !"<init>".equals(mn.getName())) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			val values = locals.load(1);
			Value[] converted;
			val types = mn.getArgumentTypes();
			if (!values.isNull()) {
				val passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
				converted = Util.convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			} else {
				helper.checkEquals(types.length, 0);
				converted = new Value[0];
			}
			val instance = vm.getMemoryManager().newInstance(declaringClass);
			val args = new Value[converted.length + 1];
			System.arraycopy(converted, 0, args, 1, converted.length);
			args[0] = instance;
			helper.invokeExact(declaringClass, "<init>", mn.getDesc(), new Value[0], args);
			ctx.setResult(instance);
			return Result.ABORT;
		});
	}
}
