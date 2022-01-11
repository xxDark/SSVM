package dev.xdark.ssvm.mirror;

/**
 * Class member info.
 *
 * @author xDark
 */
public final class MemberKey {

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
	public MemberKey(InstanceJavaClass owner, String name, String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	/**
	 * Returns member owner.
	 *
	 * @return member owner.
	 */
	public InstanceJavaClass getOwner() {
		return owner;
	}
	/**
	 * Returns member name.
	 *
	 * @return member name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns member desc.
	 *
	 * @return member desc.
	 */
	public String getDesc() {
		return desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MemberKey memberKey = (MemberKey) o;

		if (!owner.equals(memberKey.owner)) return false;
		if (!name.equals(memberKey.name)) return false;
		return desc.equals(memberKey.desc);
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}
}
