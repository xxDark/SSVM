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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * Installs a {@link SupplyingClassLoader} into the target {@link VirtualMachine}.
 *
 * @author Matt Coley
 * @see SupplyingClassLoader
 */
public class SupplyingClassLoaderInstaller {
	/**
	 * @param vm
	 * 		Virtual machine to install into.
	 *
	 * @return Class reference to {@link SupplyingClassLoader} in the VM.
	 *
	 * @throws IOException
	 * 		When the {@link SupplyingClassLoader} class cannot be streamed.
	 */
	public static Helper install(VirtualMachine vm,
								 Function<String, byte[]> classProvider,
								 Function<String, byte[]> resourceProvider) throws IOException {
		String className = SupplyingClassLoader.class.getName();
		InputStream classStream = ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class");
		if (classStream == null)
			throw new FileNotFoundException(className);


		// Define the class in the VM
		byte[] bytes = IOUtil.readAll(classStream);
		ObjectValue nullV = vm.getMemoryManager().nullValue();
		String sourceName = SupplyingClassLoader.class.getSimpleName() + ".java";
		InstanceValue loader = ClassLoaderUtils.systemClassLoader(vm);
		VMOperations operations = vm.getOperations();
		InstanceClass loaderClass = operations.defineClass(loader, className, bytes, 0, bytes.length, nullV, sourceName, 0);

		// Register invoker for the class's native provide methods to use the provided functions.
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(loaderClass, "provideClass", "(Ljava/lang/String;)[B",
				getMethodInvoker(classProvider, operations, vm.getMemoryManager()));
		vmi.setInvoker(loaderClass, "provideResource", "(Ljava/lang/String;)[B",
				getMethodInvoker(resourceProvider, operations, vm.getMemoryManager()));

		// Yield the loader type.
		return new Helper(vm, loaderClass);
	}

	@NotNull
	private static MethodInvoker getMethodInvoker(Function<String, byte[]> supplier, VMOperations operations,
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
