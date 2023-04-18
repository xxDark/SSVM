package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
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
	 * @param vm          VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		VMInterface vmi = vm.getInterface();
		InstanceClass windowsDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		if (windowsDispatcher == null) {
			InstanceClass unixDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/UnixNativeDispatcher");
			vmi.setInvoker(unixDispatcher, "getcwd", "()[B", ctx -> {
				byte[] cwd = fileManager.getCurrentWorkingDirectory().getBytes(StandardCharsets.UTF_8);
				ctx.setResult(vm.getOperations().toVMBytes(cwd));
				return Result.ABORT;
			});
			vmi.setInvoker(unixDispatcher, "init", "()V", MethodInvoker.noop());
			vmi.setInvoker(unixDispatcher, "init", "()I", ctx -> {
				ctx.setResult(0);
				return Result.ABORT;
			});
			InstanceClass linuxDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/LinuxNativeDispatcher");
			if (linuxDispatcher != null) {
				vmi.setInvoker(linuxDispatcher, "init", "()V", MethodInvoker.noop());
			} else {
				InstanceClass bsdDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/BsdNativeDispatcher");
				vmi.setInvoker(bsdDispatcher, "initIDs", "()V", MethodInvoker.noop());
				InstanceClass macDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nui/fs/MacOSXNativeDispatcher");
				if (macDispatcher != null) {
					vmi.setInvoker(macDispatcher, "normalizepath", "([CI)[C", ctx -> {
						Locals locals = ctx.getLocals();
						VMOperations ops = vm.getOperations();
						ArrayValue path = ops.checkNotNull(locals.loadReference(0));
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
