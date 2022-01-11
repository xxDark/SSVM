package dev.xdark.ssvm.mirror;

import java.util.Collections;
import java.util.Map;

/**
 * Represents class layout.
 *
 * @author xDark
 */
public final class ClassLayout {

	public static final ClassLayout EMPTY = new ClassLayout(Collections.emptyMap(), 0L);
	private final Map<MemberKey, JavaField> fields;
	private final long size;

	/**
	 * @param fields
	 * 		Map containing field info.
	 * @param size
	 * 		Total size of class layout.
	 */
	public ClassLayout(Map<MemberKey, JavaField> fields, long size) {
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
		var field = fields.get(info);
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
		for (var entry : fields.entrySet()) {
			var key = entry.getKey();
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
	public Map<MemberKey, JavaField> getFieldMap() {
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
