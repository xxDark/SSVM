package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.Value;
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
		if (!vmi.setInvoker(processEnvironment, "environ", "()[[B", ctx -> {
			val helper = vm.getHelper();
			val env = vm.getenv();
			int idx = 0;
			int len = env.size();
			val array = helper.newArray(vm.getPrimitives().bytePrimitive.newArrayClass(), len);
			for (val entry : env.entrySet()) {
				val key = helper.newUtf8(entry.getKey());
				val value = helper.newUtf8(entry.getValue());
				val keyBytes = (ArrayValue) helper.invokeVirtual("getBytes", "()[B", new Value[0], new Value[]{key}).getResult();
				val valueBytes = (ArrayValue) helper.invokeVirtual("getBytes", "()[B", new Value[0], new Value[]{value}).getResult();
				array.setValue(idx++, keyBytes);
				array.setValue(idx++, valueBytes);
			}
			ctx.setResult(array);
			return Result.ABORT;
		})) {
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
}
