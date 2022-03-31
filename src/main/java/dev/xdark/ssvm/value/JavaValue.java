package dev.xdark.ssvm.value;

/**
 * Java value wrapper.
 *
 * @param <V>
 * 		Type of Java object.
 *
 * @author xDark
 */
public interface JavaValue<V> extends InstanceValue {

	/**
	 * Returns Java value.
	 *
	 * @return Java value.
	 */
	V getValue();

	/**
	 * Sets whether this value should be wide.
	 *
	 * @param wide
	 * 		Wide flag.
	 */
	void setWide(boolean wide);
}
