package dev.xdark.ssvm.mirror;

/**
 * @author xDark
 * @see ClassLayout
 */
public final class FieldInfo {

	private final JavaClass owner;
	private final String name;
	private final String desc;

	public FieldInfo(JavaClass owner, String name, String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	/**
	 * Returns owner of the field.
	 *
	 * @return field owner.
	 */
	public JavaClass getOwner() {
		return owner;
	}

	/**
	 * Returns field name.
	 *
	 * @return field name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns field desc.
	 *
	 * @return field desc.
	 */
	public String getDesc() {
		return desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FieldInfo fieldInfo = (FieldInfo) o;

		if (!name.equals(fieldInfo.name)) return false;
		return desc.equals(fieldInfo.desc);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}
}
