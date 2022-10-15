package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.sun_management_VMManagementImpl();
		vmi.setInvoker(jc, "getVersion0", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getStringPool().intern(vm.getManagementInterface().getVersion()));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getStartupTime", "()J", ctx -> {
			ctx.setResult(vm.getManagementInterface().getStartupTime());
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getVmArguments0", "()[Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();
			List<String> args = vm.getManagementInterface().getInputArguments();
			ObjectValue[] vmArgs = new ObjectValue[args.size()];
			for (int i = 0; i < args.size(); i++) {
				vmArgs[i] = ops.newUtf8(args.get(i));
			}
			ctx.setResult(ops.toVMReferences(vmArgs));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "initOptionalSupportFields", "()V", MethodInvoker.noop());
	}
}
