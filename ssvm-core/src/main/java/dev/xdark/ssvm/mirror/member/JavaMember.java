package dev.xdark.ssvm.mirror.member;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.MetadataHolder;

import java.util.BitSet;

/**
 * Member of a class.
 *
 * @author xDark
 */
public interface JavaMember extends MetadataHolder {

	/**
	 * Returns member owner.
	 *
	 * @return member owner.
	 */
	InstanceClass getOwner();

	/**
	 * Returns member name.
	 *
	 * @return member name.
	 */
	String getName();

	/**
	 * Returns member descriptor.
	 *
	 * @return member descriptor.
	 */
	String getDesc();

	/**
	 * Returns member access.
	 *
	 * @return member access.
	 */
	int getModifiers();

	/**
	 * Returns member signature.
	 *
	 * @return member signature.
	 */
	String getSignature();

	/**
	 * @return {@link BitSet} of extra modifiers.
	 */
	BitSet extraModifiers();

	/**
	 * @return Member identifier.
	 */
	MemberIdentifier getIdentifier();

	/**
	 * @return Member slot.
	 */
	int getSlot();
}
