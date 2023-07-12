package dev.xdark.ssvm.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Native;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;

/**
 * Maps VM file descriptors to native descriptors.
 *
 * @author xDark
 */
public interface FileManager {

	int READ = 0;
	int WRITE = 1;
	int APPEND = 2;
	int ACCESS_READ    = 0x04;
	int ACCESS_WRITE   = 0x02;
	int ACCESS_EXECUTE = 0x01;
	int SPACE_TOTAL  = 0;
	int SPACE_FREE   = 1;
	int SPACE_USABLE = 2;

	/**
	 * Maps VM file descriptor to {@link InputStream}.
	 *
	 * @param handle File descriptor to map.
	 * @return mapped stream.
	 */
	InputStream getFdIn(long handle);

	/**
	 * Maps VM file descriptor to {@link OutputStream}.
	 *
	 * @param handle File descriptor to map.
	 * @return mapped stream.
	 */
	OutputStream getFdOut(long handle);

	/**
	 * Reveals the real file descriptor handle.
	 *
	 * @param handle
	 *      VM file descriptor handle.
	 * @return Host file descriptor handle or {@code -1L}.
	 */
	long getRealHandle(long handle);

	/**
	 * Called when VM closes file descriptor.
	 *
	 * @param handle VM descriptor handle.
	 * @return {@code true} if handle was closed.
	 * @throws IOException If any I/O error occurs.
	 */
	boolean close(long handle) throws IOException;

	/**
	 * Creates new VM file descriptor handle.
	 *
	 * @return file descriptor handle.
	 */
	long newFD();

	/**
	 * Returns new VM file descriptor for standard stream.
	 *
	 * @param stream Standard stream id.
	 * @return file descriptor handle.
	 */
	long newFD(int stream);

	/**
	 * Returns whether standard stream was opened for appending.
	 *
	 * @param stream Standard stream id.
	 * @return {@code true} if standard stream was opened for appending,
	 * {@code false} otherwise.
	 */
	boolean isAppend(int stream);

	/**
	 * Canonicalizes file path.
	 *
	 * @param path Path to canonicalize.
	 * @return canonicalized path.
	 * @throws IOException If any I/O error occurs.
	 */
	String canonicalize(String path) throws IOException;

	/**
	 * Opens file with the specific mode.
	 *
	 * @param path Path to open
	 * @param mode Open mode.
	 * @return file handle.
	 * @throws IOException If any I/O error occurs.
	 * @see FileManager#READ
	 * @see FileManager#WRITE
	 * @see FileManager#APPEND
	 */
	long open(String path, int mode) throws IOException;

	// File ops

	/**
	 * Renames a file path to another path.
	 *
	 * @param oldPath Old path.
	 * @param newPath New path.
	 * @return {@code true} if file was renamed, {@code false} otherwise.
	 */
	boolean rename(String oldPath, String newPath);

	/**
	 * Deletes a file path.
	 *
	 * @param path Path to delete.
	 * @return {@code true} if file was deleted, {@code false} otherwise.
	 */
	boolean delete(String path);

	/**
	 * Returns if the path has specific access.
	 * @param path Path to check.
	 * @param access Access to check.
	 * @return {@code true} if path has access, {@code false} otherwise.
	 * @see FileManager#ACCESS_READ
	 * @see FileManager#ACCESS_WRITE
	 * @see FileManager#ACCESS_EXECUTE
	 */
	boolean checkAccess(String path, int access);

	/**
	 * Sets a file permission on the path.
	 *
	 * @param path Path to set permission.
	 * @param flag Permission flag.
	 * @param value Permission value.
	 * @param ownerOnly If permission should be set only for owner.
	 * @return {@code true} if permission was set, {@code false} otherwise.
	 * @see FileManager#ACCESS_READ
	 * @see FileManager#ACCESS_WRITE
	 * @see FileManager#ACCESS_EXECUTE
	 */
	boolean setPermission(String path, int flag, boolean value, boolean ownerOnly);

	/**
	 * Sets the last modified time of the file.
	 *
	 * @param path Path to set time.
	 * @param time Time to set.
	 * @return {@code true} if time was set, {@code false} otherwise.
	 */
	boolean setLastModifiedTime(String path, long time);

	/**
	 * Sets the file to be read-only.
	 *
	 * @param path Path to set.
	 * @return {@code true} if file was set to read-only, {@code false} otherwise.
	 */
	boolean setReadOnly(String path);

	/**
	 * Get the space that a file occupies.
	 * @param path Path to get space.
	 * @param id Space id.
	 * @return Space that a file occupies.
	 * @see FileManager#SPACE_TOTAL
	 * @see FileManager#SPACE_FREE
	 * @see FileManager#SPACE_USABLE
	 */
	long getSpace(String path, int id);

	/**
	 * Create a file exclusively
	 *
	 * @param path Path to create.
	 * @return {@code true} if file was created, {@code false} otherwise.
	 * @throws IOException if the path is invalid.
	 */
	boolean createFileExclusively(String path) throws IOException;

	/**
	 * Returns attributes of a file.
	 *
	 * @param path     Path to file.
	 * @param attrType Attributes class type.
	 * @param options  Options indicating how symbolic links are handled.
	 * @return file attributes or {@code null},
	 * if path does not exist.
	 * @throws IOException If any I/O error occurs.
	 */
	<A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException;

	/**
	 * List the elements of the directory denoted
	 * by the given abstract pathname.
	 *
	 * @param path Path to list.
	 * @return list of the elements of the directory.
	 */
	String[] list(String path);

	/**
	 * Opens new zip file.
	 *
	 * @param path Path to the zip file.
	 * @param mode The mode in which the file is to be opened.
	 * @return opened zip file handle.
	 * @throws IOException If any I/O error occurs.
	 */
	long openZipFile(String path, int mode) throws IOException;

	/**
	 * Transfers a handle from {@link #open(String, int)} to being a treated as if it were opened with {@link #openZipFile(String, int)}.
	 * @param handle Handle to migrate.
	 * @param mode The mode in which the file is to be opened.
	 * @throws IOException If any I/O error occurs.
	 */
	void transferInputToZip(long handle, int mode) throws IOException;

	/**
	 * @param handle Zip file handle.
	 * @return zip file by it's handle or {@code null},
	 * if not found.
	 */
	ZipFile getZipFile(long handle);

	/**
	 * @param handle Zip entry handle.
	 * @return zip entry by it's handle or {@code null},
	 * if not found.
	 */
	ZipEntry getZipEntry(long handle);

	/**
	 * Deallocates zip entry handle.
	 *
	 * @param handle Zip entry handle to deallocate.
	 * @return {@code true} if entry was deallocated.
	 */
	boolean freeZipEntry(long handle);

	/**
	 * @return current working directory.
	 */
	String getCurrentWorkingDirectory();

	/**
	 * Maps VM file descriptor to {@link OutputStream}
	 * to standard error/output stream.
	 *
	 * @param stream Standard stream id.
	 * @return mapped stream.
	 */
	OutputStream getStreamOut(int stream);
}
