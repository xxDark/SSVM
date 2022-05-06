package dev.xdark.ssvm;

import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CloneTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test
	public void testCloneObject() {
		VMHelper helper = vm.getHelper();
		byte[] bytes = "Hello, World".getBytes(StandardCharsets.UTF_8);
		ArrayValue array = helper.toVMBytes(bytes);
		ArrayValue copy = (ArrayValue) helper.invokeVirtual("clone", "()Ljava/lang/Object;", new Value[0], new Value[]{array}).getResult();
		assertArrayEquals(bytes, helper.toJavaBytes(copy));
	}
}
