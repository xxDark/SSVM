package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Type;

/**
 * Initializes reflect/NativeConstructorAccessorImpl.
 *
 * @author xDark
 */
@UtilityClass
public class ConstructorAccessorNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeConstructorAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeConstructorAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeConstructorAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "newInstance0", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue c = locals.loadReference(0);
			int slot = vm.getPublicOperations().getInt(c, "slot");
			InstanceJavaClass declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) c.getValue("clazz", "Ljava/lang/Class;")).getValue();
			VMHelper helper = vm.getHelper();
			JavaMethod mn = helper.getMethodBySlot(declaringClass, slot);
			if (mn == null || !"<init>".equals(mn.getName())) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			Value values = locals.loadReference(1);
			Value[] converted;
			Type[] types = mn.getArgumentTypes();
			if (!values.isNull()) {
				ArrayValue passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
				converted = Util.convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			} else {
				helper.checkEquals(types.length, 0);
				converted = new Value[0];
			}
			InstanceValue instance = vm.getMemoryManager().newInstance(declaringClass);
			Locals args = vm.getThreadStorage().newLocals(mn);
			args.set(0, instance);
			for (int i = 0; i < converted.length; i++) {
				args.set(i + 1, converted[i]);
			}
			helper.invoke(mn, args);
			ctx.setResult(instance);
			return Result.ABORT;
		});
	}
}
