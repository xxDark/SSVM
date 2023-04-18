package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/security/AccessController.
 *
 * @author xDark
 */
@UtilityClass
public class AccessControllerNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass accController = (InstanceJavaClass) vm.findBootstrapClass("java/security/AccessController");
		vmi.setInvoker(accController, "getStackAccessControlContext", "()Ljava/security/AccessControlContext;", ctx -> {
			// TODO implement?
			ctx.setResult(vm.getMemoryManager().nullValue());
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;", ctx -> {
			ObjectValue action = ctx.getLocals().loadReference(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(action);
			JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(action, "run", "()Ljava/lang/Object;");
			Locals locals = vm.getThreadStorage().newLocals(method);
			locals.setReference(0, action);
			helper.invoke(method, locals, ctx.getResult());
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;", ctx -> {
			ObjectValue action = ctx.getLocals().loadReference(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(action);
			JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(action, "run", "()Ljava/lang/Object;");
			Locals locals = vm.getThreadStorage().newLocals(method);
			locals.setReference(0, action);
			helper.invoke(method, locals, ctx.getResult());
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			ObjectValue action = ctx.getLocals().loadReference(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(action);
			JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(action, "run", "()Ljava/lang/Object;");
			Locals locals = vm.getThreadStorage().newLocals(method);
			locals.setReference(0, action);
			helper.invoke(method, locals, ctx.getResult());
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			ObjectValue action = ctx.getLocals().loadReference(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(action);
			JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(action, "run", "()Ljava/lang/Object;");
			Locals locals = vm.getThreadStorage().newLocals(method);
			locals.setReference(0, action);
			helper.invoke(method, locals, ctx.getResult());
			return Result.ABORT;
		});
	}
}
