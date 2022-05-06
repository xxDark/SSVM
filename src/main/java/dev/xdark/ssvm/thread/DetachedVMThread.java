package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Represents detached thread.
 *
 * @author xDark
 */
public class DetachedVMThread extends BaseVMThread {

	private final VirtualMachine vm;
	private final Thread thread;
	private InstanceValue oop;

	/**
	 * @param vm
	 * 		VM instance.
	 * @param thread
	 * 		Java thread.
	 */
	public DetachedVMThread(VirtualMachine vm, Thread thread) {
		super(null);
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
			InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
			klass.initialize();
			oop = vm.getMemoryManager().newInstance(klass);
			vm.getHelper().initializeDefaultValues(oop);
			oop.setInt("threadStatus", ThreadState.JVMTI_THREAD_STATE_ALIVE | ThreadState.JVMTI_THREAD_STATE_RUNNABLE);
			oop.setInt("priority", Thread.NORM_PRIORITY);
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
