package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.NullValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Package.
 *
 * @author xDark
 */
@UtilityClass
public class PackageNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_lang_Package;
		vmi.setInvoker(jc, "getSystemPackage0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(NullValue.INSTANCE);
			return Result.ABORT;
		});
	}
}
