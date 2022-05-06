package dev.xdark.ssvm.mirror;

/**
 * Class member info.
 *
 * @author xDark
 */
public final class SimpleMemberKey implements MemberKey {

	private final InstanceJavaClass owner;
	private final String name;
	private final String desc;

	/**
	 * @param owner
	 * 		Member owner.
	 * @param name
	 * 		Member name.
	 * @param desc
	 * 		Member desc.
	 */
	public SimpleMemberKey(InstanceJavaClass owner, String name, String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	@Override
	public InstanceJavaClass getOwner() {
		return owner;
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
		if (!(o instanceof MemberKey)) {
			return false;
		}

		MemberKey memberKey = (MemberKey) o;

		if (!owner.equals(memberKey.getOwner())) {
			return false;
		}
		if (!name.equals(memberKey.getName())) {
			return false;
		}
		return desc.equals(memberKey.getDesc());
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}
}
