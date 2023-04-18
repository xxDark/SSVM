package dev.xdark.ssvm.mirror.member;

/**
 * Member identifier.
 *
 * @author xDark
 */
public interface MemberIdentifier {

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

	static MemberIdentifier of(String name, String desc) {
		return new SimpleMemberIdentifier(name, desc);
	}
}
