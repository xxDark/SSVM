package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Simple class loaders storage.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public class SimpleClassLoaders implements ClassLoaders {

	private final Set<InstanceValue> classLoaders = Collections.newSetFromMap(new WeakHashMap<>());
	private final Set<InstanceValue> classLoadersView = Collections.unmodifiableSet(classLoaders);
	private final VirtualMachine vm;
	private ClassLoaderData bootClassLoaderData;

	@Override
	public synchronized void register(InstanceValue classLoader) {
		classLoaders.add(classLoader);
	}

	@Override
	public ClassLoaderData setClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			if (bootClassLoaderData != null) {
				throw new IllegalStateException("Class loader data for boot loader is already set");
			}
			return bootClassLoaderData = createClassLoaderData();
		} else {
			val instance = (InstanceValue) classLoader;
			if (!instance.getValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;").isNull()) {
				throw new IllegalStateException("Class loader data for " + classLoader + " is already set");
			}
			val data = createClassLoaderData();
			val oop = vm.getMemoryManager().newJavaInstance(vm.getSymbols().java_lang_Object, data);
			instance.setValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;", oop);
			return data;
		}
	}

	@Override
	public ClassLoaderData getClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			return bootClassLoaderData;
		}
		return ((JavaValue<ClassLoaderData>) ((InstanceValue) classLoader).getValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
	}

	@Override
	public Collection<InstanceValue> getAll() {
		return classLoadersView;
	}

	/**
	 * Creates new class loader data.
	 *
	 * @return new class loader data.
	 */
	protected ClassLoaderData createClassLoaderData() {
		return new SimpleClassLoaderData();
	}
}
