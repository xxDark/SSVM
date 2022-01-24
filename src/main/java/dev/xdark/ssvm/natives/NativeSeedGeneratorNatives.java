package dev.xdark.ssvm.natives;


import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.security.SecureRandom;

/**
 * Initializes sun/security/provider/NativeSeedGenerator.
 *
 * @author xDark
 */
@UtilityClass
public class NativeSeedGeneratorNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.sun_security_provider_NativeSeedGenerator;
		vmi.setInvoker(jc, "nativeGenerateSeed", "([B)Z", ctx -> {
			val array = vm.getHelper().checkNotNullArray(ctx.getLocals().load(0));
			int len = array.getLength();
			val rng = new SecureRandom();
			val tmp = new byte[1];
			while (len-- != 0) {
				rng.nextBytes(tmp);
				array.setByte(len, tmp[0]);
			}
			ctx.setResult(IntValue.ONE);
			return Result.ABORT;
		});
	}
}
