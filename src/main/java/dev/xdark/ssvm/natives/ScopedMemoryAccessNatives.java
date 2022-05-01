package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes jdk/internal/misc/ScopedMemoryAccess.
 *
 * @author xDark
 */
@UtilityClass
public class ScopedMemoryAccessNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val jc = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/ScopedMemoryAccess");
		if (jc != null) {
			val vmi = vm.getInterface();
			vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
			vmi.setInvoker(jc, "closeScope0", "(Ljdk/internal/misc/ScopedMemoryAccess$Scope;Ljdk/internal/misc/ScopedMemoryAccess$Scope$ScopedAccessError;)V", MethodInvoker.noop());
		}
	}
}
