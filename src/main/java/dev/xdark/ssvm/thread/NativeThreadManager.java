package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Native thread manager that uses
 * Java thread to work.
 *
 * @author xDark
 */
public class NativeThreadManager implements ThreadManager {

	private final Map<Thread, VMThread> systemThreads = new WeakHashMap<>();
	private final Map<InstanceValue, NativeVMThread> vmThreads = new WeakHashMap<>();
	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public NativeThreadManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public VMThread getVmThread(Thread thread) {
		if (!(thread instanceof NativeJavaThread)) {
			Map<Thread, VMThread> systemThreads = this.systemThreads;
			VMThread vmThread = systemThreads.get(thread);
			if (vmThread == null) {
				synchronized (this) {
					vmThread = systemThreads.get(thread);
					if (vmThread == null) {
						vmThread = new DetachedVMThread(vm, thread);
						systemThreads.put(thread, vmThread);
					}
				}
			}
			return vmThread;
		}
		return ((NativeJavaThread) thread).getVmThread();
	}

	@Override
	public VMThread getVmThread(InstanceValue thread) {
		Map<InstanceValue, NativeVMThread> threadMap = this.vmThreads;
		NativeVMThread vmThread = threadMap.get(thread);
		if (vmThread == null) {
			synchronized(this) {
				vmThread = threadMap.get(thread);
				if (vmThread == null) {
					vmThread = new NativeVMThread(thread);
					threadMap.put(thread, vmThread);
				}
			}
		}
		return vmThread;
	}

	@Override
	public void setVmThread(VMThread thread) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized VMThread[] getThreads() {
		return vmThreads.values().toArray(new NativeVMThread[0]);
	}

	@Override
	public synchronized void suspendAll() {
		vmThreads.values().forEach(NativeVMThread::suspend);
	}

	@Override
	public synchronized void resumeAll() {
		vmThreads.values().forEach(NativeVMThread::resume);
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		currentThread().sleep(millis);
	}
}
