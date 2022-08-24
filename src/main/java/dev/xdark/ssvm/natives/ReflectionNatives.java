package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.JavaValue;
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
		InstanceJavaClass reflection = vm.getSymbols().internal_reflect_Reflection();
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", getCallerClass(false));
		vmi.setInvoker(reflection, "getCallerClass", "(I)Ljava/lang/Class;", getCallerClass(true));
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			JavaClass klass = ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue();
			ctx.setResult(Modifier.eraseClass(klass.getModifiers()));
			return Result.ABORT;
		});
	}

	private static MethodInvoker getCallerClass(boolean useDepth) {
		return ctx -> {
			int callerOffset = useDepth ? ctx.getLocals().loadInt(0) + 1 : 2;
			ctx.setResult(ctx.getVM().getReflection().getCallerFrame(callerOffset).getDeclaringClass().getOop());
			return Result.ABORT;
		};
	}
}
