package dev.xdark.ssvm.nt;

import java.util.concurrent.ThreadLocalRandom;

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

	@Override
	public LibraryLoadResult load(String name, boolean isBuiltin) {
		return new LibraryLoadResult(ThreadLocalRandom.current().nextLong(), JniVersion.JNI_VERSION_1_8, null);
	}

	@Override
	public void unload(String name, boolean isBuiltin, long handle) {
	}

	@Override
	public long find(long handle, String symbolName) {
		return 0L;
	}

	@Override
	public String findBuiltinLibrary(String name) {
		return name;
	}
}
