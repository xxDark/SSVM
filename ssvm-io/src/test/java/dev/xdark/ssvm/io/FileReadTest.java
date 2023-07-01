package dev.xdark.ssvm.io;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.SupplyingClassLoaderInstaller;
import dev.xdark.ssvm.dummy.FileToString;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.HostFileManager;
import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static dev.xdark.ssvm.classloading.SupplyingClassLoaderInstaller.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for file IO within a VM.
 */
public class FileReadTest {
	@Test
	public void doTest() throws IOException {
		String readmeBaseline = FileToString.readReadmePath();

		VirtualMachine vm = new VirtualMachine() {
			@Override
			protected FileManager createFileManager() {
				return new HostFileManager();
			}
		};
		vm.bootstrap();
		InvocationUtil util = InvocationUtil.create(vm);

		// Install loader into VM that can pull classes from the current classpath
		Helper helper = SupplyingClassLoaderInstaller.install(vm, SupplyingClassLoaderInstaller.supplyFromRuntime());

		// The IO class should be loadable, and we should be able to call its methods
		InstanceClass dummyClassInstance = Assertions.assertDoesNotThrow(() -> helper.loadClass(FileToString.class.getName()));
		InstanceValue dummyInstance = vm.getMemoryManager().newInstance(dummyClassInstance);
		util.invokeVoid(dummyClassInstance.getMethod("<init>", "()V"), Argument.reference(dummyInstance));

	//	String readReadmeFile = util.invokeStringReference(dummyClassInstance.getMethod("readReadmeFile", "()Ljava/lang/String;"));
	//	assertEquals(readmeBaseline, readReadmeFile);

	 	String readReadmePath = util.invokeStringReference(dummyClassInstance.getMethod("readReadmePath", "()Ljava/lang/String;"));
		assertEquals(readmeBaseline, readReadmePath);
	}
}
