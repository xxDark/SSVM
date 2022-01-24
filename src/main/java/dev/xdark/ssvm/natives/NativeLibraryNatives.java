package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/ClassLoader$NativeLibrary.
 *
 * @author xDark
 */
@UtilityClass
public class NativeLibraryNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val library = symbols.java_lang_ClassLoader$NativeLibrary;
		vmi.setInvoker(library, "load", "(Ljava/lang/String;Z)V", ctx -> {
			// TODO
			val _this = ctx.getLocals().<InstanceValue>load(0);
			_this.setBoolean("loaded", true);
			return Result.ABORT;
		});
	}
}
