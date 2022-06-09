package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.value.IntValue;
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
		InstanceJavaClass reflection = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/Reflection");
		if (reflection == null) {
			reflection = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/Reflection");
			if (reflection == null) {
				throw new IllegalStateException("Unable to locate Reflection class");
			}
		}
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", getCallerClass(false));
		vmi.setInvoker(reflection, "getCallerClass", "(I)Ljava/lang/Class;", getCallerClass(true));
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			JavaClass klass = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(IntValue.of(Modifier.eraseClass(klass.getModifiers())));
			return Result.ABORT;
		});
	}

	private static MethodInvoker getCallerClass(boolean useDepth) {
		return ctx -> {
			VirtualMachine vm = ctx.getVM();
			Backtrace backtrace = vm.currentThread().getBacktrace();
			int count = backtrace.count();
			int callerOffset = useDepth ? ctx.getLocals().load(0).asInt() + 1 : 2;
			JavaMethod caller = backtrace.get(count - callerOffset).getExecutionContext().getMethod();
			if (!useDepth) {
				callerOffset++;
			}
			if (caller.isCallerSensitive()) {
				while (true) {
					StackFrame frame = backtrace.get(count - callerOffset);
					ExecutionContext frameCtx = frame.getExecutionContext();
					if (frameCtx == null) {
						break;
					}
					JavaMethod method = frameCtx.getMethod();
					if (Modifier.isCallerSensitive(method.getAccess())) {
						callerOffset++;
					} else {
						break;
					}
				}
			}
			ctx.setResult(backtrace.get(count - callerOffset).getDeclaringClass().getOop());
			return Result.ABORT;
		};
	}
}
