package dev.xdark.ssvm.socket.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.socket.SocketManager;
import lombok.experimental.UtilityClass;

/**
 * @author xDark
 */
@UtilityClass
public class InetAddressImplFactoryNatives {

	/**
	 * @param vm            VM instance.
	 * @param socketManager Socket manager.
	 */
	public void init(VirtualMachine vm, SocketManager socketManager) {
		VMInterface vmi = vm.getInterface();
		InstanceClass jc = (InstanceClass) vm.findBootstrapClass("java/net/InetAddressImplFactory");
		vmi.setInvoker(jc, "isIPv6Supported", "()Z", ctx -> {
			ctx.setResult(socketManager.isIPv6Supported() ? 1 : 0);
			return Result.ABORT;
		});
	}
}
