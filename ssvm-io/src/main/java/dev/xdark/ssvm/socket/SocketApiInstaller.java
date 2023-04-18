package dev.xdark.ssvm.socket;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.extension.Installer;
import dev.xdark.ssvm.socket.natives.DualStackPlainSocketImplNatives;
import dev.xdark.ssvm.socket.natives.Inet4AddressNatives;
import dev.xdark.ssvm.socket.natives.InetAddressImplFactoryNatives;
import dev.xdark.ssvm.socket.natives.InetAddressNatives;

/**
 * Socket API installer.
 *
 * @author xDark
 */
public final class SocketApiInstaller extends Installer {
	private SocketManager socketManager;

	public SocketApiInstaller(VirtualMachine vm) {
		super(vm);
	}

	/**
	 * @param socketManager Socket manager.
	 * @return This installer.
	 */
	public SocketApiInstaller socketManager(SocketManager socketManager) {
		this.socketManager = socketManager;
		return this;
	}

	@Override
	public void install() {
		VirtualMachine vm = this.vm;
		SocketManager socketManager = this.socketManager;
		DualStackPlainSocketImplNatives.init(vm, socketManager);
		InetAddressNatives.init(vm);
		InetAddressImplFactoryNatives.init(vm, socketManager);
		Inet4AddressNatives.init(vm);
	}

	/**
	 * @param vm VM instance.
	 * @return New installer.
	 */
	public static SocketApiInstaller create(VirtualMachine vm) {
		return new SocketApiInstaller(vm);
	}
}
