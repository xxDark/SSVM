package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.SupplyingClassLoader;
import dev.xdark.ssvm.dummy.FileStream;
import dev.xdark.ssvm.dummy.RandomProvider;
import dev.xdark.ssvm.dummy.StringCoderLength;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.HostFileManager;
import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static dev.xdark.ssvm.classloading.SupplyingClassLoaderInstaller.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SupplyingClassLoader}
 */
public class SupplyingClassLoaderTest {
	private static VirtualMachine vm;
	private static InvocationUtil util;

	@BeforeAll
	public static void setup() {
		vm = new VirtualMachine() {
			@Override
			protected FileManager createFileManager() {
				return new HostFileManager();
			}
		};
		vm.getProperties().put("java.class.path", "");
		vm.bootstrap();
		util = InvocationUtil.create(vm);
	}

	@Test
	public void testRandom() throws IOException {
		// Create a loader pulling classes and files from the current runtime
		Helper helper = install(vm, supplyFromRuntime());

		// The dummy class should be loadable, and we should be able to call its methods
		InstanceClass dummyClassInstance = assertDoesNotThrow(() -> helper.loadClass(RandomProvider.class.getName()));
		InstanceValue dummyInstance = vm.getMemoryManager().newInstance(dummyClassInstance);
		util.invokeVoid(dummyClassInstance.getMethod("<init>", "()V"), Argument.reference(dummyInstance));
		int value = util.invokeInt(dummyClassInstance.getMethod("random", "()I"));
		int value2 = util.invokeInt(dummyClassInstance.getMethod("randomInstance", "()I"), Argument.reference(dummyInstance));
		System.out.println("static:  " + value);
		System.out.println("virtual: " + value2);

		// Loading a class that does not exist should fail with ClassNotFoundException
		assertThrows(ClassNotFoundException.class, () -> helper.loadClass(UUID.randomUUID().toString()));

	}

	@Test
	public void testFileStream() throws IOException {
		Helper helper = install(vm, supplyFromRuntime());
		InstanceClass dummyClassInstance = assertDoesNotThrow(() -> helper.loadClass(FileStream.class.getName()));

		// Calling static method should read from the README text file
		String read = util.invokeStringReference(dummyClassInstance.getMethod("streamImportantFile", "()Ljava/lang/String;"));
		assertTrue(read.contains("ssvm-invoke"));
	}

	@Test
	public void testStringCoder() throws IOException {
		Helper helper = install(vm, supplyFromRuntime());
		InstanceClass dummyClassInstance = assertDoesNotThrow(() -> helper.loadClass(StringCoderLength.class.getName()));
		assertDoesNotThrow(() -> util.invokeVoid(dummyClassInstance.getMethod("coderCmp", "()V")));
	}
}
