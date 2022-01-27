package dev.xdark.ssvm.fs;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * ZIP file wrapper.
 *
 * @author xDark
 */
public interface ZipFile extends Closeable {

	/**
	 * @return {@code true} if this zip file
	 * starts with LDC header, {@code false}
	 * otherwise.
	 */
	boolean startsWithLOC();

	/**
	 * @param index
	 * 		Position of the entry.
	 *
	 * @return zip entry or {@code null},
	 * if not found.
	 */
	ZipEntry getEntry(int index);

	/**
	 * @param name
	 * 		Entry name.
	 *
	 * @return zip entry or {@code null},
	 * if not found.
	 */
	ZipEntry getEntry(String name);

	/**
	 * @param entry
	 * 		Entry to read bytes from.
	 *
	 * @return entry content.
	 *
	 * @throws IOException
	 * 		If any I/O error occurs.
	 */
	byte[] readEntry(ZipEntry entry) throws IOException;

	/**
	 * @return total amount of entries.
	 */
	int getTotal();

	/**
	 * @return an ordered {@link Stream} of entries in this ZIP file.
	 */
	Stream<ZipEntry> stream();
}
