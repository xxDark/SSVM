package dev.xdark.ssvm.mirror.member;

import dev.xdark.ssvm.mirror.type.JavaClass;
import org.objectweb.asm.tree.FieldNode;

/**
 * Java field.
 *
 * @author xDark
 */
public interface JavaField extends JavaMember {

	@Override
	default String getName() {
		return getNode().name;
	}

	@Override
	default String getDesc() {
		return getNode().desc;
	}

	@Override
	default int getModifiers() {
		return getNode().access;
	}

	@Override
	default String getSignature() {
		return getNode().signature;
	}

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
	 * Returns field type.
	 *
	 * @return field type.
	 */
	JavaClass getType();
}
