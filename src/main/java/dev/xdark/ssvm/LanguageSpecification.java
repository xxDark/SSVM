package dev.xdark.ssvm;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Comparator;

import static org.objectweb.asm.Type.BOOLEAN;
import static org.objectweb.asm.Type.BYTE;
import static org.objectweb.asm.Type.CHAR;
import static org.objectweb.asm.Type.DOUBLE;
import static org.objectweb.asm.Type.FLOAT;
import static org.objectweb.asm.Type.INT;
import static org.objectweb.asm.Type.LONG;
import static org.objectweb.asm.Type.SHORT;

/**
 * JVM specs.
 *
 * @author xDark
 */
@UtilityClass
public class LanguageSpecification {

	public final long LONG_SIZE = Long.SIZE / Byte.SIZE;
	public final long DOUBLE_SIZE = LONG_SIZE;
	public final long INT_SIZE = Integer.SIZE / Byte.SIZE;
	public final long FLOAT_SIZE = INT_SIZE;
	public final long SHORT_SIZE = Short.SIZE / Byte.SIZE;
	public final long CHAR_SIZE = SHORT_SIZE;
	@SuppressWarnings("PointlessArithmeticExpression")
	public final long BYTE_SIZE = Byte.SIZE / Byte.SIZE;
	public final long BOOLEAN_SIZE = BYTE_SIZE;
	public final int ARRAY_DIMENSION_LIMIT = 256;

	private final Class<?>[] PRIMITIVES;
	private final long[] SIZES;

	static {
		Class<?>[] classes = {
			long.class,
			double.class,
			int.class,
			float.class,
			short.class,
			char.class,
			byte.class,
			boolean.class,
		};
		Arrays.sort(classes, Comparator.comparingInt(System::identityHashCode));
		PRIMITIVES = classes;
		long[] sizes = new long[classes.length];
		for (int i = 0; i < sizes.length; i++) {
			Class<?> c = classes[i];
			if (c == long.class || c == double.class) {
				sizes[i] = LONG_SIZE;
			} else if (c == int.class || c == float.class) {
				sizes[i] = INT_SIZE;
			} else if (c == short.class || c == char.class) {
				sizes[i] = SHORT_SIZE;
			} else if (c == byte.class || c == boolean.class) {
				sizes[i] = BYTE_SIZE;
			} else {
				throw new AssertionError(c);
			}
		}
		SIZES = sizes;
	}

	/**
	 * @param sort Primitive sort.
	 * @return Primitive size.
	 */
	public long primitiveSize(int sort) {
		switch (sort) {
			case LONG:
			case DOUBLE:
				return LONG_SIZE;
			case INT:
			case FLOAT:
				return INT_SIZE;
			case SHORT:
			case CHAR:
				return SHORT_SIZE;
			case BYTE:
			case BOOLEAN:
				return BYTE_SIZE;
			default:
				throw new IllegalArgumentException(Integer.toString(sort));
		}
	}


	/**
	 * @param type Primitive type.
	 * @return Primitive size.
	 */
	public long primitiveSize(Class<?> type) {
		if (!type.isPrimitive()) {
			throw new IllegalArgumentException(type.getName());
		}
		return SIZES[Arrays.binarySearch(PRIMITIVES, type)];
	}
}
