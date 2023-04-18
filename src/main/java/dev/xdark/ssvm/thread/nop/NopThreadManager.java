package dev.xdark.ssvm.thread.nop;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.heap.HeapThreadStorage;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.thread.backtrace.SimpleBacktrace;
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
	private final Map<InstanceValue, VMThread> instanceMap = new WeakHashMap<>();
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
		current = new NopVMThread(new SimpleBacktrace(), newThreadStorage(), thread);
		threadMap.put(thread, current);
		InstanceJavaClass klass = vm.getSymbols().java_lang_Thread();
		klass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(klass);
		current.setOop(instance);
		InstanceValue mainThreadGroup = vm.getMainThreadGroup();
		if (mainThreadGroup != null) {
			// Might be null if VM is still in boot state,
			// will be set later.
			vm.getPublicOperations().putReference(instance, "group", "Ljava/lang/ThreadGroup;", mainThreadGroup);
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
	public synchronized VMThread getVmThread(InstanceValue thread) {
		return instanceMap.computeIfAbsent(thread, k -> {
			return new NopVMThread(new SimpleBacktrace(), newThreadStorage(), k, nopThread);
		});
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

	@Deprecated
	private ThreadStorage newThreadStorage() {
		VirtualMachine vm = this.vm;
		MemoryManager manager = vm.getMemoryManager();
		MemoryAllocator allocator = vm.getMemoryAllocator();
		return new HeapThreadStorage(manager, allocator, allocator.allocateHeap(1024L * 1024L));
	}
}
