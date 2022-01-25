package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.CrcUtil;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/util/zip/CRC32.
 *
 * @author xDark
 */
@UtilityClass
public class CRC32Natives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public static void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_util_zip_CRC32;
		vmi.setInvoker(jc, "updateBytes", "(I[BII)I", ctx -> {
			val locals = ctx.getLocals();
			int crc = locals.load(0).asInt();
			val bytes = locals.<ArrayValue>load(1);
			int off = locals.load(2).asInt();
			int len = locals.load(3).asInt();
			for (int x = off + len; off < x; off++) {
				crc = CrcUtil.update(crc, bytes.getByte(off));
			}
			ctx.setResult(IntValue.of(crc));
			return Result.ABORT;
		});
	}
}
