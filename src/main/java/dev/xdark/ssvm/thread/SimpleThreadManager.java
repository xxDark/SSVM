package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of a thread manager.
 *
 * @author xDark
 */
public final class SimpleThreadManager implements ThreadManager {

	private final Object threadLock = new Object();
	private final Map<Thread, VMThread> threadMap = new WeakHashMap<>();
	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public SimpleThreadManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public VMThread getVmThread(Thread thread) {
		synchronized (threadLock) {
			var threadMap = this.threadMap;
			var existing = threadMap.get(thread);
			if (existing != null) {
				return existing;
			}
			var vm = this.vm;
			var jc = vm.getSymbols().java_lang_Thread;
			jc.initialize();
			var wrapper = vm.getMemoryManager().newJavaInstance(jc, thread);
			var vmThread = new SimpleVMThread(thread, wrapper);
			threadMap.put(thread, vmThread);
			vm.getHelper().screenVmThread(vmThread);
			return vmThread;
		}
	}

	@Override
	public void setVmThread(VMThread thread) {
		synchronized (threadLock) {
			threadMap.put(thread.getJavaThread(), thread);
		}
	}
}
