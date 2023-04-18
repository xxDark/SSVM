package dev.xdark.ssvm.fs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * Basic zip file implementation.
 *
 * @author xDark
 */
public abstract class BasicZipFile implements ZipFile {

	private final int rawHandle;
	private final Map<ZipEntry, byte[]> contents = new HashMap<>();
	private final Map<Handle, ZipEntry> handles = new HashMap<>();
	private Map<String, ZipEntry> names;

	/**
	 * @param rawHandle Raw zip handle.
	 */
	protected BasicZipFile(int rawHandle) {
		this.rawHandle = rawHandle;
	}

	@Override
	public boolean startsWithLOC() {
		return true;
	}

	@Override
	public synchronized ZipEntry getEntry(int index) {
		if (index < 0) {
			return null;
		}
		List<ZipEntry> entries = getEntries();
		if (index >= entries.size()) {
			return null;
		}
		return entries.get(index);
	}

	@Override
	public synchronized ZipEntry getEntry(String name) {
		Map<String, ZipEntry> names = this.names;
		if (names == null) {
			names = new HashMap<>();
			for (ZipEntry entry : getEntries()) {
				names.putIfAbsent(entry.getName(), entry);
			}
			this.names = names;
		}
		return names.get(name);
	}

	@Override
	public synchronized byte[] readEntry(ZipEntry entry) throws IOException {
		Map<ZipEntry, byte[]> contents = this.contents;
		byte[] content = contents.get(entry);
		if (content == null) {
			try (InputStream in = openStream(entry)) {
				if (in == null) {
					return null;
				}
				byte[] buf = new byte[1024];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int r;
				while ((r = in.read(buf)) >= 0) {
					baos.write(buf, 0, r);
				}
				contents.put(entry, content = baos.toByteArray());
			}
		}
		return content;
	}

	@Override
	public Stream<ZipEntry> stream() {
		return getEntries().stream();
	}

	@Override
	public synchronized long makeHandle(ZipEntry entry) {
		Map<Handle, ZipEntry> handles = this.handles;
		Handle handle = Handle.threadLocal();
		ThreadLocalRandom r = ThreadLocalRandom.current();
		int value;
		do {
			value = r.nextInt();
			handle.set(value);
		} while (handles.containsKey(handle));
		handles.put(handle.copy(), entry);
		return (long) value << 32L | rawHandle & 0xffffffffL;
	}

	@Override
	public synchronized ZipEntry getEntry(long handle) {
		handle >>= 32;
		return handles.get(Handle.threadLocal((int) handle));
	}

	@Override
	public synchronized boolean freeHandle(long handle) {
		handle >>= 32;
		return handles.remove(Handle.threadLocal((int) handle)) != null;
	}

	@Override
	public void close() throws IOException {
		handles.clear();
		contents.clear();
	}

	@Override
	public abstract int getTotal();

	/**
	 * @return Listo of zip entries.
	 */
	protected abstract List<ZipEntry> getEntries();

	/**
	 * @param entry Entry to get stream for.
	 * @return Opened stream.
	 * @throws IOException If any I/O error occurs.
	 */
	protected abstract InputStream openStream(ZipEntry entry) throws IOException;
}
