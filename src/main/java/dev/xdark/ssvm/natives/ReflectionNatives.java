package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.StreamSupport;

/**
 * Initializes reflect/Reflection class.
 *
 * @author xDark
 */
@UtilityClass
public class ReflectionNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		InstanceJavaClass reflection = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/Reflection");
		if (reflection == null) {
			reflection = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/Reflection");
			if (reflection == null) {
				throw new IllegalStateException("Unable to locate Reflection class");
			}
		}
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", ctx -> {
			val backtrace = vm.currentThread().getBacktrace();
			int count = backtrace.count();
			val caller = backtrace.get(count - 2).getExecutionContext().getMethod();
			int offset = 3;
			if (caller.isCallerSensitive()) {
				while (true) {
					val frame = backtrace.get(count - offset);
					val method = frame.getExecutionContext().getMethod();
					if (Modifier.isCallerSensitive(method.getAccess())) {
						offset++;
					} else {
						break;
					}
				}
			}
			ctx.setResult(backtrace.get(count - offset).getDeclaringClass().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			val klass = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(IntValue.of(Modifier.erase(klass.getModifiers())));
			return Result.ABORT;
		});
		// TODO FIXME
		vmi.setInvoker(reflection, "isCallerSensitive", "(Ljava/lang/reflect/Method;)Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
	}
}
