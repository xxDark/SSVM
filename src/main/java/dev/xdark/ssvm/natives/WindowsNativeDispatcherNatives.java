package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes sun/nio/fs/WindowsNativeDispatcher.
 * 
 * @author xDark
 */
@UtilityClass
public class WindowsNativeDispatcherNatives {


	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		vmi.setInvoker(jc, "initIDs", "()V", MethodInvoker.noop());
	}
}
