package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.ZipFile;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
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
			ObjectValue path = locals.<ObjectValue>load(0);
			helper.checkNotNull(path);
			String zipPath = helper.readUtf8(path);
			int mode = locals.load(1).asInt();
			// last file modification & usemmap are ignored.
			try {
				long handle = vm.getFileDescriptorManager().openZipFile(zipPath, mode);
				if (handle == 0L) {
					helper.throwException(symbols.java_io_IOException(), zipPath);
				}
				ctx.setResult(LongValue.of(handle));
			} catch (IOException ex) {
				helper.throwException(symbols.java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		})) {
			vmi.setInvoker(zf, "getTotal", "(J)I", ctx -> {
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(ctx.getLocals().load(0).asLong());
				if (zip == null) {
					vm.getHelper().throwException(symbols.java_lang_IllegalStateException(), "zip closed");
				}
				ctx.setResult(IntValue.of(zip.getTotal()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "startsWithLOC", "(J)Z", ctx -> {
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(ctx.getLocals().load(0).asLong());
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(zip.startsWithLOC() ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntry", "(J[BZ)J", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.load(0).asLong();
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
				VMHelper helper = vm.getHelper();
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				String entryName = new String(helper.toJavaBytes(locals.load(2)), StandardCharsets.UTF_8);
				ZipEntry entry = zip.getEntry(entryName);
				if (entry == null) {
					entry = zip.getEntry(entryName + '/');
				}

				if (entry == null) {
					ctx.setResult(LongValue.ZERO);
				} else {
					long h = zip.makeHandle(entry);
					ctx.setResult(LongValue.of(h));
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryBytes", "(JI)[B", ctx -> {
				Locals locals = ctx.getLocals();
				VMHelper helper = vm.getHelper();
				long address = locals.load(0).asLong();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(address);
				if (value == null) {
					throw new PanicException("Segfault");
				}
				int type = locals.load(2).asInt();
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
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.load(0).asLong());
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(LongValue.of(value.getTime()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCrc", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.load(0).asLong());
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(LongValue.of(value.getCrc()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntrySize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.load(0).asLong());
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(LongValue.of(value.getSize()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryCSize", "(J)J", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.load(0).asLong());
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(LongValue.of(value.getSize()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryMethod", "(J)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipEntry value = vm.getFileDescriptorManager().getZipEntry(locals.load(0).asLong());
				if (value == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(IntValue.of(ZipEntry.STORED));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntryFlag", "(J)I", ctx -> {
				// TODO
				ctx.setResult(IntValue.ZERO);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "freeEntry", "(JJ)V", ctx -> {
				Locals locals = ctx.getLocals();
				long jzfile = locals.load(0).asLong();
				ZipFile zipFile = vm.getFileDescriptorManager().getZipFile(jzfile);
				if (zipFile == null || !zipFile.freeHandle(locals.load(2).asLong())) {
					throw new PanicException("Segfault");
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "read", "(JJJ[BII)I", ctx -> {
				Locals locals = ctx.getLocals();
				ZipFile zipFile = vm.getFileDescriptorManager().getZipFile(locals.load(0).asLong());
				if (zipFile == null) {
					throw new PanicException("Segfault");
				}
				ZipEntry entry = zipFile.getEntry(locals.load(2).asLong());
				if (entry == null) {
					throw new PanicException("Segfault");
				}
				long pos = locals.load(4).asLong();
				if (pos > Integer.MAX_VALUE) {
					vm.getHelper().throwException(vm.getSymbols().java_util_zip_ZipException(), "Entry too large");
				}
				ArrayValue bytes = locals.<ArrayValue>load(6);
				int off = locals.load(7).asInt();
				int len = locals.load(8).asInt();
				byte[] read;
				try {
					read = zipFile.readEntry(entry);
				} catch (IOException ex) {
					vm.getHelper().throwException(vm.getSymbols().java_util_zip_ZipException(), ex.getMessage());
					return Result.ABORT;
				}
				int start = (int) pos;
				if (start >= read.length) {
					ctx.setResult(IntValue.M_ONE);
				} else {
					int avail = read.length - start;
					if (len > avail) {
						len = avail;
					}
					if (len <= 0) {
						ctx.setResult(IntValue.ZERO);
					} else {
						for (int i = 0; i < len; i++) {
							bytes.setByte(off + i, read[start + i]);
						}
						ctx.setResult(IntValue.of(len));
					}
				}
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getNextEntry", "(JI)J", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.load(0).asLong();
				ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
				if (zip == null) {
					throw new PanicException("Segfault");
				}
				int idx = locals.load(2).asInt();
				ZipEntry entry = zip.getEntry(idx);
				if (entry == null) {
					throw new PanicException("Segfault");
				}
				ctx.setResult(LongValue.of(zip.makeHandle(entry)));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getManifestNum", "(J)I", ctx -> {
				Locals locals = ctx.getLocals();
				long handle = locals.load(0).asLong();
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
				ctx.setResult(IntValue.of((int) count));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "close", "(J)V", ctx -> {
				try {
					if (!vm.getFileDescriptorManager().close(ctx.getLocals().load(0).asLong())) {
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
