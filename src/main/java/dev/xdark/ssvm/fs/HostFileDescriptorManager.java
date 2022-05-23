package dev.xdark.ssvm.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;

/**
 * File descriptor manager that uses host machine
 * file system.
 *
 * @author xDark
 */
public class HostFileDescriptorManager implements FileDescriptorManager {

	protected final Map<Handle, InputStream> inputs = new HashMap<>();
	protected final Map<Handle, OutputStream> outputs = new HashMap<>();
	protected final Map<Handle, ZipFile> zipFiles = new HashMap<>();

	protected final InputStream stdin;
	protected final OutputStream stdout;
	protected final OutputStream stderr;

	/**
	 * @param stdin
	 * 		System input stream.
	 * @param stdout
	 * 		System output stream.
	 * @param stderr
	 * 		System error stream.
	 */
	public HostFileDescriptorManager(InputStream stdin, OutputStream stdout, OutputStream stderr) {
		this.stdin = stdin;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public HostFileDescriptorManager() {
		this(System.in, System.out, System.err);
	}

	@Override
	public synchronized InputStream getFdIn(long handle) {
		return inputs.get(Handle.threadLocal(handle));
	}

	@Override
	public synchronized OutputStream getFdOut(long handle) {
		return outputs.get(Handle.threadLocal(handle));
	}

	@Override
	public synchronized boolean close(long handle) throws IOException {
		Handle h = Handle.threadLocal(handle);
		InputStream in = inputs.remove(h);
		if (in != null) {
			in.close();
			return true;
		}
		OutputStream out = outputs.remove(h);
		if (out != null) {
			out.close();
			return true;
		}
		ZipFile zip = zipFiles.remove(h);
		if (zip != null) {
			zip.close();
			return true;
		}
		return false;
	}

	@Override
	public synchronized long newFD() {
		return newFD0(-1L);
	}

	@Override
	public synchronized long newFD(int stream) {
		switch(stream) {
			case 0: {
				long fd = newFD();
				inputs.put(Handle.of(fd), stdin);
				return fd;
			}
			case 1: {
				long fd = newFD();
				outputs.put(Handle.of(fd), stdout);
				return fd;
			}
			case 2: {
				long fd = newFD();
				outputs.put(Handle.of(fd), stderr);
				return fd;
			}
			default:
				throw new IllegalStateException("Unsupported stream: " + stream);
		}
	}

	@Override
	public boolean isAppend(int stream) {
		return false;
	}

	@Override
	public String canonicalize(String path) throws IOException {
		return path;
	}

	@Override
	public synchronized long open(String path, int mode) throws IOException {
		switch(mode) {
			case READ: {
				long fd = newFD();
				FileInputStream in = new FileInputStream(path);
				inputs.put(Handle.of(fd), in);
				return fd;
			}
			case WRITE: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path);
				outputs.put(Handle.of(fd), out);
				return fd;
			}
			case APPEND: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path, true);
				outputs.put(Handle.of(fd), out);
				return fd;
			}
			default:
				throw new IOException("Unknown mode: " + mode);
		}
	}

	@Override
	public <A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException {
		Path p = Paths.get(path);
		if (!p.toFile().exists()) {
			return null;
		}
		return Files.readAttributes(p, attrType, options);
	}

	@Override
	public String[] list(String path) {
		return new File(path).list();
	}

	@Override
	public synchronized long openZipFile(String path, int mode) throws IOException {
		// Only use 32 bits for zip handles,
		// see SimpleZipFile
		int fd = (int) newFD0(0xffffffff);
		ZipFile zf = new SimpleZipFile(new java.util.zip.ZipFile(new File(path), mode), fd);
		zipFiles.put(Handle.of(fd), zf);
		return fd;
	}

	@Override
	public synchronized ZipFile getZipFile(long handle) {
		return zipFiles.get(Handle.threadLocal((int) handle));
	}

	@Override
	public synchronized ZipEntry getZipEntry(long handle) {
		ZipFile zf = zipFiles.get(Handle.threadLocal((int) handle));
		return zf == null ? null : zf.getEntry(handle);
	}

	@Override
	public synchronized boolean freeZipEntry(long handle) {
		ZipFile zf = zipFiles.get(Handle.threadLocal((int) handle));
		return zf != null && zf.freeHandle(handle);
	}

	@Override
	public String getCurrentWorkingDirectory() {
		return new File("").getAbsolutePath();
	}

	@Override
	public OutputStream getStreamOut(int stream) {
		switch(stream) {
			case 1:
				return stdout;
			case 2:
				return stderr;
			default:
				throw new IllegalStateException("Unsupported stream: " + stream);
		}
	}

	private long newFD0(long mask) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		Map<Handle, InputStream> inputs = this.inputs;
		Map<Handle, OutputStream> outputs = this.outputs;
		Map<Handle, ZipFile> zipFiles = this.zipFiles;
		long raw;
		Handle h = Handle.threadLocal();
		do {
			raw = rng.nextLong() & mask;
			h.set(raw);
		} while(raw == 0L || inputs.containsKey(h) || outputs.containsKey(h) || zipFiles.containsKey(h));
		return raw;
	}
}
