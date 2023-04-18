package dev.xdark.ssvm.thread.java;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.BaseVMThread;
import dev.xdark.ssvm.thread.ThreadState;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Represents detached thread.
 *
 * @author xDark
 */
public class SystemVMThread extends BaseVMThread {

	private final VirtualMachine vm;
	private final Thread thread;
	private InstanceValue oop;

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 * @param vm            VM instance.
	 * @param thread        Java thread.
	 */
	public SystemVMThread(Backtrace backtrace, ThreadStorage threadStorage, VirtualMachine vm, Thread thread) {
		super(backtrace, threadStorage, null);
		this.vm = vm;
		this.thread = thread;
	}

	@Override
	public Thread getJavaThread() {
		return thread;
	}

	@Override
	public InstanceValue getOop() {
		InstanceValue oop = this.oop;
		if (oop == null) {
			VirtualMachine vm = this.vm;
			VMOperations ops = vm.getPublicOperations();
			InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
			klass.initialize();
			MemoryManager memoryManager = vm.getMemoryManager();
			oop = memoryManager.newInstance(klass);
			ops.putInt(oop, "threadStatus", ThreadState.JVMTI_THREAD_STATE_ALIVE | ThreadState.JVMTI_THREAD_STATE_RUNNABLE);
			ops.putInt(oop, "priority", Thread.NORM_PRIORITY);
			// Though we set the group, detached threads
			// wont be visible in a thread list.
			InstanceValue mainThreadGroup = vm.getMainThreadGroup();
			if (mainThreadGroup != null) {
				// Might be null if VM is still in boot state,
				// will be set later.
				ops.putReference(oop, "group", "Ljava/lang/ThreadGroup;", mainThreadGroup);
			}
			oop.initialize();
			return this.oop = oop;
		}
		return oop;
	}

	@Override
	public void setPriority(int priority) {
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void start() {
	}

	@Override
	public void interrupt() {
	}

	@Override
	public boolean isAlive() {
		return thread.isAlive();
	}

	@Override
	public boolean isInterrupted(boolean clear) {
		return false;
	}

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
	}
}
