package dev.xdark.ssvm.filesystem;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.extension.Installer;
import dev.xdark.ssvm.filesystem.natives.FileSystemNativeDispatcherNatives;
import dev.xdark.ssvm.filesystem.natives.GenericFileSystemNatives;
import dev.xdark.ssvm.filesystem.natives.JarFileNatives;
import dev.xdark.ssvm.filesystem.natives.OldFileSystemNatives;
import dev.xdark.ssvm.filesystem.natives.ZipFileNatives;

/**
 * File system API installer.
 *
 * @author xDark
 */
public final class FileApiInstaller extends Installer {
	private FileManager fileManager;

	private FileApiInstaller(VirtualMachine vm) {
		super(vm);
	}

	/**
	 * @param fileManager File manager.
	 * @return This builder.
	 */
	public FileApiInstaller fileManager(FileManager fileManager) {
		this.fileManager = fileManager;
		return this;
	}

	@Override
	public void install() {
		VirtualMachine vm = this.vm;
		FileManager fileManager = this.fileManager;
		GenericFileSystemNatives.init(vm, fileManager);
		OldFileSystemNatives.init(vm, fileManager);
		FileSystemNativeDispatcherNatives.init(vm, fileManager);
		ZipFileNatives.init(vm, fileManager);
		JarFileNatives.init(vm, fileManager);
	}

	/**
	 * @param vm VM instance.
	 * @return New installer.
	 */
	public static FileApiInstaller create(VirtualMachine vm) {
		return new FileApiInstaller(vm);
	}
}
