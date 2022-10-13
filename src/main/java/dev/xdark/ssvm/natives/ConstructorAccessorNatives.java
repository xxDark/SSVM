package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.Helper;
import dev.xdark.ssvm.util.Operations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

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
		InstanceClass accessor = (InstanceClass) vm.findBootstrapClass("jdk/internal/reflect/NativeConstructorAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceClass) vm.findBootstrapClass("sun/reflect/NativeConstructorAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeConstructorAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "newInstance0", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue c = locals.loadReference(0);
			Operations ops = vm.getOperations();
			int slot = ops.getInt(c, "slot");
			InstanceClass declaringClass = ((JavaValue<InstanceClass>) ops.getReference(c, "clazz", "Ljava/lang/Class;")).getValue();
			Helper helper = vm.getHelper();
			JavaMethod mn = helper.getMethodBySlot(declaringClass, slot);
			if (mn == null || !"<init>".equals(mn.getName())) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			ObjectValue values = locals.loadReference(1);
			JavaClass[] types = mn.getArgumentTypes();
			ArrayValue passedArgs = null;
			if (!values.isNull()) {
				passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
			} else {
				helper.checkEquals(types.length, 0);
			}
			InstanceValue instance = vm.getMemoryManager().newInstance(declaringClass);
			Locals args = vm.getThreadStorage().newLocals(mn);
			args.setReference(0, instance);
			if (passedArgs != null) {
				Util.copyReflectionArguments(vm, types, passedArgs, args, 0);
			}
			helper.invoke(mn, args);
			ctx.setResult(instance);
			return Result.ABORT;
		});
	}
}
