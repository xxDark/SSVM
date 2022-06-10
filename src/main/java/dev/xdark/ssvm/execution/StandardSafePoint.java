package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.VMThread;

import java.util.concurrent.CountDownLatch;

/**
 * Safepoint implementation that uses atomic counter.
 *
 * @author xDark
 */
public class StandardSafePoint implements SafePoint {
	private final VirtualMachine vm;
	private volatile CountDownLatch latch;

	public StandardSafePoint(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public void poll() {
		CountDownLatch latch = this.latch;
		if (latch != null) {
			latch.countDown();
			Thread.currentThread().suspend();
		}
	}

	@Override
	public void request() {
		ThreadManager threadManager = vm.getThreadManager();
		// Attempt to block on thread manager.
		synchronized (threadManager) {
			VMThread[] threads = threadManager.getThreads();
			CountDownLatch latch = new CountDownLatch(threads.length);
			this.latch = latch;
			// And now we wait
			try {
				latch.await();
			} catch (InterruptedException ignored) {
			}
			this.latch = null;
		}
	}
}
