package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass jc = (InstanceClass) vm.findBootstrapClass("java/lang/ref/Reference");
		vmi.setInvoker(jc, "clear0", "()V", ctx -> {
			vm.getOperations().putReference((ObjectValue) ctx.getLocals().loadReference(0), "referent", "Ljava/lang/Object;", vm.getMemoryManager().nullValue());
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "refersTo0", "(Ljava/lang/Object;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue check = vm.getOperations().getReference((ObjectValue) ctx.getLocals().loadReference(0), "referent", "Ljava/lang/Object;");
			ctx.setResult(check == locals.loadReference(1) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getAndClearReferencePendingList", "()Ljava/lang/ref/Reference;", ctx -> {
			// There is only one usage of this method I as of JDK 17, and it does a null check.
			// So we can cut corners and skip actually implementing this.
			ctx.setResult(ctx.getMemoryManager().nullValue());
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hasReferencePendingList", "()Z", ctx -> {
			// See above
			ctx.setResult(0);
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "waitForReferencePendingList", "()V", MethodInvoker.noop());
	}
}
