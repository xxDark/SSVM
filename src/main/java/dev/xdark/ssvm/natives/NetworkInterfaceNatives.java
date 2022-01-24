package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/net/NetworkInterface.
 *
 * @author xDark
 */
@UtilityClass
public class NetworkInterfaceNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_net_NetworkInterface;
		vmi.setInvoker(jc, "init", "()V", MethodInvoker.noop());
		// TODO introduce NetworkManager or something like that.
		vmi.setInvoker(jc, "getAll", "()[Ljava/net/NetworkInterface;", ctx -> {
			val helper = vm.getHelper();
			ctx.setResult(helper.emptyArray(jc));
			return Result.ABORT;
		});
	}
}
