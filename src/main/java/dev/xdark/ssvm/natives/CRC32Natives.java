package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.CrcUtil;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
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
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_util_zip_CRC32();
		vmi.setInvoker(jc, "updateBytes", "(I[BII)I", ctx -> {
			Locals locals = ctx.getLocals();
			int crc = locals.loadInt(0);
			ArrayValue bytes = locals.load(1);
			int off = locals.loadInt(2);
			int len = locals.loadInt(3);
			for (int x = off + len; off < x; off++) {
				crc = CrcUtil.update(crc, bytes.getByte(off));
			}
			ctx.setResult(IntValue.of(crc));
			return Result.ABORT;
		});
	}
}
