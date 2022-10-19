package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.value.InstanceValue;
import org.junit.jupiter.api.Test;

public final class VMBootTest {

	@Test
	public void testBoot() {
		VirtualMachine vm = new VirtualMachine();
		try {
			vm.bootstrap();
		} catch (Exception ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof VMException) {
				InstanceValue oop = ((VMException) cause).getOop();
				if (oop.getJavaClass() == vm.getSymbols().java_lang_ExceptionInInitializerError()) {
					oop = (InstanceValue) vm.getOperations().getReference(oop, "exception", "Ljava/lang/Throwable;");
				}
				vm.getOperations().toJavaException(oop).printStackTrace();
			} else {
				throw ex;
			}
		}
	}
}
