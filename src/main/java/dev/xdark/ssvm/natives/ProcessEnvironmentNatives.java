package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
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
			JavaMethod getBytes = vm.getPublicLinkResolver().resolveSpecialMethod(vm.getSymbols().java_lang_String(), "getBytes", "()[B");
			ThreadStorage ts = vm.getThreadStorage();
			for (Map.Entry<String, String> entry : env.entrySet()) {
				ObjectValue key = helper.newUtf8(entry.getKey());
				ObjectValue value = helper.newUtf8(entry.getValue());
				ArrayValue keyBytes, valueBytes;
				{
					Locals locals = ts.newLocals(getBytes);
					locals.setReference(0, key);
					keyBytes = (ArrayValue) helper.invokeReference(getBytes, locals);
				}
				{
					Locals locals = ts.newLocals(getBytes);
					locals.setReference(0, value);
					valueBytes = (ArrayValue) helper.invokeReference(getBytes, locals);
				}
				array.setReference(idx++, keyBytes);
				array.setReference(idx++, valueBytes);
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
