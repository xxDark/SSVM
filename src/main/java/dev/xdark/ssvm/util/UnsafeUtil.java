package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe utilities.
 *
 * @author xDark
 */
@UtilityClass
public class UnsafeUtil {

	private final Unsafe UNSAFE;
	private final int ADDRESS_SIZE = Unsafe.ADDRESS_SIZE >> 2;

	/**
	 * @return unsafe instance.
	 */
	public Unsafe get() {
		return UNSAFE;
	}

	/**
	 * Returns size of the type.
	 *
	 * @param desc
	 * 		Type to get size from.
	 *
	 * @return Size of the type.
	 */
	@SuppressWarnings("DuplicateBranchesInSwitch")
	public long getSizeFor(String desc) {
		switch(desc) {
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
	public long addressOf(Object value) {
		Object[] helper = new Object[]{value};
		switch(ADDRESS_SIZE) {
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
	public Object byAddress(long address) {
		Object[] helper = new Object[]{null};
		switch(ADDRESS_SIZE) {
			case 1:
				UNSAFE.putInt(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET, (int) address);
				break;
			case 2:
				UNSAFE.putLong(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET, address);
				break;
			default:
				throw new RuntimeException("Unsupported address size: " + Unsafe.ADDRESS_SIZE);
		}
		return UNSAFE.getObject(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET);
	}

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			UNSAFE = (Unsafe) field.get(null);
		} catch(NoSuchFieldException | IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}