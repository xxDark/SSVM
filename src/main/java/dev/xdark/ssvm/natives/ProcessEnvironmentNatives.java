package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/ProcessEnvironment.
 *
 * @author xDark
 */
@UtilityClass
public class ProcessEnvironmentNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val processEnvironment = vm.getSymbols().java_lang_ProcessEnvironment;
		vmi.setInvoker(processEnvironment, "environmentBlock", "()Ljava/lang/String;", ctx -> {
			val result = new StringBuilder();
			val env = vm.getenv();
			for (val entry : env.entrySet()) {
				result.append(entry.getKey()).append('=').append(entry.getValue())
						.append('\0');
			}
			ctx.setResult(vm.getHelper().newUtf8(result.append('\0').toString()));
			return Result.ABORT;
		});
	}
}
