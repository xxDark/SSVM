package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
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
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass zf = symbols.java_util_zip_ZipFile();
		InstanceClass jf = symbols.java_util_jar_JarFile();
		vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();
			long handle = ops.getLong(ctx.getLocals().loadReference(0), zf, "jzfile");
			ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
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
