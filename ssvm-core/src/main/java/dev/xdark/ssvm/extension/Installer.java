package dev.xdark.ssvm.extension;

import dev.xdark.ssvm.VirtualMachine;

/**
 * Extension installer.
 *
 * @author xDark
 */
public abstract class Installer {
	protected final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	protected Installer(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Installs the extension.
	 */
	public abstract void install();
}
