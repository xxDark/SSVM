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
		Long wrapper = handle;
		InputStream in = inputs.remove(wrapper);
		if (in != null) {
			in.close();
			return;
		}
		OutputStream out = outputs.remove(wrapper);
		if (out != null) {
			out.close();
			return;
		}
		ZipFile zip = zipFiles.remove(wrapper);
		if (zip != null) {
			zip.close();
		}
	}

	@Override
	public long newFD() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		Map<Long, InputStream> inputs = this.inputs;
		Map<Long, OutputStream> outputs = this.outputs;
		Map<Long, ZipFile> zipFiles = this.zipFiles;
		long handle;
		Long wrapper;
		do {
			handle = rng.nextLong();
		} while(inputs.containsKey(wrapper = handle) || outputs.containsKey(wrapper) || zipFiles.containsKey(wrapper));
		return handle;
	}

	@Override
	public long newFD(int stream) {
		switch(stream) {
			case 0: {
				long handle = newFD();
				inputs.put(handle, stdin);
				return handle;
			}
			case 1: {
				long handle = newFD();
				outputs.put(handle, stdout);
				return handle;
			}
			case 2: {
				long handle = newFD();
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
		switch(mode) {
			case READ: {
				long fd = newFD();
				FileInputStream in = new FileInputStream(path);
				inputs.put(fd, in);
				return fd;
			}
			case WRITE: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path);
				outputs.put(fd, out);
				return fd;
			}
			case APPEND: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path, true);
				outputs.put(fd, out);
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
	public long openZipFile(String path, int mode) throws IOException {
		long fd = newFD();
		ZipFile zf = new SimpleZipFile(new java.util.zip.ZipFile(new File(path), mode));
		zipFiles.put(fd, zf);
		return fd;
	}

	@Override
	public ZipFile getZipFile(long handle) {
		return zipFiles.get(handle);
	}

	@Override
	public String getCurrentWorkingDirectory() {
		return new File("").getAbsolutePath();
	}
}
