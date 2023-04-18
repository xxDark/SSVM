package dev.xdark.ssvm.socket.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.socket.SocketManager;
import lombok.experimental.UtilityClass;

/**
 * @author xDark
 */
@UtilityClass
public class DualStackPlainSocketImplNatives {

	/**
	 * @param vm            VM instance.
	 * @param socketManager Socket manager.
	 */
	public void init(VirtualMachine vm, SocketManager socketManager) {
		VMInterface vmi = vm.getInterface();
		InstanceClass jc = (InstanceClass) vm.findBootstrapClass("java/net/DualStackPlainSocketImpl");
		vmi.setInvoker(jc, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(jc, "socket0", "(ZZ)I", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(socketManager.createSocket(locals.loadInt(0) != 0, locals.loadInt(1) != 0));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "bind0", "(ILjava/net/InetAddress;IZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			socketManager.bind(locals.loadInt(0), locals.loadReference(1), locals.loadInt(2), locals.loadInt(3) != 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "listen0", "(II)V", ctx -> {
			Locals locals = ctx.getLocals();
			socketManager.listen(locals.loadInt(0), locals.loadInt(1));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "close0", "(I)V", ctx -> {
			Locals locals = ctx.getLocals();
			socketManager.close(locals.loadInt(0));
			return Result.ABORT;
		});
	}
}
