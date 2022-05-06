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
	 * @param vm
	 * 		VM instance.
	 */
	public NopThreadManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public VMThread getVmThread(Thread thread) {
		Map<Thread, VMThread> threadMap = this.threadMap;
		VMThread current = threadMap.get(thread);
		if (current != null) {
			return current;
		}
		synchronized(this) {
			current = threadMap.get(thread);
			if (current == null) {
				VirtualMachine vm = this.vm;
				InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
				klass.initialize();
				InstanceValue instance = vm.getMemoryManager().newInstance(klass);
				current = new NopVMThread(instance, thread);
				threadMap.put(thread, current);
				vm.getHelper().screenVmThread(current);
				instance.initialize();
			}
		}
		return current;
	}

	@Override
	public VMThread getVmThread(InstanceValue thread) {
		return new NopVMThread(thread, nopThread);
	}

	@Override
	public synchronized void setVmThread(VMThread thread) {
		threadMap.put(thread.getJavaThread(), thread);
	}

	@Override
	public synchronized VMThread[] getThreads() {
		return threadMap.values().toArray(new VMThread[0]);
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
