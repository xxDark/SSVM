package dev.xdark.ssvm.natives;


import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
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
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.sun_security_provider_NativeSeedGenerator();
		vmi.setInvoker(jc, "nativeGenerateSeed", "([B)Z", ctx -> {
			ArrayValue array = vm.getHelper().checkNotNull(ctx.getLocals().<ObjectValue>load(0));
			int len = array.getLength();
			SecureRandom rng = new SecureRandom();
			byte[] tmp = new byte[1];
			while (len-- != 0) {
				rng.nextBytes(tmp);
				array.setByte(len, tmp[0]);
			}
			ctx.setResult(IntValue.ONE);
			return Result.ABORT;
		});
	}
}
