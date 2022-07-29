package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of a thread manager
 * that does not start Java threads.
 *
 * @author xDark
 */
public final class NopThreadManager implements ThreadManager {

	private final Thread nopThread = new NopThread();
	private final Map<Thread, VMThread> threadMap = new WeakHashMap<>();
	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public NopThreadManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public synchronized void attachCurrentThread() {
		Thread thread = Thread.currentThread();
		Map<Thread, VMThread> threadMap = this.threadMap;
		NopVMThread current = (NopVMThread) threadMap.get(thread);
		if (current != null) {
			return;
		}
		VirtualMachine vm = this.vm;
		current = new NopVMThread(thread);
		threadMap.put(thread, current);
		InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
		klass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(klass);
		current.setOop(instance);
		InstanceValue mainThreadGroup = vm.getMainThreadGroup();
		if (mainThreadGroup != null) {
			// Might be null if VM is still in boot state,
			// will be set later.
			instance.setValue("group", "Ljava/lang/ThreadGroup;", mainThreadGroup);
		}
		vm.getHelper().screenVmThread(current);
		instance.initialize();
	}

	@Override
	public synchronized void detachCurrentThread() {
		threadMap.remove(Thread.currentThread());
	}

	@Override
	public VMThread getVmThread(Thread thread) {
		Map<Thread, VMThread> threadMap = this.threadMap;
		VMThread current = threadMap.get(thread);
		if (current != null) {
			return current;
		}
		throw new IllegalStateException("Access from detached thread");
	}

	@Override
	public VMThread getVmThread(InstanceValue thread) {
		return new NopVMThread(thread, nopThread);
	}

	@Override
	public synchronized VMThread[] getThreads() {
		return threadMap.values().toArray(new VMThread[0]);
	}

	@Override
	public VMThread[] getVisibleThreads() {
		return getThreads();
	}

	@Override
	public VMThread createMainThread() {
		return getVmThread(Thread.currentThread());
	}

	@Override
	public synchronized void suspendAll() {
		threadMap.values().forEach(VMThread::suspend);
	}

	@Override
	public synchronized void resumeAll() {
		threadMap.values().forEach(VMThread::resume);
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		currentThread().sleep(millis);
	}
}
