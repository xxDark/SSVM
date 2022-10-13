package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.CrcUtil;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/util/zip/CRC32.
 *
 * @author xDark
 */
@UtilityClass
public class CRC32Natives {

	/**
	 * @param vm VM instance.
	 */
	public static void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_util_zip_CRC32();
		vmi.setInvoker(jc, "updateBytes", "(I[BII)I", ctx -> {
			Locals locals = ctx.getLocals();
			int crc = locals.loadInt(0);
			ArrayValue bytes = vm.getHelper().checkNotNull(locals.loadReference(1));
			int off = locals.loadInt(2);
			int len = locals.loadInt(3);
			for (int x = off + len; off < x; off++) {
				crc = CrcUtil.update(crc, bytes.getByte(off));
			}
			ctx.setResult(crc);
			return Result.ABORT;
		});
	}
}
