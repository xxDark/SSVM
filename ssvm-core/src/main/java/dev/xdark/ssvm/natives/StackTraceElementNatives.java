package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/StackTraceElement.
 *
 * @author xDark
 */
@UtilityClass
public class StackTraceElementNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass stackTraceElement = (InstanceClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		vmi.setInvoker(stackTraceElement, "initStackTraceElements", "([Ljava/lang/StackTraceElement;Ljava/lang/Throwable;)V", ctx -> {
			VMOperations ops = vm.getOperations();
			Locals locals = ctx.getLocals();
			ArrayValue arr = ops.checkNotNull(locals.loadReference(0));
			InstanceValue ex = ops.checkNotNull(locals.loadReference(1));
			ArrayValue initialized = ops.checkNotNull(ops.getReference(ex, "backtrace", "Ljava/lang/Object;"));

			int x = 0;
			for (int i = initialized.getLength(); i != 0; ) {
				arr.setReference(x++, initialized.getReference(--i));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(stackTraceElement, "isHashedInJavaBase", "(Ljava/lang/Module;)Z", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});
	}
}
