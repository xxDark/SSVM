package dev.xdark.ssvm.nt;

/**
 * Simple implementation for native library manager.
 *
 * @author xDark
 */
public class SimpleNativeLibraryManager implements NativeLibraryManager {

	@Override
	public String mapLibraryName(String name) {
		return System.mapLibraryName(name);
	}
}
