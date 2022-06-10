package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * Initializes java/lang/ProcessEnvironment.
 *
 * @author xDark
 */
@UtilityClass
public class ProcessEnvironmentNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass processEnvironment = vm.getSymbols().java_lang_ProcessEnvironment();
		if (!vmi.setInvoker(processEnvironment, "environ", "()[[B", ctx -> {
			VMHelper helper = vm.getHelper();
			Map<String, String> env = vm.getenv();
			int idx = 0;
			int len = env.size();
			ArrayValue array = helper.newArray(vm.getPrimitives().bytePrimitive().newArrayClass(), len * 2);
			for (Map.Entry<String, String> entry : env.entrySet()) {
				ObjectValue key = helper.newUtf8(entry.getKey());
				ObjectValue value = helper.newUtf8(entry.getValue());
				ArrayValue keyBytes = (ArrayValue) helper.invokeVirtual("getBytes", "()[B", new Value[0], new Value[]{key}).getResult();
				ArrayValue valueBytes = (ArrayValue) helper.invokeVirtual("getBytes", "()[B", new Value[0], new Value[]{value}).getResult();
				array.setValue(idx++, keyBytes);
				array.setValue(idx++, valueBytes);
			}
			ctx.setResult(array);
			return Result.ABORT;
		})) {
			vmi.setInvoker(processEnvironment, "environmentBlock", "()Ljava/lang/String;", ctx -> {
				StringBuilder result = new StringBuilder();
				Map<String, String> env = vm.getenv();
				for (Map.Entry<String, String> entry : env.entrySet()) {
					result.append(entry.getKey()).append('=').append(entry.getValue())
						.append('\0');
				}
				ctx.setResult(vm.getHelper().newUtf8(result.append('\0').toString()));
				return Result.ABORT;
			});
		}
	}
}
