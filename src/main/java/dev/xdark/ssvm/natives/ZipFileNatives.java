package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.ZipFile;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass zf = symbols.java_util_zip_ZipFile();
		vmi.setInvoker(zf, "initIDs", "()V", MethodInvoker.noop());
		if (vmi.setInvoker(zf, "open", "(Ljava/lang/String;IJZ)J", ctx -> {
			// Old-style zip file implementation.
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ObjectValue path = locals.loadReference(0);
			helper.checkNotNull(path);
			String zipPath = helper.readUtf8(path);
			int mode = locals.loadInt(1);
			// last file modification & usemmap are ignored.
			try {
				long handle = vm.getFileDescriptorManager().openZipFile(zipPath, mode);
				if (handle == 0L) {
					helper.throwException(symbols.java_io_IOException(), zipPath);
				}
				ctx.setResult(handle);
			} catch (IOException ex) {
				helper.throwException(symbols.java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		})) {
			vmi.setInvoker(zf, "getTotal", "(J)I", ctx -> {
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(ctx.getLocals().loadLong(0));
				if (zip == null) {
					vm.getHelper().throwException(symbols.java_lang_IllegalStateException(), "zip closed");
				}
				ctx.setResult(zip.getTotal());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "startsWithLOC", "(J)Z", ctx -> {
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(ctx.getLocals().loadLong(0));
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(zip.startsWithLOC() ? 1 : 0);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntry", "(J[BZ)J", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.loadLong(0);
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
				VMHelper helper = vm.getHelper();
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				String entryName = new String(helper.toJavaBytes(locals.loadReference(2)), StandardCharsets.UTF_8);
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
				VMHelper helper = vm.getHelper();
				long address = locals.loadLong(0);
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(address);
				if (value == null) {
					throw new PanicException("Segfault");
				}
				int type = locals.loadInt(2);
				switch (type) {
					case 0:
						ctx.setResult(helper.toVMBytes(value.getName().getBytes(StandardCharsets.UTF_8)));
						break;
					case 1:
						byte[] extra = value.getExtra();
						ctx.setResult(extra == null ? vm.getMemoryManager().nullValue() : helper.toVMBytes(extra));
						break;
					case 2:
						String comment = value.getComment();
						ctx.setResult(comment == null ? vm.getMemoryManager().nullValue() : helper.toVMBytes(comment.getBytes(StandardCharsets.UTF_8)));
						break;
					default:
						ctx.setResult(vm.getMemoryManager().nullValue());
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryTime", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getTime());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCrc", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getCrc());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntrySize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getSize());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCSize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.loadLong(0));
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(value.getSize());
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryMethod", "(J)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.loadLong(0));
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
				ZipFile zipFile = vm.getFileDescriptorManager().getZipFile(jzfile);
				if (zipFile == null || !zipFile.freeHandle(locals.loadLong(2))) {
					throw new PanicException("Segfault");
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "read", "(JJJ[BII)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipFile zipFile = vm.getFileDescriptorManager().getZipFile(locals.loadLong(0));
				if (zipFile == null) {
					throw new PanicException("Segfault");
				}
				ZipEntry entry = zipFile.getEntry(locals.loadLong(2));
				if (entry == null) {
					throw new PanicException("Segfault");
				}
				long pos = locals.loadLong(4);
				if (pos > Integer.MAX_VALUE) {
					vm.getHelper().throwException(vm.getSymbols().java_util_zip_ZipException(), "Entry too large");
				}
				ArrayValue bytes = locals.loadReference(6);
				int off = locals.loadInt(7);
				int len = locals.loadInt(8);
				byte[] read;
				try {
					read = zipFile.readEntry(entry);
				} catch (IOException ex) {
					vm.getHelper().throwException(vm.getSymbols().java_util_zip_ZipException(), ex.getMessage());
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
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
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
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				long count = zip.stream()
					.filter(x -> "META-INF/MANIFEST.MF".equalsIgnoreCase(x.getName()))
					.count();
				if (count > Integer.MAX_VALUE) {
					count = Integer.MAX_VALUE;
				}
				ctx.setResult((int) count);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "close", "(J)V", ctx -> {
				try {
					if (!vm.getFileDescriptorManager().close(ctx.getLocals().loadLong(0))) {
						throw new PanicException("Segfault");
					}
				} catch (IOException ex) {
					vm.getHelper().throwException(symbols.java_util_zip_ZipException(), ex.getMessage());
				}
				return Result.ABORT;
			});
		}
	}
}
