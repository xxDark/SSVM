package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.VMException;
import org.junit.jupiter.api.Test;

public final class VMBootTest {

	@Test
	public void testBoot() {
		VirtualMachine vm = new VirtualMachine();
		try {
			vm.bootstrap();
		} catch (IllegalStateException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof VMException) {
				vm.getHelper().toJavaException(((VMException) cause).getOop()).printStackTrace();
			}
			throw ex;
		}
	}
}
