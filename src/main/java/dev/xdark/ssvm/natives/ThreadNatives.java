package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Thread.
 *
 * @author xDark
 */
@UtilityClass
public class ThreadNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass thread = symbols.java_lang_Thread();
		vmi.setInvoker(thread, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentThread().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "interrupt", "()V", ctx -> {
			VMThread th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.interrupt();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "setPriority0", "(I)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMThread th = vm.getThreadManager().getVmThread(locals.<InstanceValue>load(0));
			th.setPriority(locals.load(1).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "start0", "()V", ctx -> {
			VMThread th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.start();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isAlive", "()Z", ctx -> {
			VMThread th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			ctx.setResult(th.isAlive() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isInterrupted", "(Z)Z", ctx -> {
			Locals locals = ctx.getLocals();
			VMThread th = vm.getThreadManager().getVmThread(locals.<InstanceValue>load(0));
			ctx.setResult(th.isInterrupted(locals.load(1).asBoolean()) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "holdsLock", "(Ljava/lang/Object;)Z", ctx -> {
			ObjectValue arg = vm.getHelper().checkNotNull(ctx.getLocals().load(0));
			ctx.setResult(arg.isHeldByCurrentThread() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "getThreads", "()[Ljava/lang/Thread;", ctx -> {
			VMThread[] threads = vm.getThreadManager().getVisibleThreads();
			ArrayValue array = vm.getHelper().newArray(thread, threads.length);
			for (int i = 0; i < threads.length; i++) {
				array.setValue(i, threads[i].getOop());
			}
			ctx.setResult(array);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "sleep", "(J)V", ctx -> {
			long time = ctx.getLocals().load(0).asLong();
			if (time < 0L) {
				vm.getHelper().throwException(symbols.java_lang_IllegalArgumentException(), "timeout value is negative");
			}
			try {
				vm.getThreadManager().sleep(time);
			} catch (InterruptedException ex) {
				VMHelper helper = vm.getHelper();
				helper.throwException(symbols.java_lang_InterruptedException(), helper.newUtf8(ex.getMessage()));
			}
			return Result.ABORT;
		});
	}
}
