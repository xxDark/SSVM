package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
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
				synchronized(this) {
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
					vmThread = createThread(thread);
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
	public VMThread[] getVisibleThreads() {
		return getThreads();
	}

	@Override
	public VMThread createMainThread() {
		VirtualMachine vm = this.vm;
		Thread thread = Thread.currentThread();
		// If we were previously detached, attach this thread
		// using existing OOP if possible, there might be some
		// code that already cached thread instance.
		VMThread detached = systemThreads.remove(thread);
		boolean callInitialize = true;
		InstanceValue instance;
		fromDetached: {
			if (detached != null) {
				DetachedVMThread dtvm = (DetachedVMThread) detached;
				if (dtvm.isOopSet()) {
					instance = dtvm.getOop();
					callInitialize = false;
					break fromDetached;
				}
			}
			InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
			klass.initialize();
			instance = vm.getMemoryManager().newInstance(klass);
			vm.getHelper().initializeDefaultValues(instance);
		}
		NativeVMThread vmThread = createMainThread(instance, thread);
		vmThreads.put(instance, vmThread);
		instance.setInt("threadStatus", ThreadState.JVMTI_THREAD_STATE_ALIVE | ThreadState.JVMTI_THREAD_STATE_RUNNABLE);
		instance.setInt("priority", Thread.MAX_PRIORITY);
		if (callInitialize) {
			instance.initialize();
		}
		return vmThread;
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

	/**
	 * Creates new native thread.
	 *
	 * @param value
	 * 		Thread oop.
	 *
	 * @return created thread.
	 */
	protected NativeVMThread createThread(InstanceValue value) {
		return new NativeVMThread(value);
	}

	/**
	 * Creates new main thread.
	 *
	 * @param value
	 * 		Thread oop.
	 * @param thread
	 * 		Java thread.
	 *
	 * @return main thread.
	 */
	protected NativeVMThread createMainThread(InstanceValue value, Thread thread) {
		return new NativeVMThread(value, thread);
	}
}
