package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes jdk/internal/misc/ScopedMemoryAccess.
 *
 * @author xDark
 */
@UtilityClass
public class ScopedMemoryAccessNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceClass jc = (InstanceClass) vm.findBootstrapClass("jdk/internal/misc/ScopedMemoryAccess");
		if (jc != null) {
			VMInterface vmi = vm.getInterface();
			vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
			vmi.setInvoker(jc, "closeScope0", "(Ljdk/internal/misc/ScopedMemoryAccess$Scope;Ljdk/internal/misc/ScopedMemoryAccess$Scope$ScopedAccessError;)V", MethodInvoker.noop());
		}
	}
}
