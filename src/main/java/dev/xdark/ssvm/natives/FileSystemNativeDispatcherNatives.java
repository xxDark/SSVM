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
public class FileSystemNativeDispatcherNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		InstanceJavaClass nativeDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		if (nativeDispatcher == null) {
			nativeDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/LinuxNativeDispatcher");
			vmi.setInvoker(nativeDispatcher, "init", "()V", MethodInvoker.noop());
		} else {
			vmi.setInvoker(nativeDispatcher, "initIDs", "()V", MethodInvoker.noop());
		}
	}
}
