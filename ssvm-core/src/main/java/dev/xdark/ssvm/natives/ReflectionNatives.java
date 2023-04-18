package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes reflect/Reflection class.
 *
 * @author xDark
 */
@UtilityClass
public class ReflectionNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass reflection = vm.getSymbols().internal_reflect_Reflection();
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", getCallerClass(vm, false));
		vmi.setInvoker(reflection, "getCallerClass", "(I)Ljava/lang/Class;", getCallerClass(vm, true));
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			JavaClass klass = ctx.getClassStorage().lookup(ctx.getOperations().checkNotNull(ctx.getLocals().loadReference(0)));
			ctx.setResult(Modifier.eraseClass(klass.getModifiers()));
			return Result.ABORT;
		});
	}

	private static MethodInvoker getCallerClass(VirtualMachine vm, boolean useDepth) {
		return ctx -> {
			if (!vm.currentOSThread().getBacktrace().at(2).getMethod().isCallerSensitive()) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_InternalError(), "CallerSensitive annotation expected at frame 1");
				return Result.ABORT;
			}
			int callerOffset = useDepth ? ctx.getLocals().loadInt(0) + 1 : 3;
			ExecutionContext<?> caller = vm.getReflection().getCallerFrame(callerOffset);
			ctx.setResult(caller == null ? vm.getMemoryManager().nullValue() : caller.getOwner().getOop());
			return Result.ABORT;
		};
	}
}
