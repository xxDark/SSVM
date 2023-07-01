package dev.xdark.ssvm.filesystem;

import dev.xdark.ssvm.io.Handle;
import dev.xdark.ssvm.util.IOUtil;

import java.io.*;
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
public class HostFileManager implements FileManager {

	protected final Map<Handle, InputStream> inputs = new HashMap<>();
	protected final Map<Handle, OutputStream> outputs = new HashMap<>();
	protected final Map<Handle, ZipFile> zipFiles = new HashMap<>();

	protected final InputStream stdin;
	protected final OutputStream stdout;
	protected final OutputStream stderr;

	/**
	 * @param stdin  System input stream.
	 * @param stdout System output stream.
	 * @param stderr System error stream.
	 */
	public HostFileManager(InputStream stdin, OutputStream stdout, OutputStream stderr) {
		this.stdin = stdin;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public HostFileManager() {
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
	public synchronized long getRealHandle(long handle) {
		Handle h = Handle.threadLocal(handle);
		try {
			FileDescriptor fd = null;
			InputStream in = inputs.get(h);
			if (in != null) {
				if (in instanceof FileInputStream) {
					fd = ((FileInputStream) in).getFD();
				} else if (in == stdin) {
					fd = FileDescriptor.in;
				}
			}
			if (fd == null) {
				OutputStream out = outputs.get(h);
				if (out != null) {
					if (out instanceof FileOutputStream) {
						fd = ((FileOutputStream) out).getFD();
					} else if (out == stdout) {
						fd = FileDescriptor.out;
					} else if (out == stderr) {
						fd = FileDescriptor.err;
					}
				}
			}
			if (fd != null) {
				return IOUtil.getHandleOrFd(fd);
			}
		} catch (IOException ex) {
		}
		return -1L;
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
		switch (stream) {
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
		switch (mode) {
			case READ: {
				long fd = newFD();
				InputStream in = new BufferedInputStream(new FileInputStream(path));
				in.mark(Integer.MAX_VALUE);
				Handle h = Handle.of(fd);
				inputs.put(h, in);
				return fd;
			}
			case WRITE: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path);
				Handle h = Handle.of(fd);
				outputs.put(h, out);
				return fd;
			}
			case APPEND: {
				long fd = newFD();
				FileOutputStream out = new FileOutputStream(path, true);
				Handle h = Handle.of(fd);
				outputs.put(h, out);
				return fd;
			}
			default:
				throw new IOException("Unknown mode: " + mode);
		}
	}

	@Override
	public boolean rename(String oldPath, String newPath) {
		return new File(oldPath).renameTo(new File(newPath));
	}

	@Override
	public boolean delete(String path) {
		return new File(path).delete();
	}

	@Override
	public boolean checkAccess(String path, int access) {
		File file = new File(path);
		switch (access) {
			case FileManager.ACCESS_READ: return file.canRead();
			case FileManager.ACCESS_WRITE: return file.canWrite();
			case FileManager.ACCESS_EXECUTE: return file.canExecute();
			default: return false;
		}
	}

	@Override
	public boolean setPermission(String path, int flag, boolean value, boolean ownerOnly) {
		File file = new File(path);
		switch (flag) {
			case ACCESS_READ: return file.setReadable(value);
			case ACCESS_WRITE: return file.setWritable(value);
			case ACCESS_EXECUTE: return file.setExecutable(value);
			default: return false;
		}
	}

	@Override
	public boolean setLastModifiedTime(String path, long time) {
		return new File(path).setLastModified(time);
	}

	@Override
	public boolean setReadOnly(String path) {
		return new File(path).setReadOnly();
	}

	@Override
	public long getSpace(String path, int id) {
		File file = new File(path);
		switch (id) {
			case FileManager.SPACE_TOTAL: return file.getTotalSpace();
			case FileManager.SPACE_FREE: return file.getFreeSpace();
			case FileManager.SPACE_USABLE: return file.getUsableSpace();
			default: return 0L;
		}
	}

	@Override
	public boolean createFileExclusively(String path) throws IOException {
		return new File(path).createNewFile();
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
		ZipFile zf = new SimpleZipFile(fd, new java.util.zip.ZipFile(new File(path), mode));
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
		switch (stream) {
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
		} while (raw == 0L || inputs.containsKey(h) || outputs.containsKey(h) || zipFiles.containsKey(h));
		return raw;
	}
}
