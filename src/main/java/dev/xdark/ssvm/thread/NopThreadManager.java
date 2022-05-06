package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
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
		VirtualMachine vm = this.vm;
		InstanceJavaClass klass = vm.getSymbols().java_lang_Thread;
		klass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(klass);
		VMHelper helper = vm.getHelper();
		NopVMThread vmThread = new NopVMThread(instance, thread);
		threadMap.put(thread, vmThread);
		helper.screenVmThread(vmThread);
		return vmThread;
	}

	@Override
	public VMThread getVmThread(InstanceValue thread) {
		return new NopVMThread(thread, nopThread);
	}

	@Override
	public void setVmThread(VMThread thread) {
		threadMap.put(thread.getJavaThread(), thread);
	}

	@Override
	public VMThread[] getThreads() {
		return threadMap.values().toArray(new VMThread[0]);
	}

	@Override
	public void suspendAll() {
		threadMap.values().forEach(VMThread::suspend);
	}

	@Override
	public void resumeAll() {
		threadMap.values().forEach(VMThread::resume);
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		currentThread().sleep(millis);
	}
}
