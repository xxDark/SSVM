package dev.xdark.ssvm.classloading;

/**
 * Responsible for transforming user input to class nodes.
 *
 * @author xDark
 */
public interface ClassDefiner {

	/**
	 * Converts an array of bytes into an instance of {@link ClassParseResult}.
	 *
	 * @param name
	 * 		Name of the class.
	 * @param classBytes
	 * 		Class bytes.
	 * @param off
	 * 		Class bytes offset.
	 * @param len
	 * 		Class bytes length.
	 * @param source
	 * 		Source of class parsing.
	 *
	 * @return parsed class or {@code null}, if failed.
	 */
	ClassParseResult parseClass(String name, byte[] classBytes, int off, int len, String source);
}
