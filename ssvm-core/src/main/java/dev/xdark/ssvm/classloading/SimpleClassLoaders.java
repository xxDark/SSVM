package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.metadata.MetadataStorage;
import dev.xdark.ssvm.metadata.SimpleMetadataStorage;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.util.Assertions;
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
	private long anonymousClassLoaderOffset = -1L;

	public SimpleClassLoaders(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public synchronized ClassLoaderData createClassLoaderData(ObjectValue classLoader) {
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
	public ClassLoaderData createAnonymousClassLoaderData(InstanceClass klass) {
		ClassLoaderData original = getClassLoaderData(klass.getClassLoader());
		Assertions.notNull(original, "class loader data must be set");
		ClassLoaderData delegate = new AnonymousClassLoaderData(createClassLoaderData(), original);
		klass.getOop().getData().writeInt(anonymousClassLoaderOffset(), classLoaderMap.register(delegate));
		return delegate;
	}

	@Override
	public ClassLoaderData getClassLoaderData(JavaClass klass) {
		InstanceValue oop = klass.getOop();
		if (oop != null) {
			int id = oop.getData().readInt(anonymousClassLoaderOffset());
			if (id > 0) {
				return classLoaderMap.lookup(id);
			}
		}
		return getClassLoaderData(klass.getClassLoader());
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

	private long anonymousClassLoaderOffset() {
		long anonymousClassLoaderOffset = this.anonymousClassLoaderOffset;
		if (anonymousClassLoaderOffset == -1L) {
			anonymousClassLoaderOffset = vm.getSymbols().java_lang_Class().getField(InjectedClassLayout.java_lang_Class_anonymousClassLoader.name(), "I").getOffset();
			this.anonymousClassLoaderOffset = anonymousClassLoaderOffset;
		}
		return anonymousClassLoaderOffset;
	}
}
