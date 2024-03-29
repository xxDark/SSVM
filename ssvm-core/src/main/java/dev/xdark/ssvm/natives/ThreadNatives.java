package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jvmti.ThreadState;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.experimental.UtilityClass;

import java.util.List;

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
		Symbols symbols = vm.getSymbols();
		InstanceClass thread = symbols.java_lang_Thread();
		vmi.setInvoker(thread, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentJavaThread().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "interrupt0", "()V", ctx -> {
			vm.getThreadManager().interrupt(ctx.getLocals().loadReference(0));
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "setPriority0", "(I)V", ctx -> {
			Locals locals = ctx.getLocals();
			vm.getThreadManager().setPriority(locals.loadReference(0), locals.loadInt(1));
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "start0", "()V", ctx -> {
			vm.getThreadManager().startThread(ctx.getLocals().loadReference(0));
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isAlive", "()Z", ctx -> {
			JavaThread th = vm.getThreadManager().getThread(ctx.getLocals().loadReference(0));
			ctx.setResult(th != null && th.getOsThread().getThreadState().has(ThreadState.JVMTI_THREAD_STATE_ALIVE) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isInterrupted", "(Z)Z", ctx -> {
			Locals locals = ctx.getLocals();
			boolean interrupted = vm.getThreadManager().isInterrupted(locals.loadReference(0), locals.loadInt(1) != 0);
			ctx.setResult(interrupted ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "holdsLock", "(Ljava/lang/Object;)Z", ctx -> {
			Mutex mutex = vm.getMemoryManager().getMutex(vm.getOperations().checkNotNull(ctx.getLocals().loadReference(0)));
			ctx.setResult(mutex.isHeldByCurrentThread() ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "getThreads", "()[Ljava/lang/Thread;", ctx -> {
			List<JavaThread> threads = vm.getThreadManager().snapshot();
			int threadCount = threads.size();
			ArrayValue array = vm.getOperations().allocateArray(thread, threadCount);
			for (int i = 0; i < threadCount; i++) {
				array.setReference(i, threads.get(i).getOop());
			}
			ctx.setResult(array);
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "sleep", "(J)V", ctx -> {
			long time = ctx.getLocals().loadLong(0);
			if (time < 0L) {
				vm.getOperations().throwException(symbols.java_lang_IllegalArgumentException(), "timeout value is negative");
				return Result.ABORT;
			}
			vm.getThreadManager().sleep(time);
			return Result.ABORT;
		});
	}
}
