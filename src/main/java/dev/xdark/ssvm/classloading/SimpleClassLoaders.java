package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.util.Operations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Simple class loaders storage.
 *
 * @author xDark
 */
public class SimpleClassLoaders implements ClassLoaders {

	private final Set<InstanceValue> classLoaders = Collections.newSetFromMap(new WeakHashMap<>());
	private final Set<InstanceValue> classLoadersView = Collections.unmodifiableSet(classLoaders);
	private final Symbols symbols;
	private final Operations ops;
	private final MemoryManager memoryManager;
	private ClassLoaderData bootClassLoaderData;

	public SimpleClassLoaders(Symbols symbols, Operations ops, MemoryManager memoryManager) {
		this.symbols = symbols;
		this.ops = ops;
		this.memoryManager = memoryManager;
	}

	@Override
	public synchronized ClassLoaderData setClassLoaderData(ObjectValue classLoader) {
		if (classLoader.isNull()) {
			if (bootClassLoaderData != null) {
				throw new IllegalStateException("Class loader data for boot loader is already set");
			}
			return bootClassLoaderData = createClassLoaderData();
		} else {
			Operations ops = this.ops;
			InstanceValue instance = (InstanceValue) classLoader;
			if (!ops.getReference(instance, NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;").isNull()) {
				throw new IllegalStateException("Class loader data for " + classLoader + " is already set");
			}
			ClassLoaderData data = createClassLoaderData();
			JavaValue<ClassLoaderData> oop = memoryManager.newJavaInstance(symbols.java_lang_Object(), data);
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
		return ((JavaValue<ClassLoaderData>) ops.getReference(classLoader, NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
	}

	@Override
	public Collection<InstanceValue> getAll() {
		return classLoadersView;
	}

	@Override
	public void initializeBootOop(InstanceClass javaClass, InstanceClass javaLangClass) {
		MemoryManager memoryManager = this.memoryManager;
		InstanceValue oop = javaLangClass == javaClass ? memoryManager.newJavaLangClass(javaClass) : memoryManager.newClassOop(javaClass);
		javaClass.setOop(oop);
	}

	@Override
	public void setClassData(InstanceClass javaClass, ObjectValue classData) {
		ops.putReference(javaClass.getOop(), "classData", "Ljava/lang/Object;", classData);
	}

	@Override
	public ObjectValue getClassData(InstanceClass javaClass) {
		return ops.getReference(javaClass.getOop(), "classData", "Ljava/lang/Object;");
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
