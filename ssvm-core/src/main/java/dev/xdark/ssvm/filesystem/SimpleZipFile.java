package dev.xdark.ssvm.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Basic ZIP file wrapper implementation.
 *
 * @author xDark
 */
public class SimpleZipFile extends BasicZipFile {

	private final ZipFile handle;
	private List<ZipEntry> entries;

	/**
	 * @param rawHandle Raw zip handle.
	 * @param handle    Zip file.
	 */
	public SimpleZipFile(int rawHandle, ZipFile handle) {
		super(rawHandle);
		this.handle = handle;
	}

	@Override
	public int getTotal() {
		return handle.size();
	}

	@Override
	public void close() throws IOException {
		super.close();
		entries = null;
		handle.close();
	}

	@Override
	protected synchronized List<ZipEntry> getEntries() {
		List<ZipEntry> entries = this.entries;
		if (entries == null) {
			return this.entries = handle.stream().collect(Collectors.toList());
		}
		return entries;
	}

	@Override
	protected InputStream openStream(ZipEntry entry) throws IOException {
		return handle.getInputStream(entry);
	}
}
