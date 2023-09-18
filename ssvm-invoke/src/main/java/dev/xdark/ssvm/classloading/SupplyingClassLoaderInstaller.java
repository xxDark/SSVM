package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.util.ClassLoaderUtils;
import dev.xdark.ssvm.util.IOUtil;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static dev.xdark.ssvm.util.Unchecked.supplier;

/**
 * Installs a {@link SupplyingClassLoader} into the target {@link VirtualMachine}.
 *
 * @author Matt Coley
 * @see SupplyingClassLoader
 */
public class SupplyingClassLoaderInstaller {
	private static int loaderIndex;

	/**
	 * @param path
	 * 		Root directory to pull data from.
	 *
	 * @return Supplier that pulls classes and files from the given directory.
	 *
	 * @throws IOException
	 * 		When the path was not a directory.
	 */
	public static DataSupplier supplyFromDirectory(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Function<String, byte[]> classProvider = n -> {
				Path classPath = path.resolve(n.replace('.', '/') + ".class");
				if (Files.isRegularFile(classPath))
					return supplier(() -> Files.readAllBytes(classPath));
				return null;
			};
			Function<String, byte[]> resourceProvider = n -> {
				if (n.startsWith("/"))
					n = n.substring(1);
				Path resourcePath = path.resolve(n);
				if (Files.isRegularFile(resourcePath))
					return supplier(() -> Files.readAllBytes(resourcePath));
				return null;
			};
			return supplyFromFunctions(classProvider, resourceProvider);
		}

		// Not a directory
		throw new IOException("Path was not a directory: " + path);
	}

	/**
	 * @param path
	 * 		Path to ZIP/JAR file.
	 *
	 * @return Supplier that pulls classes and files from the given archive.
	 *
	 * @throws IOException
	 * 		When the ZIP file could not be read.
	 */
	public static DataSupplier supplyFromZip(Path path) throws IOException {
		Map<String, byte[]> contents = new HashMap<>();
		try (ZipFile zipFile = new ZipFile(path.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getSize() > 0) {
					InputStream stream = zipFile.getInputStream(entry);
					contents.put(entry.getName(), IOUtil.readAll(stream));
				}
			}
		}

		Function<String, byte[]> classProvider = n -> contents.get(n.replace('.', '/') + ".class");
		Function<String, byte[]> resourceProvider = n -> {
			if (n.startsWith("/"))
				n = n.substring(1);
			return contents.get(n);
		};

		return supplyFromFunctions(classProvider, resourceProvider);
	}

	/**
	 * @param paths
	 * 		Paths to pull data from, in order where the first items are used first for look-ups.
	 *
	 * @return Supplier that pulls classes and files from the given paths <i>(ZIP/JAR or directory)</i>.
	 *
	 * @throws IOException
	 * 		When one or more paths could not be read from.
	 * @see #supplyFromDirectory(Path)
	 * @see #supplyFromZip(Path)
	 */
	public static DataSupplier supplyFromPaths(Path... paths) throws IOException {
		DataSupplier supplier = null;

		// Go in reverse order, using supplier prefixing so that the first paths in the array have preference
		// over data in following paths.
		for (int i = paths.length - 1; i >= 0; i--) {
			Path path = paths[i];

			// Treat as root directory
			if (Files.isDirectory(path)) {
				DataSupplier fromDirectory = supplyFromDirectory(path);
				supplier = (supplier == null) ? fromDirectory : supplier.prefix(fromDirectory);
			} else if (Files.isRegularFile(path)) {
				DataSupplier fromDirectory = supplyFromZip(path);
				supplier = (supplier == null) ? fromDirectory : supplier.prefix(fromDirectory);
			}
		}

		// Fallback if no paths were provided
		if (supplier == null)
			supplier = supplyNothing();

		return supplier;
	}

	/**
	 * @return Supplier that pulls classes and files from the current classpath. Mostly useful for testing.
	 */
	public static DataSupplier supplyFromRuntime() {
		return supplyFromFunctions(
				className -> supplier(() -> IOUtil.readAll(ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class"))),
				resourceName -> supplier(() -> IOUtil.readAll(ClassLoader.getSystemResourceAsStream(resourceName))));
	}

	/**
	 * @param classes
	 * 		Map of internal class names to class bytecode.
	 * @param resources
	 * 		Map of file paths to file contents.
	 *
	 * @return Supplier that pulls classes and files from the given maps.
	 */
	public static DataSupplier supplyFromMaps(Map<String, byte[]> classes, Map<String, byte[]> resources) {
		return supplyFromFunctions(className -> {
					return classes.get(className.replace('.', '/'));
				},
				resourceName -> {
					if (resourceName.startsWith("/"))
						resourceName = resourceName.substring(1);
					return resources.get(resourceName);
				});
	}

	/**
	 * @param classProvider
	 * 		Function to lookup class bytecode by its internal name.
	 * @param resourceProvider
	 * 		Function to lookup a file's content by its path name.
	 *
	 * @return Supplier that pulls classes and files from the given functions.
	 */
	public static DataSupplier supplyFromFunctions(Function<String, byte[]> classProvider,
												   Function<String, byte[]> resourceProvider) {
		return new DataSupplier() {
			@Override
			public byte[] getClass(String className) {
				return classProvider.apply(className);
			}

			@Override
			public byte[] getResource(String resourcePath) {
				return resourceProvider.apply(resourcePath);
			}
		};
	}

	/**
	 * @return Supplier of nothing.
	 */
	public static DataSupplier supplyNothing() {
		return new DataSupplier() {
			@Override
			public byte[] getClass(String className) {
				return null;
			}

			@Override
			public byte[] getResource(String resourcePath) {
				return null;
			}
		};
	}

	/**
	 * Creates a {@link SupplyingClassLoader} with class and resource loading definitions
	 * based on the given data supplier.
	 *
	 * @param vm
	 * 		Virtual machine to install into.
	 * @param supplier
	 * 		Supplier to provide {@code byte[]} of classes and resources.
	 *
	 * @return Helper for interacting with the {@link SupplyingClassLoader} loaded into the VM.
	 *
	 * @throws IOException
	 * 		When the {@link SupplyingClassLoader} class cannot be streamed.
	 */
	public static Helper install(VirtualMachine vm,
								 DataSupplier supplier) throws IOException {
		String loaderName = SupplyingClassLoader.class.getName();
		InputStream loaderStream = ClassLoader.getSystemResourceAsStream(loaderName.replace('.', '/') + ".class");
		if (loaderStream == null)
			throw new FileNotFoundException(loaderName);

		// Define the class in the VM with a unique name so that multiple installs can be done.
		String uniqueName = loaderName + loaderIndex++;
		byte[] bytes = map(IOUtil.readAll(loaderStream), loaderName, uniqueName);
		ObjectValue nullV = vm.getMemoryManager().nullValue();
		String sourceName = uniqueName.substring(uniqueName.lastIndexOf('/') + 1) + ".java";
		InstanceValue loader = ClassLoaderUtils.systemClassLoader(vm);
		VMOperations operations = vm.getOperations();
		InstanceClass loaderClass = operations.defineClass(loader, uniqueName, bytes, 0, bytes.length, nullV, sourceName, 0);

		// Register invoker for the class's native provide methods to use the provided functions.
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(loaderClass, "provideClass", "(Ljava/lang/String;)[B",
				getMethodInvoker(supplier::getClass, operations, vm.getMemoryManager()));
		vmi.setInvoker(loaderClass, "provideResource", "(Ljava/lang/String;)[B",
				getMethodInvoker(supplier::getResource, operations, vm.getMemoryManager()));

		// Yield the loader type.
		return new Helper(vm, loaderClass);
	}

	private static byte[] map(byte[] bytes, String fromName, String toName) {
		ClassWriter writer = new ClassWriter(0);
		ClassReader reader = new ClassReader(bytes);
		reader.accept(new ClassRemapper(writer, new SimpleRemapper(fromName.replace('.', '/'), toName.replace('.', '/'))), 0);
		return writer.toByteArray();
	}

	@NotNull
	private static MethodInvoker getMethodInvoker(Function<String, byte[]> supplier,
												  VMOperations operations,
												  MemoryManager memoryManager) {
		return ctx -> {
			// Read the parameter name from the VM.
			String paramName = operations.readUtf8(ctx.getLocals().loadReference(1));

			// Set the result according to the function return value.
			byte[] funcBytes = supplier.apply(paramName);
			if (funcBytes == null)
				ctx.setResult(memoryManager.nullValue());
			else
				ctx.setResult(operations.toVMBytes(funcBytes));

			// Used for native method implementations.
			return Result.ABORT;
		};
	}

	/**
	 * Model to represent the implementation the native functions of {@link SupplyingClassLoader}.
	 */
	public interface DataSupplier {
		/**
		 * @param className
		 * 		Class name, such as {@code com.example.Foo}
		 * 		or {@code com.example.Foo$Bar} for an inner class.
		 *
		 * @return Raw bytes of class, or {@code null} if not found.
		 */
		byte[] getClass(String className);

		/**
		 * @param resourcePath
		 * 		File path name such as {@code subdir/Filename.txt}
		 *
		 * @return Raw bytes of file, or {@code null} if not found.
		 */
		byte[] getResource(String resourcePath);

		/**
		 * @param other
		 * 		Other {@link DataSupplier} to append to the chain.
		 *
		 * @return New data supplier which checks for results in {@code this} supplier first,
		 * but then uses the given {@code other} as a fallback.
		 */
		default DataSupplier append(DataSupplier other) {
			DataSupplier current = this;
			return new DataSupplier() {
				@Override
				public byte[] getClass(String className) {
					byte[] classFile = current.getClass(className);
					if (classFile == null)
						classFile = other.getClass(className);
					return classFile;
				}

				@Override
				public byte[] getResource(String resourcePath) {
					byte[] resource = current.getResource(resourcePath);
					if (resource == null)
						resource = other.getResource(resourcePath);
					return resource;
				}
			};
		}

		/**
		 * @param other
		 * 		Other {@link DataSupplier} to append to the chain.
		 *
		 * @return New data supplier which checks for results in the {@code other} supplier first,
		 * but then uses the current {@code 'this'} supplier as a fallback.
		 */
		default DataSupplier prefix(DataSupplier other) {
			DataSupplier current = this;
			return new DataSupplier() {
				@Override
				public byte[] getClass(String className) {
					byte[] classFile = other.getClass(className);
					if (classFile == null)
						classFile = current.getClass(className);
					return classFile;
				}

				@Override
				public byte[] getResource(String resourcePath) {
					byte[] resource = other.getResource(resourcePath);
					if (resource == null)
						resource = current.getResource(resourcePath);
					return resource;
				}
			};
		}
	}

	/**
	 * Helper wrapping common operations for using {@link SupplyingClassLoader}.
	 * <p><b>Example:</b>
	 * <pre>
	 * {@code
	 * InvocationUtil util = InvocationUtil.create(vm);
	 *
	 * // Load DummyClass.class
	 * var helper = SupplyingClassLoaderInstaller.install(vm, cs, rs);
	 * InstanceClass dummyClass = helper.loadClass("DummyClass");
	 *
	 * // Make a new instance of DummyClass
	 * InstanceValue dummyInstance = vm.getMemoryManager().newInstance(dummyClassInstance);
	 * util.invokeVoid(dummyClassInstance.getMethod("<init>", "()V"), Argument.reference(dummyInstance));
	 *
	 * // Calling methods (static & virtual)
	 * int value = util.invokeInt(dummyClassInstance.getMethod("randomStatic", "()I"));
	 * int value = util.invokeInt(dummyClassInstance.getMethod("randomInstance", "()I"), Argument.reference(dummyInstance));
	 * }
	 * </pre>
	 */
	public static class Helper {
		private final VirtualMachine vm;
		private final InvocationUtil invokeUtil;
		private final VMOperations operations;
		private final ClassStorage classStorage;
		private final InstanceClass loaderClass;
		private InstanceValue loaderInstance;
		private JavaMethod loadClassMethod;

		/**
		 * @param vm
		 * 		VM to operate within.
		 * @param loaderClass
		 * 		Class reference to {@link SupplyingClassLoader} in the VM.
		 */
		public Helper(VirtualMachine vm, InstanceClass loaderClass) {
			this.vm = vm;
			this.loaderClass = loaderClass;
			invokeUtil = InvocationUtil.create(vm);
			operations = vm.getOperations();
			classStorage = vm.getClassStorage();
		}

		/**
		 * @return The class-loader instance used to load classes into the VM.
		 */
		public InstanceValue getClassLoaderInstance() {
			init(); // Ensure loaded
			return loaderInstance;
		}

		/**
		 * Initialize the {@link SupplyingClassLoader} inside the {@link VirtualMachine}.
		 */
		private void init() {
			if (loaderInstance == null) {
				InvocationUtil util = InvocationUtil.create(vm);

				// Initialize an instance of the classloader
				loaderInstance = vm.getMemoryManager().newInstance(loaderClass);
				util.invokeVoid(loaderClass.getMethod("<init>", "()V"), Argument.reference(loaderInstance));

				// Locate the ClassLoader.loadClass(...) method
				loadClassMethod = vm.getSymbols().java_lang_ClassLoader()
						.getMethod("loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
			}
		}

		/**
		 * @param name
		 * 		Some type name as {@link Class#getName()}.
		 * 		For example {@code java.lang.String} or {@code com.example.Outer$Inner}
		 *
		 * @return {@code T.class} reference where {@code T} is the type given by the provided name.
		 */
		public InstanceClass loadClass(String name) throws ClassNotFoundException {
			// Ensure loader is initialized
			init();

			// Class<T> where T is the type of 'name'
			try {
				ObjectValue classReference = invokeUtil.invokeReference(loadClassMethod,
						Argument.reference(loaderInstance),
						Argument.reference(operations.newUtf8(name))
				);

				// Convert to T.class
				return (InstanceClass) classStorage.lookup(classReference);
			} catch (VMException ex) {
				InstanceValue oop = ex.getOop();

				// Check if the VM exception's oop is a ClassNotFoundException.
				// Can be useful when T references another class that cannot be loaded, such as X.
				// Thus our exception reports 'not found: X'
				if (vm.getSymbols().java_lang_ClassNotFoundException() == vm.getMemoryManager().readClass(oop)) {
					ObjectValue messageValue = invokeUtil.invokeReference(vm.getSymbols().java_lang_Throwable().getMethod("getMessage", "()Ljava/lang/String;"),
							Argument.reference(oop));
					String notFoundType = operations.readUtf8(messageValue);
					throw new ClassNotFoundException(notFoundType);
				}

				// Not a ClassNotFoundException, just rethrow the VM exception
				throw ex;
			}
		}
	}
}
