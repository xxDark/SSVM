package dev.xdark.ssvm.thread.java;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.thread.AllocatedThreadStorage;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadState;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.SimpleBacktrace;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Native thread manager that uses
 * Java thread to work.
 *
 * @author xDark
 */
public class NativeThreadManager implements ThreadManager {

	private final Map<Thread, VMThread> systemThreads = new HashMap<>();
	private final Map<InstanceValue, VMThread> vmThreads = new HashMap<>();
	private final VirtualMachine vm;
	private final ThreadGroup threadGroup;

	/**
	 * @param vm          VM instance.
	 * @param threadGroup Thread group to use.
	 */
	public NativeThreadManager(VirtualMachine vm, ThreadGroup threadGroup) {
		this.vm = vm;
		this.threadGroup = threadGroup;
	}

	@Override
	public synchronized void attachCurrentThread() {
		Thread thread = Thread.currentThread();
		if (thread instanceof NativeJavaThread) {
			throw new IllegalStateException("Cannot attach Java thread");
		}
		Map<Thread, VMThread> systemThreads = this.systemThreads;
		VMThread vmThread = systemThreads.get(thread);
		if (vmThread == null) {
			vmThread = new SystemVMThread(newBacktrace(), newThreadStorage(), vm, thread);
			systemThreads.put(thread, vmThread);
		}
	}

	@Override
	public synchronized void detachCurrentThread() {
		Thread thread = Thread.currentThread();
		if (thread instanceof NativeJavaThread) {
			throw new IllegalStateException("Cannot detach Java thread");
		}
		systemThreads.remove(thread);
	}

	@Override
	public VMThread getVmThread(Thread thread) {
		if (!(thread instanceof NativeJavaThread)) {
			Map<Thread, VMThread> systemThreads = this.systemThreads;
			VMThread vmThread = systemThreads.get(thread);
			if (vmThread != null) {
				return vmThread;
			}
			throw new IllegalStateException("Access from detached thread");
		}
		return ((NativeJavaThread) thread).getVmThread();
	}

	@Override
	public VMThread getVmThread(InstanceValue thread) {
		Map<InstanceValue, VMThread> threadMap = this.vmThreads;
		VMThread vmThread = threadMap.get(thread);
		if (vmThread == null) {
			synchronized (this) {
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
	public synchronized VMThread[] getThreads() {
		return Stream.concat(
			systemThreads.values().stream(),
			vmThreads.values().stream()
		).toArray(VMThread[]::new);
	}

	@Override
	public synchronized VMThread[] getVisibleThreads() {
		return vmThreads.values().toArray(new VMThread[0]);
	}

	@Override
	public VMThread createMainThread() {
		VirtualMachine vm = this.vm;
		Thread thread = Thread.currentThread();
		// If we were previously detached, attach this thread
		// using existing OOP if possible, there might be some
		// code that already cached thread instance.
		VMThread vmThread = systemThreads.get(thread);
		InstanceValue instance = vmThread.getOop();
		VMOperations ops = vm.getPublicOperations();
		ops.putInt(instance, "threadStatus", ThreadState.JVMTI_THREAD_STATE_ALIVE | ThreadState.JVMTI_THREAD_STATE_RUNNABLE);
		ops.putInt(instance, "priority", Thread.MAX_PRIORITY);
		return vmThread;
	}

	@Override
	public synchronized void suspendAll() {
		for (VMThread thread : getThreads()) {
			thread.suspend();
		}
	}

	@Override
	public synchronized void resumeAll() {
		for (VMThread thread : getThreads()) {
			thread.resume();
		}
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		currentThread().sleep(millis);
	}

	@Deprecated
	protected Backtrace newBacktrace() {
		return new SimpleBacktrace();
	}

	@Deprecated
	protected ThreadStorage newThreadStorage() {
		VirtualMachine vm = this.vm;
		MemoryManager manager = vm.getMemoryManager();
		MemoryAllocator allocator = vm.getMemoryAllocator();
		return new AllocatedThreadStorage(manager, allocator, allocator.allocateHeap(1024L * 1024L));
	}

	/**
	 * Creates new native thread.
	 *
	 * @param value Thread oop.
	 * @return created thread.
	 */
	protected NativeVMThread createThread(InstanceValue value) {
		return new NativeVMThread(newBacktrace(), newThreadStorage(), value, threadGroup, () -> {
			synchronized (this) {
				vmThreads.remove(value);
				vm.getSafePoint().pollAndSuspend();
			}
		});
	}
}
