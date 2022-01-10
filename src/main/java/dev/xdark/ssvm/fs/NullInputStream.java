package dev.xdark.ssvm.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Just like Linux /dev/null.
 *
 * @author xDark
 */
public final class NullInputStream extends InputStream {

	@Override
	public int read() {
		return 0;
	}

	@Override
	public int read(byte[] b) {
		return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) {
		return -1;
	}

	@Override
	public byte[] readAllBytes() {
		return new byte[0];
	}

	@Override
	public byte[] readNBytes(int len) {
		return new byte[0];
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) {
		return 0;
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return 0L;
	}
}
