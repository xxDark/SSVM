package dev.xdark.ssvm.socket.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

/**
 * @author xDark
 */
@UtilityClass
public class Inet4AddressNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass jc = (InstanceClass) vm.findBootstrapClass("java/net/Inet4Address");
		vmi.setInvoker(jc, "init", "()V", MethodInvoker.noop());
	}
}
