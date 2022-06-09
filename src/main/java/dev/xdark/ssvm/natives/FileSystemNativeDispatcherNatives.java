package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

/**
 * Initializes sun/nio/fs/WindowsNativeDispatcher.
 *
 * @author xDark
 */
@UtilityClass
public class FileSystemNativeDispatcherNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass windowsDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		if (windowsDispatcher == null) {
			InstanceJavaClass unixDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/UnixNativeDispatcher");
			vmi.setInvoker(unixDispatcher, "getcwd", "()[B", ctx -> {
				byte[] cwd = vm.getFileDescriptorManager().getCurrentWorkingDirectory().getBytes(StandardCharsets.UTF_8);
				ctx.setResult(vm.getHelper().toVMBytes(cwd));
				return Result.ABORT;
			});
			vmi.setInvoker(unixDispatcher, "init", "()V", MethodInvoker.noop());
			vmi.setInvoker(unixDispatcher, "init", "()I", ctx -> {
				ctx.setResult(IntValue.ZERO);
				return Result.ABORT;
			});
			InstanceJavaClass linuxDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/LinuxNativeDispatcher");
			if (linuxDispatcher != null) {
				vmi.setInvoker(linuxDispatcher, "init", "()V", MethodInvoker.noop());
			} else {
				InstanceJavaClass bsdDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nio/fs/BsdNativeDispatcher");
				vmi.setInvoker(bsdDispatcher, "initIDs", "()V", MethodInvoker.noop());
				InstanceJavaClass macDispatcher = (InstanceJavaClass) vm.findBootstrapClass("sun/nui/fs/MacOSXNativeDispatcher");
				if (macDispatcher != null) {
					vmi.setInvoker(macDispatcher, "normalizepath", "([CI)[C", ctx -> {
						Locals locals = ctx.getLocals();
						VMHelper helper = vm.getHelper();
						ArrayValue path = helper.checkNotNull(locals.<ObjectValue>load(0));
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
