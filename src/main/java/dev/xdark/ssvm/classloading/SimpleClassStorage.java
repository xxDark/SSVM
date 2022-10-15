package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.metadata.SimpleMetadataStorage;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Constantly expanding class storage.
 *
 * @author xDark
 */
public final class SimpleClassStorage extends SimpleMetadataStorage<JavaClass> implements ClassStorage {

	private final VirtualMachine vm;

	public SimpleClassStorage(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public JavaClass lookup(InstanceValue oop) {
		int id = vm.getOperations().getInt(oop, InjectedClassLayout.java_lang_Class_id.name());
		JavaClass mirror = lookup(id);
		Assertions.notNull(mirror, "no mirror");
		return mirror;
	}
}
