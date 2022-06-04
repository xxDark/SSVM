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
	private final int ADDRESS_SIZE;
	private final int ARRAY_OBJECT_BASE_OFFSET;

	/**
	 * @return unsafe instance.
	 */
	public Unsafe get() {
		return UNSAFE;
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
				return UNSAFE.getInt(helper, ARRAY_OBJECT_BASE_OFFSET);
			case 2:
				return UNSAFE.getLong(helper, ARRAY_OBJECT_BASE_OFFSET);
			default:
				throw new RuntimeException("Unsupported address size: " + ADDRESS_SIZE);
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
				throw new RuntimeException("Unsupported address size: " + ADDRESS_SIZE);
		}
		return UNSAFE.getObject(helper, Unsafe.ARRAY_OBJECT_BASE_OFFSET);
	}

	static {
		try {
			Unsafe unsafe = null;
			for (Field field : Unsafe.class.getDeclaredFields()) {
				if (Unsafe.class == field.getType()) {
					field.setAccessible(true);
					unsafe = (Unsafe) field.get(null);
					break;
				}
			}
			if (unsafe == null) {
				throw new IllegalStateException("Unable to locate unsafe instance");
			}
			ADDRESS_SIZE = unsafe.addressSize() >> 2;;
			ARRAY_OBJECT_BASE_OFFSET = unsafe.arrayBaseOffset(Object[].class);
			UNSAFE = unsafe;
		} catch(IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
