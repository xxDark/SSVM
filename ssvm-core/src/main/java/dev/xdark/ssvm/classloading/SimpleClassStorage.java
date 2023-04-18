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
	private long offset = -1L;

	public SimpleClassStorage(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public JavaClass lookup(ObjectValue oop) {
		int id = oop.getData().readInt(offset());
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
		oop.getData().writeInt(offset(), id);
		return id;
	}

	private long offset() {
		long offset = this.offset;
		if (offset == -1L) {
			JavaField field = vm.getSymbols().java_lang_Class().getField(
				InjectedClassLayout.java_lang_Class_id.name(),
				"I"
			);
			offset = field.getOffset();
			this.offset = offset;
		}
		return offset;
	}
}
