package dev.xdark.ssvm.fs;

import java.io.IOException;
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

	private static final class NullInputStream extends InputStream {

		NullInputStream() {
		}

		@Override
		public int read() throws IOException {
			return -1;
		}
	}

	private static final class NullOutputStream extends OutputStream {

		NullOutputStream() {
		}

		@Override
		public void write(int b) throws IOException {
		}

		@Override
		public void write(byte[] b) throws IOException {
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
		}
	}
}
