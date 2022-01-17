package dev.xdark.ssvm.mirror;

import lombok.val;

import java.util.Collections;
import java.util.Map;

/**
 * Represents field class layout.
 *
 * @author xDark
 */
public final class FieldLayout {

	public static final FieldLayout EMPTY = new FieldLayout(Collections.emptyMap(), 0L);
	private final Map<MemberKey, JavaField> fields;
	private final long size;

	/**
	 * @param fields
	 * 		Map containing field info.
	 * @param size
	 * 		Total size of class layout.
	 */
	public FieldLayout(Map<MemberKey, JavaField> fields, long size) {
		this.fields = fields;
		this.size = size;
	}

	/**
	 * Returns field offset.
	 *
	 * @param info
	 * 		Field information.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffset(MemberKey info) {
		val field = fields.get(info);
		return field == null ? -1L : field.getOffset();
	}

	/**
	 * Returns field offset.
	 *
	 * @param javaClass
	 * 		Class defining the field
	 * @param name
	 * 		Field name.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffset(JavaClass javaClass, String name) {
		for (val entry : fields.entrySet()) {
			val key = entry.getKey();
			if (javaClass == key.getOwner() && name.equals(key.getName())) {
				return entry.getValue().getOffset();
			}
		}
		return -1L;
	}

	/**
	 * Returns a map containing fields info.
	 *
	 * @return map containing fields info.
	 */
	public Map<MemberKey, JavaField> getFields() {
		return fields;
	}

	/**
	 * Returns total size of the layout.
	 *
	 * @return total size of the layout.
	 */
	public long getSize() {
		return size;
	}
}
