package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/security/AccessController.
 *
 * @author xDark
 */
@UtilityClass
public class AccessControllerNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val accController = (InstanceJavaClass) vm.findBootstrapClass("java/security/AccessController");
		vmi.setInvoker(accController, "getStackAccessControlContext", "()Ljava/security/AccessControlContext;", ctx -> {
			// TODO implement?
			ctx.setResult(NullValue.INSTANCE);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedExceptionAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedExceptionAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}