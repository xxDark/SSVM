package dev.xdark.ssvm.fs;

import lombok.val;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * File descriptor manager that uses host machine
 * file system.
 *
 * @author xDark
 */
public class HostFileDescriptorManager implements FileDescriptorManager {

	protected final Map<Long, InputStream> inputs = new HashMap<>();
	protected final Map<Long, OutputStream> outputs = new HashMap<>();
	private final Map<Long, ZipFile> zipFiles = new HashMap<>();

	private final InputStream stdin;
	private final OutputStream stdout;
	private final OutputStream stderr;

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
	public InputStream getFdIn(long handle) {
		return inputs.get(handle);
	}

	@Override
	public OutputStream getFdOut(long handle) {
		return outputs.get(handle);
	}

	@Override
	public void close(long handle) throws IOException {
		val wrapper = (Long) handle;
		val in = inputs.remove(wrapper);
		if (in != null) {
			in.close();
			return;
		}
		val out = outputs.remove(wrapper);
		if (out != null) {
			out.close();
			return;
		}
		val zip = zipFiles.remove(wrapper);
		if (zip != null) {
			zip.close();
		}
	}

	@Override
	public long newFD() {
		val rng = ThreadLocalRandom.current();
		val inputs = this.inputs;
		val outputs = this.outputs;
		val zipFiles = this.zipFiles;
		long handle;
		Long wrapper;
		do {
			handle = rng.nextLong();
		} while (inputs.containsKey(wrapper = handle) || outputs.containsKey(wrapper) || zipFiles.containsKey(wrapper));
		return handle;
	}

	@Override
	public long newFD(int stream) {
		switch (stream) {
			case 0: {
				val handle = newFD();
				inputs.put(handle, stdin);
				return handle;
			}
			case 1: {
				val handle = newFD();
				outputs.put(handle, stdout);
				return handle;
			}
			case 2: {
				val handle = newFD();
				outputs.put(handle, stderr);
				return handle;
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
	public long open(String path, int mode) throws IOException {
		switch (mode) {
			case READ: {
				val fd = newFD();
				val in = new FileInputStream(path);
				inputs.put(fd, in);
				return fd;
			}
			case WRITE: {
				val fd = newFD();
				val out = new FileOutputStream(path);
				outputs.put(fd, out);
				return fd;
			}
			case APPEND: {
				val fd = newFD();
				val out = new FileOutputStream(path, true);
				outputs.put(fd, out);
				return fd;
			}
			default:
				throw new IOException("Unknown mode: " + mode);
		}
	}

	@Override
	public <A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException {
		val p = Paths.get(path);
		if (!p.toFile().exists()) return null;
		return Files.readAttributes(p, attrType, options);
	}

	@Override
	public String[] list(String path) {
		return new File(path).list();
	}

	@Override
	public long openZipFile(String path, int mode) throws IOException {
		val fd = newFD();
		val zf = new SimpleZipFile(new java.util.zip.ZipFile(new File(path), mode));
		zipFiles.put(fd, zf);
		return fd;
	}

	@Override
	public ZipFile getZipFile(long handle) {
		return zipFiles.get(handle);
	}
}
