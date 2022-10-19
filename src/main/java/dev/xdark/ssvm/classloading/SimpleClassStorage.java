package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.metadata.SimpleMetadataStorage;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

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
	public JavaClass lookup(ObjectValue oop) {
		JavaField field = vm.getSymbols().java_lang_Class().getField(
			InjectedClassLayout.java_lang_Class_id.name(),
			"I"
		);
		int id = oop.getData().readInt(field.getOffset());
		JavaClass mirror = lookup(id);
		Assertions.notNull(mirror, "no mirror");
		return mirror;
	}

	@Override
	protected int afterRegistration(JavaClass value, int id) {
		value.setId(id);
		InstanceValue oop = value.getOop();
		if (oop == null) {
			// Fixed by the VM
			return id;
		}
		JavaField field = vm.getSymbols().java_lang_Class().getField(
			InjectedClassLayout.java_lang_Class_id.name(),
			"I"
		);
		oop.getData().writeInt(field.getOffset(), id);
		return id;
	}
}
