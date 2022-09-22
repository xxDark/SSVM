package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.SimpleInstanceJavaClass;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

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
	public synchronized ClassLoaderData setClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			if (bootClassLoaderData != null) {
				throw new IllegalStateException("Class loader data for boot loader is already set");
			}
			return bootClassLoaderData = createClassLoaderData();
		} else {
			VMOperations ops = vm.getTrustedOperations();
			InstanceValue instance = (InstanceValue) classLoader;
			if (!ops.getReference(instance, NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;").isNull()) {
				throw new IllegalStateException("Class loader data for " + classLoader + " is already set");
			}
			ClassLoaderData data = createClassLoaderData();
			JavaValue<ClassLoaderData> oop = vm.getMemoryManager().newJavaInstance(vm.getSymbols().java_lang_Object(), data);
			ops.putReference(instance, NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;", oop);
			classLoaders.add(instance);
			return data;
		}
	}

	@Override
	public ClassLoaderData getClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			return bootClassLoaderData;
		}
		return ((JavaValue<ClassLoaderData>) vm.getTrustedOperations().getReference(classLoader, NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
	}

	@Override
	public Collection<InstanceValue> getAll() {
		return classLoadersView;
	}

	@Override
	public void setClassOop(InstanceJavaClass javaClass) {
		javaClass.setOop(vm.getMemoryManager().newClassOop(javaClass));
	}

	@Override
	public void initializeBootOop(InstanceJavaClass javaClass, InstanceJavaClass javaLangClass) {
		VirtualMachine vm = this.vm;
		MemoryManager memoryManager = vm.getMemoryManager();
		JavaValue<InstanceJavaClass> oop = javaLangClass == javaClass ? memoryManager.newJavaLangClass(javaClass) : memoryManager.newClassOop(javaClass);
		javaClass.setOop(oop);
	}

	@Override
	public void setClassData(InstanceJavaClass javaClass, ObjectValue classData) {
		vm.getTrustedOperations().putReference(javaClass.getOop(), "classData", "Ljava/lang/Object;", classData);
	}

	@Override
	public ObjectValue getClassData(InstanceJavaClass javaClass) {
		return vm.getTrustedOperations().getReference(javaClass.getOop(), "classData", "Ljava/lang/Object;");
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
