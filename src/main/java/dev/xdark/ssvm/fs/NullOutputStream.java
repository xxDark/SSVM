package dev.xdark.ssvm.fs;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Just like Linux /dev/null.
 *
 * @author xDark
 */
public final class NullOutputStream extends OutputStream {

	@Override
	public void write(int b) {
	}

	@Override
	public void write(byte[] b) {
	}

	@Override
	public void write(byte[] b, int off, int len) {
	}
}
