package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Iinitalizes management interface.
 *
 * @author xDark
 */
@UtilityClass
public class VMManagementNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.sun_management_VMManagementImpl;
		vmi.setInvoker(jc, "getVersion0", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getStringPool().intern(vm.getManagementInterface().getVersion()));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getStartupTime", "()J", ctx -> {
			ctx.setResult(LongValue.of(vm.getManagementInterface().getStartupTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getVmArguments0", "()[Ljava/lang/String;", ctx -> {
			VMHelper helper = vm.getHelper();
			List<String> args = vm.getManagementInterface().getInputArguments();
			ObjectValue[] vmArgs = new ObjectValue[args.size()];
			for (int i = 0; i < args.size(); i++) {
				vmArgs[i] = helper.newUtf8(args.get(i));
			}
			ctx.setResult(helper.toVMValues(vmArgs));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "initOptionalSupportFields", "()V", MethodInvoker.noop());
	}
}
