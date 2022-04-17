package dev.xdark.ssvm.nt;

import java.util.concurrent.ThreadLocalRandom;

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

	@Override
	public long load(String name, boolean isBuiltin) {
		System.load(name);
		return ThreadLocalRandom.current().nextLong();
	}

	@Override
	public void unload(String name, boolean isBuiltin, long handle) {
	}

	@Override
	public long find(long handle, String symbolName) {
		return 0L;
	}

	@Override
	public int getJniVersion() {
		return JniVersion.JNI_VERSION_1_8; 
	}
}
