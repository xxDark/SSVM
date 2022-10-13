package dev.xdark.ssvm.classloading;

import lombok.experimental.UtilityClass;

/**
 * Default implementation of boot class loader that
 * pulls classes from native boot class loader.
 *
 * @author xDark
 */
@UtilityClass
public class RuntimeBootClassFinder {

	private final ClassLoader DELEGATE = new BootClassLoaderDelegate();

	/**
	 * Returns boot class loader that
	 * pulls classes from native boot class loader.
	 *
	 * @return boot class loader.
	 */
	public static BootClassFinder create() {
		return new DelegatingBootClassFinder(DELEGATE);
	}

	private static final class BootClassLoaderDelegate extends ClassLoader {

		BootClassLoaderDelegate() {
			super(null);
		}
	}
}
