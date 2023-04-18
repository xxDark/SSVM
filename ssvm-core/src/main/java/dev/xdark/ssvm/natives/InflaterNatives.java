package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/util/zip/Inflater.
 */
@UtilityClass
public class InflaterNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass jc = vm.getSymbols().java_util_zip_Inflater();
		vmi.setInvoker(jc, "initIDs", "()V", MethodInvoker.noop());
	}
}
