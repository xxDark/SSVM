package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.val;

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
	public VMThread getVmThread(Thread thread) {
		val threadMap = this.threadMap;
		val current = threadMap.get(thread);
		if (current != null) {
			return current;
		}
		val vm = this.vm;
		val klass = vm.getSymbols().java_lang_Thread;
		klass.initialize();
		val instance = vm.getMemoryManager().newInstance(klass);
		val helper = vm.getHelper();
		val vmThread = new NopVMThread(instance, thread);
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
}
