package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.SupplyingClassLoader;
import dev.xdark.ssvm.classloading.SupplyingClassLoaderInstaller;
import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.IOUtil;
import dev.xdark.ssvm.value.InstanceValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static dev.xdark.ssvm.TestUtil.unchecked;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link SupplyingClassLoader}
 */
public class SupplyingClassLoaderTest {
	@Test
	public void doTest() throws IOException {
		VirtualMachine vm = new VirtualMachine();
		vm.bootstrap();
		InvocationUtil util = InvocationUtil.create(vm);

		// Install loader into VM
		SupplyingClassLoaderInstaller.Helper helper = SupplyingClassLoaderInstaller.install(vm,
				className -> unchecked(() -> IOUtil.readAll(ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class"))),
				resourceName -> null);

		// The dummy class should be loadable, and we should be able to call its methods
		InstanceClass dummyClassInstance = assertDoesNotThrow(() -> helper.loadClass(DummyClass.class.getName()));
		InstanceValue dummyInstance = vm.getMemoryManager().newInstance(dummyClassInstance);
		util.invokeVoid(dummyClassInstance.getMethod("<init>", "()V"), Argument.reference(dummyInstance));
		int value = util.invokeInt(dummyClassInstance.getMethod("random", "()I"));
		int value2 = util.invokeInt(dummyClassInstance.getMethod("randomInstance", "()I"), Argument.reference(dummyInstance));
		System.out.println("static:  " + value);
		System.out.println("virtual: " + value2);

		// Loading a class that does not exist should fail with ClassNotFoundException
		assertThrows(ClassNotFoundException.class, () -> helper.loadClass(UUID.randomUUID().toString()));
	}
}
