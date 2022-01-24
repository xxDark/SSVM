package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Thread.
 *
 * @author xDark
 */
@UtilityClass
public class ThreadNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val thread = symbols.java_lang_Thread;
		vmi.setInvoker(thread, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentThread().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "interrupt", "()V", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.interrupt();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "setPriority0", "(I)V", ctx -> {
			val locals = ctx.getLocals();
			val th = vm.getThreadManager().getVmThread(locals.<InstanceValue>load(0));
			th.setPriority(locals.load(1).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "start0", "()V", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.start();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isAlive", "()Z", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			ctx.setResult(th.isAlive() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
	}
}
