package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.nio.charset.StandardCharsets;

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
		val windowsDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		if (windowsDispatcher == null) {
			val unixDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/UnixNativeDispatcher") ;
			vmi.setInvoker(unixDispatcher, "getcwd", "()[B", ctx -> {
				val cwd = vm.getFileDescriptorManager().getCurrentWorkingDirectory().getBytes(StandardCharsets.UTF_8);
				ctx.setResult(vm.getHelper().toVMBytes(cwd));
				return Result.ABORT;
			});
			vmi.setInvoker(unixDispatcher, "init", "()V", MethodInvoker.noop());
			val linuxDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/LinuxNativeDispatcher");
			if (linuxDispatcher != null) {
				vmi.setInvoker(linuxDispatcher, "init", "()V", MethodInvoker.noop());
			} else {
				val bsdDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/BsdNativeDispatcher");
				vmi.setInvoker(bsdDispatcher, "initIDs", "()V", MethodInvoker.noop());
				val macDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nui/fs/MacOSXNativeDispatcher");
				if (macDispatcher != null) {
					vmi.setInvoker(macDispatcher, "normalizepath", "([CI)[C", ctx -> {
						val locals = ctx.getLocals();
						val helper = vm.getHelper();
						val path = helper.checkNotNullArray(locals.load(0));
						// int form = locals.load(1).asInt();
						ctx.setResult(path);
						return Result.ABORT;
					});
				}
			}
		} else {
			vmi.setInvoker(windowsDispatcher, "initIDs", "()V", MethodInvoker.noop());
		}
	}
}
