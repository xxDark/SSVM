package dev.xdark.ssvm.nt;

/**
 * Native library manager that delegates all
 * operations to JVM.
 *
 * @author xDark
 */
public class HostNativeLibraryManager implements NativeLibraryManager {

	@Override
	public String mapLibraryName(String name) {
		return System.mapLibraryName(name);
	}
}
