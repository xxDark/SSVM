package dev.xdark.ssvm.fs;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple implementation of file descriptor manager.
 *
 * @author xDark
 */
public final class SimpleFileDescriptorManager implements FileDescriptorManager {

	private final InputStream in = new NullInputStream();
	private final OutputStream out = new NullOutputStream();

	@Override
	public InputStream getFdIn(long handle) {
		return in;
	}

	@Override
	public OutputStream getFdOut(long handle) {
		return out;
	}

	@Override
	public void close(long handle) {
	}

	@Override
	public long newFD() {
		return 0L;
	}

	@Override
	public long newFD(int stream) {
		return 0L;
	}

	@Override
	public boolean isAppend(int stream) {
		return false;
	}

	@Override
	public String canonicalize(String path) {
		return path;
	}

	@Override
	public long open(String path, int mode) {
		return 0L;
	}
}
