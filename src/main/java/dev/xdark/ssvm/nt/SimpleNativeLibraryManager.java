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
	public long load(String name, boolean isBuiltin) {
		return ThreadLocalRandom.current().nextLong();
	}

	@Override
	public void unload(String name, boolean isBuiltin, long handle) {
	}

	@Override
	public int getJniVersion() {
		return JniVersion.JNI_VERSION_1_8;
	}
}
