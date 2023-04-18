package dev.xdark.ssvm.process;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.extension.Installer;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.process.natives.ProcessImplNatives;

/**
 * Process API installer.
 *
 * @author xDark
 */
public final class ProcessApiInstaller extends Installer {
	private FileManager fileManager;
	private ProcessManager processManager;

	private ProcessApiInstaller(VirtualMachine vm) {
		super(vm);
	}

	/**
	 * @param fileManager File manager.
	 * @return This builder.
	 */
	public ProcessApiInstaller fileManager(FileManager fileManager) {
		this.fileManager = fileManager;
		return this;
	}

	/**
	 * @param processManager Process manager.
	 * @return This builder.
	 */
	public ProcessApiInstaller processHandleManager(ProcessManager processManager) {
		this.processManager = processManager;
		return this;
	}

	@Override
	public void install() {
		ProcessImplNatives.init(vm, fileManager, processManager);
	}

	/**
	 * @param vm VM instance.
	 * @return New installer.
	 */
	public static ProcessApiInstaller create(VirtualMachine vm) {
		return new ProcessApiInstaller(vm);
	}
}
