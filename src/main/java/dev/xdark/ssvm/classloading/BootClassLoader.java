package dev.xdark.ssvm.classloading;

/**
 * Boot class loader interface for the VM.
 *
 * @author xDark
 */
public interface BootClassLoader {

	/**
	 * Resolves boot class by it's name.
	 *
	 * @param name Name of the class.
	 * @return Parsed class or {@code null}, if not found.
	 */
	ClassParseResult findBootClass(String name);
}
