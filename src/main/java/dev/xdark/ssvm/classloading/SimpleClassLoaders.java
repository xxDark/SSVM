package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.metadata.MetadataStorage;
import dev.xdark.ssvm.metadata.SimpleMetadataStorage;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple class loaders storage.
 *
 * @author xDark
 */
public class SimpleClassLoaders implements ClassLoaders {

	private final Set<InstanceValue> classLoaders = new HashSet<>();
	private final Set<InstanceValue> classLoadersView = Collections.unmodifiableSet(classLoaders);
	private final MetadataStorage<ClassLoaderData> classLoaderMap = new SimpleMetadataStorage<>();
	private final VirtualMachine vm;
	private ClassLoaderData bootClassLoaderData;

	public SimpleClassLoaders(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public synchronized ClassLoaderData setClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			if (bootClassLoaderData != null) {
				throw new IllegalStateException("Class loader data for boot loader is already set");
			}
			return bootClassLoaderData = createClassLoaderData();
		} else {
			VMOperations ops = vm.getOperations();
			InstanceValue instance = (InstanceValue) classLoader;
			ClassLoaderData data = createClassLoaderData();
			ops.putInt(instance, InjectedClassLayout.java_lang_ClassLoader_oop.name(), classLoaderMap.register(data));
			classLoaders.add(instance);
			return data;
		}
	}

	@Override
	public ClassLoaderData getClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			return bootClassLoaderData;
		}
		return classLoaderMap.lookup(vm.getOperations().getInt(classLoader, InjectedClassLayout.java_lang_ClassLoader_oop.name()));
	}

	@Override
	public Collection<InstanceValue> getAll() {
		return classLoadersView;
	}

	@Override
	public void initializeBootOop(InstanceClass javaClass, InstanceClass javaLangClass) {
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue oop = javaLangClass == javaClass ? memoryManager.newJavaLangClass(javaClass) : memoryManager.newClassOop(javaClass);
		javaClass.setOop(oop);
	}

	@Override
	public void setClassData(InstanceClass javaClass, ObjectValue classData) {
		vm.getOperations().putReference(javaClass.getOop(), "classData", "Ljava/lang/Object;", classData);
	}

	@Override
	public ObjectValue getClassData(InstanceClass javaClass) {
		return vm.getOperations().getReference(javaClass.getOop(), "classData", "Ljava/lang/Object;");
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
