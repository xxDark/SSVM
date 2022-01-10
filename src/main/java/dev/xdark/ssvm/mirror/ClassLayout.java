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
	private final Map<FieldInfo, Long> offsetMap;
	private final long size;

	/**
	 * @param offsetMap
	 * 		Map containing field offsets.
	 * @param size
	 * 		Total size of class layout.
	 */
	public ClassLayout(Map<FieldInfo, Long> offsetMap, long size) {
		this.offsetMap = offsetMap;
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
	public long getFieldOffset(FieldInfo info) {
		var offset = offsetMap.get(info);
		return offset == null ? -1L : offset.longValue();
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
		for (var entry : offsetMap.entrySet()) {
			var key = entry.getKey();
			if (javaClass == key.getOwner() && name.equals(key.getName())) {
				return entry.getValue();
			}
		}
		return -1L;
	}

	/**
	 * Returns a map containing field offsets.
	 *
	 * @return map containing field offsets.
	 */
	public Map<FieldInfo, Long> getOffsetMap() {
		return offsetMap;
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
