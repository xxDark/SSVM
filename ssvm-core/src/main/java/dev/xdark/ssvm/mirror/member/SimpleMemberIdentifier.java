package dev.xdark.ssvm.mirror.member;

/**
 * Class member identifier.
 *
 * @author xDark
 */
public final class SimpleMemberIdentifier implements MemberIdentifier {

	private final String name;
	private final String desc;

	/**
	 * @param name  Member name.
	 * @param desc  Member desc.
	 */
	public SimpleMemberIdentifier(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MemberIdentifier)) {
			return false;
		}

		MemberIdentifier memberKey = (MemberIdentifier) o;

		if (!name.equals(memberKey.getName())) {
			return false;
		}
		return desc.equals(memberKey.getDesc());
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}
}
