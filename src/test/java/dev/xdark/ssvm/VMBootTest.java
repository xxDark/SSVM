package dev.xdark.ssvm;

import org.junit.jupiter.api.Test;

public final class VMBootTest {

	@Test
	public void testBoot() {
		new VirtualMachine().bootstrap();
	}
}
