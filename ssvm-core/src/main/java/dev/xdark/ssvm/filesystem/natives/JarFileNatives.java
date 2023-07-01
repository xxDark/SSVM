package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.ZipFile;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.zip.ZipEntry;

/**
 * Initializes java/util/jar/JarFile.
 *
 * @author xDark
 */
@UtilityClass
public class JarFileNatives {

	/**
	 * @param vm VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass zf = symbols.java_util_zip_ZipFile();
		InstanceClass jf = symbols.java_util_jar_JarFile();
		vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();

			long handle;
			ObjectValue _this = ctx.getLocals().loadReference(0);
			if (vm.getJvmVersion() <= 8) {
				// Directly has file handle as a field
				handle = ops.getLong(_this, zf, "jzfile");
			} else {
				// ZipFile.res --> CleanableResource.zsrc --> Source.zfile --> RandomAccessFile.fd --> FileDescriptor.handle
				ObjectValue res = ops.getReference(_this, zf, "res", "Ljava/util/zip/ZipFile$CleanableResource;");
				ObjectValue zsrc = ops.getReference(res, "zsrc", "Ljava/util/zip/ZipFile$Source;");
				ObjectValue zfile = ops.getReference(zsrc, "zfile", "Ljava/io/RandomAccessFile;");
				ObjectValue fd = ops.getReference(zfile, "fd", "Ljava/io/FileDescriptor;");
				handle = ops.getLong(fd, "handle");
			}

			ZipFile zip = fileManager.getZipFile(handle);
			if (zip == null) {
				ops.throwException(symbols.java_lang_IllegalStateException(), "zip closed");
			}
			ObjectValue[] paths = zip.stream()
				.map(ZipEntry::getName)
				.filter(name -> name.toUpperCase(Locale.ENGLISH).startsWith("META-INF/"))
				.map(ops::newUtf8)
				.toArray(ObjectValue[]::new);
			ctx.setResult(ops.toVMReferences(paths));
			return Result.ABORT;
		});
	}
}
