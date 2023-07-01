package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.*;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.ZipFile;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;

/**
 * Initializes java/util/zip/ZipFile.
 *
 * @author xDark
 */
@UtilityClass
public class ZipFileNatives {

	/**
	 * @param vm          VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass zf = symbols.java_util_zip_ZipFile();
		vmi.setInvoker(zf, "initIDs", "()V", MethodInvoker.noop());
		if (hookZip(vm, fileManager)) {
			vmi.setInvoker(zf, "getTotal", "(J)I", ctx -> {
				ZipFile zip = fileManager.getZipFile(ctx.getLocals().loadLong(0));
				if (zip == null) {
					vm.getOperations().throwException(symbols.java_lang_IllegalStateException(), "zip closed");
					return Result.ABORT;
				}
				ctx.setResult(zip.getTotal());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "startsWithLOC", "(J)Z", ctx -> {
				ZipFile zip = fileManager.getZipFile(ctx.getLocals().loadLong(0));
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(zip.startsWithLOC() ? 1 : 0);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntry", "(J[BZ)J", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.loadLong(0);
				ZipFile zip = fileManager.getZipFile(handle);
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				VMOperations ops = vm.getOperations();
				String entryName = new String(ops.toJavaBytes(locals.loadReference(2)), StandardCharsets.UTF_8);
				ZipEntry entry = zip.getEntry(entryName);
				if (entry == null) {
					entry = zip.getEntry(entryName + '/');
				}

				if (entry == null) {
					ctx.setResult(0L);
				} else {
					long h = zip.makeHandle(entry);
					ctx.setResult(h);
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryBytes", "(JI)[B", ctx -> {
				Locals locals = ctx.getLocals();
				long address = locals.loadLong(0);
				ZipEntry value = fileManager.getZipEntry(address);
				if (value == null) {
					throw new PanicException("Segfault");
				}
				VMOperations ops = vm.getOperations();
				int type = locals.loadInt(2);
				switch (type) {
					case 0:
						ctx.setResult(ops.toVMBytes(value.getName().getBytes(StandardCharsets.UTF_8)));
						break;
					case 1:
						byte[] extra = value.getExtra();
						ctx.setResult(extra == null ? vm.getMemoryManager().nullValue() : ops.toVMBytes(extra));
						break;
					case 2:
						String comment = value.getComment();
						ctx.setResult(comment == null ? vm.getMemoryManager().nullValue() : ops.toVMBytes(comment.getBytes(StandardCharsets.UTF_8)));
						break;
					default:
						ctx.setResult(vm.getMemoryManager().nullValue());
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryTime", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = fileManager.getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getTime());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCrc", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = fileManager.getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getCrc());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntrySize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = fileManager.getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getSize());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCSize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = fileManager.getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getSize());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryMethod", "(J)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = fileManager.getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(ZipEntry.STORED);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryFlag", "(J)I", ctx -> {
				// TODO
				ctx.setResult(0);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "freeEntry", "(JJ)V", ctx -> {
				Locals locals = ctx.getLocals();
				long jzfile = locals.loadLong(0);
				ZipFile zipFile = fileManager.getZipFile(jzfile);
				if (zipFile == null || !zipFile.freeHandle(locals.loadLong(2))) {
					throw new PanicException("Segfault");
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "read", "(JJJ[BII)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipFile zipFile = fileManager.getZipFile(locals.loadLong(0));
				if (zipFile == null) {
					throw new PanicException("Segfault");
				}
				ZipEntry entry = zipFile.getEntry(locals.loadLong(2));
				if (entry == null) {
					throw new PanicException("Segfault");
				}
				long pos = locals.loadLong(4);
				if (pos > Integer.MAX_VALUE) {
					vm.getOperations().throwException(vm.getSymbols().java_util_zip_ZipException(), "Entry too large");
				}
				ArrayValue bytes = locals.loadReference(6);
				int off = locals.loadInt(7);
				int len = locals.loadInt(8);
				byte[] read;
				try {
					read = zipFile.readEntry(entry);
				} catch (IOException ex) {
					vm.getOperations().throwException(vm.getSymbols().java_util_zip_ZipException(), ex.getMessage());
					return Result.ABORT;
				}
				int start = (int) pos;
				if (start >= read.length) {
					ctx.setResult(-1);
				} else {
					int avail = read.length - start;
					if (len > avail) {
						len = avail;
					}
					if (len <= 0) {
						ctx.setResult(0);
					} else {
						int bytesStart = vm.getMemoryManager().arrayBaseOffset(byte.class);
						bytes.getMemory().getData()
							.write(bytesStart + off, read, start, len);
						ctx.setResult(len);
					}
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getNextEntry", "(JI)J", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.loadLong(0);
				ZipFile zip = fileManager.getZipFile(handle);
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				int idx = locals.loadInt(2);
				ZipEntry entry = zip.getEntry(idx);
				if (entry == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(zip.makeHandle(entry));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getManifestNum", "(J)I", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.loadLong(0);
				ZipFile zip = fileManager.getZipFile(handle);
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				long count = zip.stream()
					.filter(x -> "META-INF/MANIFEST.MF".equalsIgnoreCase(x.getName()))
					.count();
				ctx.setResult((int) count);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "close", "(J)V", ctx -> {
				try {
					if (!fileManager.close(ctx.getLocals().loadLong(0))) {
						throw new PanicException("Segfault");
					}
				} catch (IOException ex) {
					vm.getOperations().throwException(symbols.java_util_zip_ZipException(), ex.getMessage());
				}
				return Result.ABORT;
			});
		}
	}

	private static boolean hookZip(VirtualMachine vm, FileManager fileManager) {
		boolean hooked = false;
		VMOperations ops = vm.getOperations();
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass zf = symbols.java_util_zip_ZipFile();
		hooked |= vmi.setInvoker(zf, "open", "(Ljava/lang/String;IJZ)J", ctx -> {
			// Old-style zip file implementation.
			// This method only exists in JDK 8 and below. Later versions migrated to using RandomAccessFile.
			Locals locals = ctx.getLocals();
			ObjectValue path = locals.loadReference(0);
			ops.checkNotNull(path);
			String zipPath = ops.readUtf8(path);
			int mode = locals.loadInt(1);
			// last file modification & usemmap are ignored.
			try {
				long handle = fileManager.openZipFile(zipPath, mode);
				if (handle == 0L) {
					ops.throwException(symbols.java_io_IOException(), zipPath);
				}
				ctx.setResult(handle);
			} catch (IOException ex) {
				ops.throwException(symbols.java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		hooked |= vmi.setInvoker(zf, "<init>", "(Ljava/io/File;ILjava/nio/charset/Charset;)V", ctx -> {
			// Interpret the method
			InterpretedInvoker.INSTANCE.intercept(ctx);

			// We should have opened a file handle.
			// We want to move it from a standard input to a zip file in the file manager.
			ObjectValue _this = ctx.getLocals().loadReference(0);
			long handle = getJdk9ZipFileHandle(ctx, _this);
			try {
				fileManager.transferInputToZip(handle, java.util.zip.ZipFile.OPEN_READ);
			} catch (IOException ex) {
				ops.throwException(symbols.java_io_IOException(), ex.getMessage());
			}

			return Result.ABORT;
		});
		return hooked;
	}

	public static long getJdk9ZipFileHandle(ExecutionContext<?> ctx, ObjectValue zip) {
		VMOperations ops = ctx.getVM().getOperations();
		InstanceClass zf = ctx.getSymbols().java_util_zip_ZipFile();
		ObjectValue res = ops.getReference(zip, zf, "res", "Ljava/util/zip/ZipFile$CleanableResource;");
		ObjectValue zsrc = ops.getReference(res, "zsrc", "Ljava/util/zip/ZipFile$Source;");
		ObjectValue zfile = ops.getReference(zsrc, "zfile", "Ljava/io/RandomAccessFile;");
		ObjectValue fd = ops.getReference(zfile, "fd", "Ljava/io/FileDescriptor;");
		return ops.getLong(fd, "handle");
	}
}
