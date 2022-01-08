package dev.xdark.ssvm.mirror;

import java.util.Map;

/**
 * Represents class layout.
 *
 * @author xDark
 */
public final class ClassLayout {

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
