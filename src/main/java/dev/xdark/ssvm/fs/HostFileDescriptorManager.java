package dev.xdark.ssvm.fs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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
public final class HostFileDescriptorManager implements FileDescriptorManager {

	private final Map<Long, InputStream> inputs = new HashMap<>();
	private final Map<Long, OutputStream> outputs = new HashMap<>();

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
		var in = inputs.remove(handle);
		if (in != null) {
			in.close();
		} else {
			var out = outputs.remove(handle);
			if (out != null) out.close();
		}
	}

	@Override
	public long newFD() {
		var rng = ThreadLocalRandom.current();
		var inputs = this.inputs;
		var outputs = this.outputs;
		long handle;
		Long wrapper;
		do {
			handle = rng.nextLong() & 0xFFFFFFFFL;
		} while (inputs.containsKey(wrapper = handle) || outputs.containsKey(wrapper));
		return handle;
	}

	@Override
	public long newFD(int stream) {
		switch (stream) {
			case 0: {
				var handle = newFD();
				inputs.put(handle, stdin);
				return handle;
			}
			case 1: {
				var handle = newFD();
				outputs.put(handle, stdout);
				return handle;
			}
			case 2: {
				var handle = newFD();
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
				var fd = newFD();
				var in = new FileInputStream(path);
				inputs.put(fd, in);
				return fd;
			}
			case WRITE: {
				var fd = newFD();
				var out = new FileOutputStream(path);
				outputs.put(fd, out);
				return fd;
			}
			case APPEND: {
				var fd = newFD();
				var out = new FileOutputStream(path, true);
				outputs.put(fd, out);
				return fd;
			}
			default:
				throw new IOException("Unknown mode: " + mode);
		}
	}

	@Override
	public <A extends BasicFileAttributes> A getAttributes(String path, Class<A> attrType, LinkOption... options) throws IOException {
		var p = Path.of(path);
		if (!p.toFile().exists()) return null;
		return Files.readAttributes(p, attrType, options);
	}
}
