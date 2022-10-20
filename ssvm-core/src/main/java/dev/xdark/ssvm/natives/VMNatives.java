package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes VM class.
 *
 * @author xDark
 */
@UtilityClass
public class VMNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass klass = (InstanceClass) vm.findBootstrapClass("jdk/internal/misc/VM");
		if (klass != null) {
			vmi.setInvoker(klass, "initializeFromArchive", "(Ljava/lang/Class;)V", MethodInvoker.noop());
		} else {
			klass = (InstanceClass) vm.findBootstrapClass("sun/misc/VM");
			if (klass == null) {
				throw new IllegalStateException("Unable to locate VM class");
			}
			vmi.setInvoker(klass, "latestUserDefinedLoader0", "()Ljava/lang/ClassLoader;", ctx -> {
				vm.getOperations().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError());
				return Result.ABORT;
			});
		}
		vmi.setInvoker(klass, "initialize", "()V", MethodInvoker.noop());
		InstanceClass win32ErrorMode = (InstanceClass) vm.findBootstrapClass("sun/io/Win32ErrorMode");
		if (win32ErrorMode != null) {
			vmi.setInvoker(win32ErrorMode, "setErrorMode", "(J)J", ctx -> {
				ctx.setResult(0L);
				return Result.ABORT;
			});
		}
	}
}
