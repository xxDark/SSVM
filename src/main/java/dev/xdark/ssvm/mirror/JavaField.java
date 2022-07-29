package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.util.MetadataHolder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

/**
 * Java field.
 *
 * @author xDark
 */
public interface JavaField extends MetadataHolder {

	/**
	 * Returns field owner.
	 *
	 * @return field owner.
	 */
	InstanceJavaClass getOwner();

	/**
	 * Returns ASM field info.
	 *
	 * @return ASM field info.
	 */
	FieldNode getNode();

	/**
	 * Returns field slot.
	 *
	 * @return field slot.
	 */
	int getSlot();

	/**
	 * Returns field offset.
	 *
	 * @return field offset.
	 */
	long getOffset();

	/**
	 * Returns field name.
	 *
	 * @return field name.
	 */
	default String getName() {
		return getNode().name;
	}

	/**
	 * Returns field descriptor.
	 *
	 * @return field descriptor.
	 */
	default String getDesc() {
		return getNode().desc;
	}

	/**
	 * Returns field access.
	 *
	 * @return field access.
	 */
	default int getAccess() {
		return getNode().access;
	}

	/**
	 * Returns field signature.
	 *
	 * @return field signature.
	 */
	default String getSignature() {
		return getNode().signature;
	}

	/**
	 * Returns field type.
	 *
	 * @return field type.
	 */
	Type getType();
}
