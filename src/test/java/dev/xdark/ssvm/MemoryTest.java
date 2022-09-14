package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.NavigableMemoryAllocator;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.heap.HeapThreadStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemoryTest {
	private static ThreadStorage storage;

	@BeforeAll
	public static void setup() {
		MemoryAllocator alloc = new NavigableMemoryAllocator();
		storage = new HeapThreadStorage(null, alloc, alloc.allocateHeap(2048L));
	}

	@Test
	public void testLocalsAndStack() {
		Stack stack = storage.newStack(1);
		Locals locals = storage.newLocals(1);
		stack.pushInt(50005);
		stack.sinkInto(locals, 1);
		assertEquals(50005, locals.loadInt(0));
		assertTrue(stack.isEmpty());
		stack.pushInt(10005);
		stack.sinkInto(locals, 0, 1);
		assertEquals(10005, locals.loadInt(0));
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testStackWide() {
		Stack stack = storage.newStack(2);
		stack.pushLong(Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, stack.peekLong());
		assertFalse(stack.isEmpty());
		assertEquals(Long.MAX_VALUE, stack.popLong());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testLocalsCopy() {
		Locals a = storage.newLocals(1);
		Locals b = storage.newLocals(1);
		a.setInt(0, 6000);
		assertEquals(6000, a.loadInt(0));
		b.copyFrom(a, 0, 0, 1);
		assertEquals(6000, b.loadInt(0));
		assertEquals(1, b.maxSlots());
	}

	/*
	@Disabled
	@Test
	public void testGC() {
		VirtualMachine vm = new VirtualMachine() {
			@Override
			protected MemoryManager createMemoryManager() {
				return new SimpleMemoryManager(this) {
					@Override
					protected GarbageCollector createGarbageCollector() {
						return new MarkAndSweepGarbageCollector(vm);
					}
				};
			}
		};
		vm.initialize();
		vm.getInterface().setInvoker(vm.getSymbols().java_lang_System(), "loadLibrary", "(Ljava/lang/String;)V", MethodInvoker.interpreted(ctx -> {
			if ("zip".equals(vm.getHelper().readUtf8(ctx.getLocals().loadReference(0)))) {
				invokeGC(vm.getMemoryManager());
			}
			return Result.CONTINUE;
		}));
		vm.bootstrap();
		MemoryManager memoryManager = vm.getMemoryManager();
		invokeGC(memoryManager);
		invokeGC(memoryManager);
	}

	private static void invokeGC(MemoryManager memoryManager) {
		Collection<ObjectValue> objects = memoryManager.listObjects();
		System.out.println("allocated objects: " + objects.size());
		Thread invokeGC = new Thread(() -> {
			long now = System.currentTimeMillis();
			memoryManager.getGarbageCollector().invoke();
			System.out.println("gc took:" + (System.currentTimeMillis() - now));
		}, "GC Thread");
		invokeGC.start();
		try {
			invokeGC.join();
		} catch (InterruptedException ignored) {
		}
		System.out.println("allocated objects after GC: " + objects.size());
	}
	*/
}
