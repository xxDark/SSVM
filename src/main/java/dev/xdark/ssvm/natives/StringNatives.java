package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;

import java.nio.ByteOrder;

/**
 * Initializes string related parts.
 *
 * @author xDark
 */
@UtilityClass
public class StringNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass string = symbols.java_lang_String();
		vmi.setInvoker(string, "intern", "()Ljava/lang/String;", ctx -> {
			InstanceValue str = ctx.getLocals().loadReference(0);
			ctx.setResult(vm.getStringPool().intern(str));
			return Result.ABORT;
		});
		InstanceJavaClass utf16 = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StringUTF16");
		if (utf16 != null) {
			vmi.setInvoker(utf16, "isBigEndian", "()Z", ctx -> {
				ctx.setResult(vm.getMemoryAllocator().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0);
				return Result.ABORT;
			});
		}
	}
}
