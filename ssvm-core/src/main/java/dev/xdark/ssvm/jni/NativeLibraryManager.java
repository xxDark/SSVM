package dev.xdark.ssvm.jni;

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
	 * @param name Library name to map.
	 * @return mapped library name.
	 */
	String mapLibraryName(String name);

	/**
	 * Loads native library.
	 *
	 * @param name      Library name.
	 * @param isBuiltin Whether the library is builtin.
	 * @return native library handle or {@code 0L}, if failed.
	 */
	LibraryLoadResult load(String name, boolean isBuiltin);

	/**
	 * Unloads native library.
	 *
	 * @param name      Library name.
	 * @param isBuiltin Whether the library is builtin.
	 * @param handle    Library handle.
	 */
	void unload(String name, boolean isBuiltin, long handle);

	/**
	 * Lookup symbol address in native library.
	 *
	 * @param handle     Library handle.
	 * @param symbolName The symbol name to lookup.
	 * @return symbol address or {@code 0L}, if failed.
	 */
	long find(long handle, String symbolName);

	/**
	 * Returns a path to a static library.
	 *
	 * @param name Library name.
	 * @return path to a library or {@code nul}, if not found.
	 */
	String findBuiltinLibrary(String name);
}
