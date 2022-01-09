package dev.xdark.ssvm.util;

import sun.misc.Unsafe;

/**
 * Unsafe utilities.
 *
 * @author xDark
 */
public final class UnsafeUtil {

	private static final Unsafe UNSAFE;

	private UnsafeUtil() {
	}

	/**
	 * Returns page size.
	 *
	 * @return page size.
	 */
	public static int getPageSize() {
		return UNSAFE.pageSize();
	}

	/**
	 * Returns size of the type.
	 * @param desc
	 * 		Type to get size from.
	 * @return
	 * 		Size of the type.
	 */
	@SuppressWarnings("DuplicateBranchesInSwitch")
	public static long getSizeFor(String desc) {
		switch (desc) {
			case "J":
			case "D":
				return 8L;
			case "I":
			case "F":
				return 4L;
			case "C":
			case "S":
				return 2L;
			case "B":
			case "Z":
				return 1L;
			default:
				return 8L;
		}
	}

	/**
	 * Returns address of an object.
	 *
	 * @param value
	 * 		Object to get address from.
	 *
	 * @return address of an object.
	 */
	public static long addressOf(Object value) {
		var helper = new Object[]{value};
		switch (Unsafe.ADDRESS_SIZE >> 2) {
			case 1:
				return UNSAFE.getInt(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET);
			case 2:
				return UNSAFE.getLong(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET);
			default:
				throw new RuntimeException("Unsupported address size: " + Unsafe.ADDRESS_SIZE);
		}
	}

	/**
	 * Gets object by it's address.
	 *
	 * @param address
	 * 		Address of an object.
	 *
	 * @return object at the specific address.
	 */
	public static Object byAddress(long address) {
		var helper = new Object[]{null};
		switch (Unsafe.ADDRESS_SIZE >> 2) {
			case 1:
				UNSAFE.putLong(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET, address);
				break;
			case 2:
				UNSAFE.putInt(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET, (int) address);
				break;
			default:
				throw new RuntimeException("Unsupported address size: " + Unsafe.ADDRESS_SIZE);
		}
		return UNSAFE.getObject(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET);
	}

	static {
		try {
			var field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			UNSAFE = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
