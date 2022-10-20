package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Package.
 *
 * @author xDark
 */
@UtilityClass
public class PackageNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_lang_Package();
		vmi.setInvoker(jc, "getSystemPackage0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getMemoryManager().nullValue());
			return Result.ABORT;
		});
	}
}
