package dev.xdark.ssvm.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;

/**
 * Delegating implementation of the file manager to facilitate easier splitting of implementations for users.
 *
 * @author Matt Coley
 */
@SuppressWarnings("unused")
public class DelegatingFileManager implements FileManager {
	private final FileManager delegate;

	public DelegatingFileManager(FileManager delegate) {
		if (delegate == null)
			throw new IllegalArgumentException("Delegate cannot be null");
		this.delegate = delegate;
	}

	@Override
	public InputStream getFdIn(long handle) {
		return delegate.getFdIn(handle);
	}

	@Override
	public OutputStream getFdOut(long handle) {
		return delegate.getFdOut(handle);
	}

	@Override
	public long getRealHandle(long handle) {
		return delegate.getRealHandle(handle);
	}

	@Override
	public boolean close(long handle) throws IOException {
		return delegate.close(handle);
	}

	@Override
	public long newFD() {
		return delegate.newFD();
	}

	@Override
	public long newFD(int stream) {
		return delegate.newFD(stream);
	}

	@Override
	public boolean isAppend(int stream) {
		return delegate.isAppend(stream);
	}

	@Override
	public String canonicalize(String path) throws IOException {
		return delegate.canonicalize(path);
	}

	@Override
	public long open(String path, int mode) throws IOException {
		return delegate.open(path, mode);
	}

	@Override
	public boolean rename(String oldPath, String newPath) {
		return delegate.rename(oldPath, newPath);
	}

	@Override
	public boolean delete(String path) {
		return delegate.delete(path);
	}

	@Override
	public boolean checkAccess(String path, int access) {
		return delegate.checkAccess(path, access);
	}

	@Override
	public boolean setPermission(String path, int flag, boolean value, boolean ownerOnly) {
		return delegate.setPermission(path, flag, value, ownerOnly);
	}

	@Override
	public boolean setLastModifiedTime(String path, long time) {
		return delegate.setLastModifiedTime(path, time);
	}

	@Override
	public boolean setReadOnly(String path) {
		return delegate.setReadOnly(path);
	}

	@Override
	public long getSpace(String path, int id) {
		return delegate.getSpace(path, id);
	}

	@Override
	public boolean createFileExclusively(String path) throws IOException {
		return delegate.createFileExclusively(path);
	}

	@Override
	public <A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException {
		return delegate.getAttributes(path, attrType, options);
	}

	@Override
	public String[] list(String path) {
		return delegate.list(path);
	}

	@Override
	public long openZipFile(String path, int mode) throws IOException {
		return delegate.openZipFile(path, mode);
	}

	@Override
	public ZipFile getZipFile(long handle) {
		return delegate.getZipFile(handle);
	}

	@Override
	public ZipEntry getZipEntry(long handle) {
		return delegate.getZipEntry(handle);
	}

	@Override
	public boolean freeZipEntry(long handle) {
		return delegate.freeZipEntry(handle);
	}

	@Override
	public String getCurrentWorkingDirectory() {
		return delegate.getCurrentWorkingDirectory();
	}

	@Override
	public OutputStream getStreamOut(int stream) {
		return delegate.getStreamOut(stream);
	}
}
