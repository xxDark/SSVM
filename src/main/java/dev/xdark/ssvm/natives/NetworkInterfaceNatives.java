package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/net/NetworkInterface.
 *
 * @author xDark
 */
@UtilityClass
public class NetworkInterfaceNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_net_NetworkInterface();
		vmi.setInvoker(jc, "init", "()V", MethodInvoker.noop());
		// TODO introduce NetworkManager or something like that.
		vmi.setInvoker(jc, "getAll", "()[Ljava/net/NetworkInterface;", ctx -> {
			VMHelper helper = vm.getHelper();
			ctx.setResult(helper.emptyArray(jc));
			return Result.ABORT;
		});
	}
}
