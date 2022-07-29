package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
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
		JavaMethod clone = vm.getPublicLinkResolver().resolveVirtualMethod(array, "clone", "()Ljava/lang/Object;");
		Locals locals = vm.getThreadStorage().newLocals(clone);
		locals.set(0, array);
		ArrayValue copy = (ArrayValue) helper.invoke(clone, locals).getResult();
		assertArrayEquals(bytes, helper.toJavaBytes(copy));
	}
}
