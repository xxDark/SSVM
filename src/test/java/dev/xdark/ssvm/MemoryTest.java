package dev.xdark.ssvm;

import org.junit.jupiter.api.Test;

public class MemoryTest {

	@Test
	public void testGC() {
		VirtualMachine vm = new VirtualMachine();
		vm.bootstrap();
		System.out.println("allocated objects: " + vm.getMemoryManager().listObjects().size());
	}
}
