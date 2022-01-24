package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.security.SecureRandom;

/**
 * Initializes sun/security/provider/SeedGenerator.
 *
 * @author xDark
 */
@UtilityClass
public class SeedGeneratorNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public static void init(VirtualMachine vm) {
		// TODO remove this stub, let VM decide.
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.sun_security_provider_SeedGenerator;
		vmi.setInvoker(jc, "getSystemEntropy", "()[B", ctx -> {
			val bytes = new byte[20];
			new SecureRandom().nextBytes(bytes);
			ctx.setResult(vm.getHelper().toVMBytes(bytes));
			return Result.ABORT;
		});
	}
}
