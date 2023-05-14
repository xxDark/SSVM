## Basic example

Shows how to boot VM & run an application.

```java
import dev.xdark.ssvm.classloading.SupplyingClassLoader;
import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;

import static dev.xdark.ssvm.classloading.SupplyingClassLoaderInstaller.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SsvmRunner {
    public static void main(String[] args) {
        VirtualMachine vm = new VirtualMachine() {
            // Provide any overriding behaviors here
        };
        vm.bootstrap();
        MemoryManager memoryManager = vm.getMemoryManager();

        // Create a loader pulling classes and files from a root directory
        Helper helper = install(vm, supplyFromDirectory(directory));

        // Create a loader pulling classes and files from a zip/jar file
        Helper helper = install(vm, supplyFromZip(jarFile));

        // Create a loader pulling from multiple paths to directories and zip/jar files
        //  - Items appearing first are preferred in loading order over others
        Helper helper = install(vm, supplyFromPaths(jarFile, directory));

        // Create a loader pulling from multiple suppliers appended together
        //  - The first supplier is preferred and appended items are then used in reverse order
        Helper helper = install(vm, supplyFromZip(jarFile)
                .append(supplyFromZip(library1))
                .append(supplyFromZip(library2))
        );

        // Invoke the 'main' method
        InstanceClass mainClassInstance = assertDoesNotThrow(() -> helper.loadClass("com/example/ProgramName"));
        ArrayValue argsValue = memoryManager
                .newArray(vm.getSymbols().java_lang_String().getArrayClass(), 0);

        // If you did want to specify arguments (instead of setting length to 0) you can do so like this:
        //   VMOperations ops = vm.getOperations();
        //   ops.arrayStoreReference(argsValue, 0, ops.newUtf8("--file"));
        //   ops.arrayStoreReference(argsValue, 1, ops.newUtf8("hello.txt"));

        // Call main
        InvocationUtil util = InvocationUtil.create(vm);
        util.invokeVoid(mainClassInstance.getMethod("main", "([Ljava/lang/String;)V"), Argument.reference(argsValue));

        // Or, call an instance method, like Runnable.run()
        InstanceValue mainInstance = memoryManager.newInstance(mainClassInstance);
        util.invokeVoid(mainClassInstance.getMethod("<init>", "()V"), Argument.reference(mainInstance));
        util.invokeVoid(mainClassInstance.getMethod("run", "()V"), Argument.reference(mainInstance));
    }
}
```