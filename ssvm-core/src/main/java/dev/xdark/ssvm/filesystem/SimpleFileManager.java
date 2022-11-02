package dev.xdark.ssvm.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;

/**
 * Simple implementation of file descriptor manager.
 *
 * @author xDark
 */
public class SimpleFileManager implements FileManager {

	@Override
	public InputStream getFdIn(long handle) {
		return null;
	}

	@Override
	public OutputStream getFdOut(long handle) {
		return null;
	}

	@Override
	public long getRealHandle(long handle) {
		return 0;
	}

	@Override
	public boolean close(long handle) {
		return false;
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

	@Override
	public ZipEntry getZipEntry(long handle) {
		return null;
	}

	@Override
	public boolean freeZipEntry(long handle) {
		return false;
	}

	@Override
	public String getCurrentWorkingDirectory() {
		return new File("").getAbsolutePath();
	}

	@Override
	public OutputStream getStreamOut(int stream) {
		return null;
	}
}
