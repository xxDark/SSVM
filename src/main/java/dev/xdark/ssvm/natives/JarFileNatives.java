package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import lombok.val;

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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val zf = symbols.java_util_jar_JarFile;
		vmi.setInvoker(zf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
			val handle = ctx.getLocals().<InstanceValue>load(0).getLong("jzfile");
			val zip = vm.getFileDescriptorManager().getZipFile(handle);
			val helper = vm.getHelper();
			if (zip == null) {
				helper.throwException(symbols.java_lang_IllegalStateException, "zip closed");
			}
			val paths = zip.stream()
					.map(ZipEntry::getName)
					.filter(name -> name.toUpperCase(Locale.ENGLISH).startsWith("META-INF/"))
					.map(helper::newUtf8)
					.toArray(ObjectValue[]::new);
			ctx.setResult(helper.toVMValues(paths));
			return Result.ABORT;
		});
	}
}
