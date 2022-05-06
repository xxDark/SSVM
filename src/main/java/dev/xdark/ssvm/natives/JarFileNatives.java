package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.ZipFile;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.InstanceValue;
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
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass zf = symbols.java_util_jar_JarFile();
		vmi.setInvoker(zf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
			long handle = ctx.getLocals().<InstanceValue>load(0).getLong("jzfile");
			ZipFile zip = vm.getFileDescriptorManager().getZipFile(handle);
			VMHelper helper = vm.getHelper();
			if (zip == null) {
				helper.throwException(symbols.java_lang_IllegalStateException(), "zip closed");
			}
			ObjectValue[] paths = zip.stream()
					.map(ZipEntry::getName)
					.filter(name -> name.toUpperCase(Locale.ENGLISH).startsWith("META-INF/"))
					.map(helper::newUtf8)
					.toArray(ObjectValue[]::new);
			ctx.setResult(helper.toVMValues(paths));
			return Result.ABORT;
		});
	}
}
