package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/util/zip/Inflater.
 */
@UtilityClass
public class InflaterNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_util_zip_Inflater;
		vmi.setInvoker(jc, "initIDs", "()V", MethodInvoker.noop());
	}
}
