package dev.xdark.ssvm.fs;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * Basic ZIP file wrapper implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public class SimpleZipFile implements ZipFile {

	private final java.util.zip.ZipFile handle;
	private final Map<ZipEntry, byte[]> contents = new HashMap<>();
	private Map<String, ZipEntry> names;
	private List<ZipEntry> entries;

	@Override
	public boolean startsWithLOC() {
		return true;
	}

	@Override
	public ZipEntry getEntry(int index) {
		if (index < 0) return null;
		val entries = getEntries();
		if (index >= entries.size()) return null;
		return entries.get(index);
	}

	@Override
	public ZipEntry getEntry(String name) {
		Map<String, ZipEntry> names = this.names;
		if (names == null) {
			this.names = names = getEntries().stream()
					.collect(Collectors.toMap(ZipEntry::getName, Function.identity()));;
		}
		ZipEntry entry = names.get(name);
		if (entry == null) entry = names.get(name + '/');
		return entry;
	}

	@Override
	public byte[] readEntry(ZipEntry entry) throws IOException {
		val contents = this.contents;
		byte[] content = contents.get(entry);
		if (content == null) {
			try (InputStream in = handle.getInputStream(entry)) {
				if (in == null) return null;
				val buf = new byte[1024];
				val baos = new ByteArrayOutputStream();
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
	public int getTotal() {
		return handle.size();
	}

	@Override
	public Stream<ZipEntry> stream() {
		return getEntries().stream();
	}

	@Override
	public void close() throws IOException {
		handle.close();
	}

	private List<ZipEntry> getEntries() {
		List<ZipEntry> entries = this.entries;
		if (entries == null) {
			return this.entries = handle.stream().collect(Collectors.toList());
		}
		return entries;
	}
}
