package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.ZipFile;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
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
		if (vm.getJvmVersion() <= 8) {
			vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
				VMOperations ops = vm.getOperations();
				ObjectValue _this = ctx.getLocals().loadReference(0);

				// Directly has file handle as a field in JDK 8
				long handle = ops.getLong(_this, zf, "jzfile");

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
		} else {
			// TODO: Noop for this method is a hack and should be fixed later (Affects JDK 9+)
			vmi.setInvoker(jf, "checkForSpecialAttributes", "()V", MethodInvoker.noop());
			vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
				VMOperations ops = vm.getOperations();
				ObjectValue _this = ctx.getLocals().loadReference(0);

				// JDK 9+ has lots of indirection to get the file handle...
				// ZipFile.res --> CleanableResource.zsrc --> Source.zfile --> RandomAccessFile.fd --> FileDescriptor.handle
				ObjectValue res = ops.getReference(_this, zf, "res", "Ljava/util/zip/ZipFile$CleanableResource;");
				ObjectValue zsrc = ops.getReference(res, "zsrc", "Ljava/util/zip/ZipFile$Source;");
				ObjectValue zfile = ops.getReference(zsrc, "zfile", "Ljava/io/RandomAccessFile;");
				ObjectValue fd = ops.getReference(zfile, "fd", "Ljava/io/FileDescriptor;");
				long handle = ops.getLong(fd, "handle");

				// zsrc tracks which meta-inf names are found as offsets
				ObjectValue metanames = ops.getReference(zsrc, "metanames", "[I");
				if (metanames.isNull()) {
					ObjectValue[] paths = new ObjectValue[0];
					ctx.setResult(ops.toVMReferences(paths));
					return Result.ABORT;
				}

				// TODO: Conversion process prevents this from being found
				//  - original fd handle is long
				//  - but zip file handles are int, so we cannot fit the original handle into the int range
				ZipFile zipFile = fileManager.getZipFile(handle);
				ObjectValue[] paths = zipFile.stream()
						.map(ZipEntry::getName)
						.filter(name -> name.startsWith("META-INF/"))
						.map(ops::newUtf8)
						.toArray(ObjectValue[]::new);
				ctx.setResult(ops.toVMReferences(paths));
				return Result.ABORT;
			});
		}
	}
}
