package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/ref/Reference.
 *
 * @author xDark
 */
@UtilityClass
public class ReferenceNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ref/Reference");
		vmi.setInvoker(jc, "clear0", "()V", ctx -> {
			ctx.getLocals().<InstanceValue>load(0).setValue("referent", "Ljava/lang/Object;", NullValue.INSTANCE);
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "refersTo0", "(Ljava/lang/Object;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue check = locals.<InstanceValue>load(0).getValue("referent", "Ljava/lang/Object;");
			ctx.setResult(check == locals.load(1) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
	}
}
