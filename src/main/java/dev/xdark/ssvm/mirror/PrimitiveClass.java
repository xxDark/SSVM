package dev.xdark.ssvm.mirror;

/**
 * Java class implementation for long, int, double, float, etc.
 *
 * @author xDark
 */
public interface PrimitiveClass extends JavaClass {

	/**
	 * Fast mapping to ASM's type sorts.
	 *
	 * @return Type sort.
	 */
	int getSort();
}
