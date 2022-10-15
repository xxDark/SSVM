package dev.xdark.ssvm.natives;


import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * Initializes sun/security/provider/NativeSeedGenerator.
 *
 * @author xDark
 */
@UtilityClass
public class NativeSeedGeneratorNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.sun_security_provider_NativeSeedGenerator();
		vmi.setInvoker(jc, "nativeGenerateSeed", "([B)Z", ctx -> {
			ArrayValue array = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(0));
			int len = array.getLength();
			SecureRandom rng = new SecureRandom();
			byte[] tmp = new byte[1];
			while (len-- != 0) {
				rng.nextBytes(tmp);
				array.setByte(len, tmp[0]);
			}
			ctx.setResult(1);
			return Result.ABORT;
		});
	}
}
