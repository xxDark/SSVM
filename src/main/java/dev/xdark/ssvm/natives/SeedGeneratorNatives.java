package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * Initializes sun/security/provider/SeedGenerator.
 *
 * @author xDark
 */
@UtilityClass
public class SeedGeneratorNatives {

	/**
	 * @param vm VM instance.
	 */
	public static void init(VirtualMachine vm) {
		// TODO remove this stub, let VM decide.
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.sun_security_provider_SeedGenerator();
		vmi.setInvoker(jc, "getSystemEntropy", "()[B", ctx -> {
			byte[] bytes = new byte[20];
			new SecureRandom().nextBytes(bytes);
			ctx.setResult(vm.getHelper().toVMBytes(bytes));
			return Result.ABORT;
		});
	}
}
