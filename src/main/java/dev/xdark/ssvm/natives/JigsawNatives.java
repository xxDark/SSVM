package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes module system.
 *
 * @author xDark
 */
@UtilityClass
public class JigsawNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass bootLoader = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/loader/BootLoader");
		if (bootLoader != null) {
			vmi.setInvoker(bootLoader, "setBootLoaderUnnamedModule0", "(Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(bootLoader, "getSystemPackageLocation", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
				ctx.setResult(vm.getMemoryManager().nullValue());
				return Result.ABORT;
			});
		}
		InstanceJavaClass module = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Module");
		if (module != null) {
			vmi.setInvoker(module, "defineModule0", "(Ljava/lang/Module;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "defineModule0", "(Ljava/lang/Module;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addReads0", "(Ljava/lang/Module;Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExports0", "(Ljava/lang/Module;Ljava/lang/String;Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExportsToAll0", "(Ljava/lang/Module;Ljava/lang/String;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExportsToAllUnnamed0", "(Ljava/lang/Module;Ljava/lang/String;)V", MethodInvoker.noop());
		}
		InstanceJavaClass moduleLayer = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ModuleLayer");
		if (moduleLayer != null) {
			vmi.setInvoker(moduleLayer, "defineModules", "(Ljava/lang/module/Configuration;Ljava/util/function/Function;)Ljava/lang/ModuleLayer;", ctx -> {
				ctx.setResult(ctx.getLocals().load(0));
				return Result.ABORT;
			});
		}
	}
}
