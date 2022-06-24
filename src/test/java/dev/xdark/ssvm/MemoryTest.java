package dev.xdark.ssvm;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.gc.GarbageCollector;
import dev.xdark.ssvm.memory.gc.MarkAndSweepGarbageCollector;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.SimpleMemoryManager;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class MemoryTest {

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
			if ("zip".equals(vm.getHelper().readUtf8(ctx.getLocals().load(0)))) {
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
}
