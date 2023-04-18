package dev.xdark.ssvm.mirror;

public interface MemberKey {

	/**
	 * Returns member owner.
	 *
	 * @return member owner.
	 */
	InstanceJavaClass getOwner();

	/**
	 * Returns member name.
	 *
	 * @return member name.
	 */
	String getName();

	/**
	 * Returns member desc.
	 *
	 * @return member desc.
	 */
	String getDesc();
}
