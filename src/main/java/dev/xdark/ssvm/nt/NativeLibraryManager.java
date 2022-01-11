package dev.xdark.ssvm.nt;

/**
 * Manages native libraries.
 *
 * @author xDark
 */
public interface NativeLibraryManager {

	/**
	 * Maps a library name into a platform-specific
	 * string representing a native library.
	 *
	 * @param name
	 * 		Library name to map.
	 *
	 * @return mapped library name.
	 */
	String mapLibraryName(String name);
}
