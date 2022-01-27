package dev.xdark.ssvm.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Simple implementation of file descriptor manager.
 *
 * @author xDark
 */
public class SimpleFileDescriptorManager implements FileDescriptorManager {

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

	@Override
	public <A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException {
		return null;
	}

	@Override
	public String[] list(String path) {
		return new String[0];
	}

	@Override
	public long openZipFile(String path, int mode) throws IOException {
		return 0L;
	}

	@Override
	public ZipFile getZipFile(long handle) {
		return null;
	}
}
