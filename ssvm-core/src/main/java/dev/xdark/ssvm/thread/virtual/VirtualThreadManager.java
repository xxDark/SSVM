package dev.xdark.ssvm.thread.virtual;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.filesystem.Handle;
import dev.xdark.ssvm.jvmti.ThreadState;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.thread.OSThread;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.SimpleBacktrace;
import dev.xdark.ssvm.thread.heap.HeapThreadStorage;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Virtual thread manager and scheduler.
 *
 * @author xDark
 */
public final class VirtualThreadManager implements ThreadManager {
	private static final VirtualJavaThread SENTINEL = new VirtualJavaThread(null, null);
	// Mapping between eetop and Java thread
	private final Map<Handle, VirtualJavaThread> javaThreads = new HashMap<>();
	// Attached threads
	private final Map<Thread, VirtualJavaThread> foreignThreads = new IdentityHashMap<>();
	// All threads so far
	private final List<VirtualJavaThread> allThreads = new LinkedList<>();
	// Threads for scheduling
	private final Queue<VirtualJavaThread> scheduled = new PriorityQueue<>(Comparator.comparingInt(t -> t.getOsThread().getPriority()));
	// TODO this is stupid
	private final List<VirtualJavaThread> asleep = new ArrayList<>();
	private final Object threadLock = new Object[0];
	private final VirtualMachine vm;
	private VirtualJavaThread currentThread;

	public VirtualThreadManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public void startThread(InstanceValue oop) {
		startThread0(oop, false);
	}

	@Override
	public void suspendThread(InstanceValue oop) {
		vm.getOperations().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError());
	}

	@Override
	public void resumeThread(InstanceValue oop) {
		vm.getOperations().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError());
	}

	@Override
	public void setPriority(InstanceValue oop, int priority) {
		VirtualJavaThread th = forThread(oop);
		if (th != null) {
			Queue<VirtualJavaThread> allThreads = this.scheduled;
			OSThread ost = th.getOsThread();
			if (ost.getPriority() != priority) {
				boolean offer = allThreads.remove(th);
				// Re-insert thread for the scheduler
				ost.setPriority(priority);
				if (offer) {
					// setPriority may've been called
					// from another thread on a thread that is asleep
					allThreads.offer(th);
				}
			}
		}
	}

	@Override
	public void interrupt(InstanceValue oop) {
		VirtualJavaThread th = forThread(oop);
		if (th != null) {
			if (dead(th)) {
				return;
			}
			Thread foreign = th.foreign;
			if (foreign != null) {
				foreign.interrupt();
			} else {
				if (th.timeout != 0L) {
					th.timeout = 0L;
					// Wake up thread from sleep
					th.exception = vm.getOperations().newException(vm.getSymbols().java_lang_InterruptedException(), "sleep interrupted");
					asleep.remove(th);
					scheduled.offer(th);
				} else {
					th.interrupted = true;
				}
			}
		}
	}

	@Override
	public void setName(InstanceValue oop, String name) {
		VirtualJavaThread th = forThread(oop);
		if (th != null) {
			th.getOsThread().setName(name);
		}
	}

	@Override
	public void stop(InstanceValue oop, InstanceValue exception) {
		VirtualJavaThread th = forThread(oop);
		if (th != null) {
			th.exception = exception;
		}
	}

	@Override
	public JavaThread currentJavaThread() {
		return currentThread();
	}

	@Override
	public OSThread currentOsThread() {
		return currentThread().getOsThread();
	}

	@Override
	public void attachCurrentThread() {
		Thread th = Thread.currentThread();
		Map<Thread, VirtualJavaThread> foreignThreads = this.foreignThreads;
		synchronized (threadLock) {
			// TODO need to do counting here
			if (foreignThreads.putIfAbsent(th, SENTINEL) == null) {
				VirtualOSThread osThread = newOsThread(0L);
				InstanceValue oop = vm.getMemoryManager().newInstance(vm.getSymbols().java_lang_Thread());
				VirtualJavaThread javaThread = new VirtualJavaThread(oop, osThread);
				foreignThreads.put(th, javaThread);
				setThreadEeetop(javaThread);
				javaThread.foreign = th;
				String name = th.getName();
				VMOperations ops = vm.getOperations();
				ops.putReference(oop, "name", "Ljava/lang/String;", ops.newUtf8(name));
				int priority = th.getPriority();
				ops.putInt(oop, "priority", priority);
				syncThread(osThread, oop);
			}
		}
	}

	@Override
	public void detachCurrentThread() {
		Thread th = Thread.currentThread();
		VirtualJavaThread jth;
		synchronized (threadLock) {
			jth = foreignThreads.remove(th);
		}
		if (jth != null) {
			jth.osThread.free();
		}
	}

	@Override
	public boolean isInterrupted(InstanceValue oop, boolean clear) {
		VirtualJavaThread th = forThread(oop);
		if (dead(th)) {
			return false;
		}
		boolean flag = th.interrupted;
		if (clear) {
			th.interrupted = false;
		}
		return flag;
	}

	@Override
	public List<JavaThread> snapshot() {
		return new ArrayList<>(allThreads);
	}

	@Override
	public void sleep(long millis) {
		VirtualJavaThread th = currentThread();
		if (millis == 0L) {
			return;
		}
		Assertions.check(!dead(th), "thread is not alive");
		OSThread osThread = th.getOsThread();
		if (th.attached) {
			osThread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED);
			// Call to native Thread.sleep
			th.timeout = millis;
			try {
				Thread.sleep(millis);
			} catch (InterruptedException ex) {
				// Propagate to VM code
				th.timeout = 0L;
				th.interrupted = true;
				vm.getOperations().throwException(vm.getSymbols().java_lang_InterruptedException(), "sleep interrupted");
			} finally {
				osThread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE);
			}
		} else {
			Assertions.check(th.timeout == 0L, "already sleeping");
			if (th.interrupted) {
				th.interrupted = false;
				vm.getOperations().throwException(vm.getSymbols().java_lang_InterruptedException(), "sleep interrupted");
			} else {
				osThread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED);
				th.timeout = millis;
				// Remove form scheduled threads and put to asleep
				scheduled.remove(th);
				asleep.add(th);
			}
		}
	}

	@Override
	public void yield() {
		// Do nothing
	}

	@Override
	public JavaThread createMainThread() {
		InstanceValue oop = vm.getMemoryManager().newInstance(vm.getSymbols().java_lang_Thread());
		Thread th = Thread.currentThread();
		VirtualJavaThread javaThread = foreignThreads.get(th); // TODO fixme
		if (javaThread == null) {
			VirtualOSThread osThread = newOsThread(0L);
			javaThread = new VirtualJavaThread(oop, osThread);
		} else {
			// TODO this is invalid, VM must be started from valid thread created by ThreadManager
			/*
			javaThread.foreign = null;
			javaThread.attached = false;
			*/
		}
		currentThread = javaThread;
		VMOperations ops = vm.getOperations();
		ops.putReference(oop, "name", "Ljava/lang/String;", ops.newUtf8("main"));
		int priority = th.getPriority();
		ops.putInt(oop, "priority", priority);
		OSThread osThread = javaThread.getOsThread();
		syncThread(osThread, oop);
		osThread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE);
		setThreadEeetop(javaThread);
		schedule(javaThread);
		return javaThread;
	}

	@Override
	public JavaThread getThread(InstanceValue oop) {
		// Fast check
		VirtualJavaThread currentThread = this.currentThread;
		if (currentThread != null && oop == currentThread.getOop()) {
			return currentThread;
		}
		return forThread(oop);
	}

	private VirtualJavaThread currentThread() {
		VirtualJavaThread th;
		synchronized (threadLock) {
			th = foreignThreads.get(Thread.currentThread());
		}
		if (th == null) {
			th = currentThread;
			Assertions.notNull(th, "not a Java thread");
			if (th.attached) {
				Assertions.check(th.foreign == Thread.currentThread(), "currentThread cache is poisoned");
			}
			return th;
		}
		return th;
	}

	private VirtualOSThread newOsThread(long stackSize) {
		if (stackSize == 0L) {
			// Default stack size, 1MB should be enough.
			// TODO configurable, like Java's -Xss flag.
			stackSize = 1024L * 1024L;
		}
		Backtrace backtrace = new SimpleBacktrace(1024);
		MemoryAllocator memoryAllocator = vm.getMemoryAllocator();
		ThreadStorage storage = new HeapThreadStorage(vm.getMemoryManager(), memoryAllocator, memoryAllocator.allocateHeap(stackSize));
		return new VirtualOSThread(backtrace, storage);
	}

	private VirtualJavaThread startThread0(InstanceValue oop, boolean attached) {
		VMOperations ops = vm.getOperations();
		long stackSize = ops.getLong(oop, "stackSize");
		VirtualOSThread thread = newOsThread(stackSize);
		// Do sync between OS thread and Java thread
		syncThread(thread, oop);
		thread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE);
		VirtualJavaThread javaThread = new VirtualJavaThread(oop, thread);
		javaThread.attached = attached;
		setThreadEeetop(javaThread);
		if (!attached) {
			// Insert into a list of all threads for the scheduler
			schedule(javaThread);
		}
		return javaThread;
	}

	private void schedule(VirtualJavaThread thread) {
		allThreads.add(thread);
		scheduled.offer(thread);
	}

	private void setThreadEeetop(VirtualJavaThread th) {
		Handle handle = Handle.of(0L);
		// Insert into javaThreads map with free eetop
		Map<Handle, VirtualJavaThread> javaThreads = this.javaThreads;
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		do {
			handle.set(rng.nextLong());
		} while (javaThreads.putIfAbsent(handle, th) != null);
		vm.getOperations().putLong(th.getOop(), "eetop", handle.get());
	}

	private void syncThread(OSThread thread, InstanceValue oop) {
		VMOperations ops = vm.getOperations();
		thread.setName(ops.readUtf8(ops.getReference(oop, "name", "Ljava/lang/String;")));
		thread.setPriority(ops.getInt(oop, "priority"));
		thread.setState(ThreadState.JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE);
	}

	private VirtualJavaThread forThread(InstanceValue oop) {
		VMOperations ops = vm.getOperations();
		long eetop = ops.getLong(oop, "eetop");
		Handle handle = Handle.threadLocal(eetop);
		return javaThreads.get(handle);
	}

	private static boolean dead(JavaThread th) {
		return th == null || th.getOsThread().getState() == ThreadState.JVMTI_THREAD_STATE_TERMINATED;
	}
}
