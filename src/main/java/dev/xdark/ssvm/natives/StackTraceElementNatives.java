package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/StackTraceElement.
 *
 * @author xDark
 */
@UtilityClass
public class StackTraceElementNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass stackTraceElement = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		vmi.setInvoker(stackTraceElement, "initStackTraceElements", "([Ljava/lang/StackTraceElement;Ljava/lang/Throwable;)V", ctx -> {
			VMHelper helper = vm.getHelper();
			Locals locals = ctx.getLocals();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			InstanceValue ex = helper.checkNotNull(locals.loadReference(1));
			Backtrace backtrace = ((JavaValue<Backtrace>) ex.getValue("backtrace", "Ljava/lang/Object;")).getValue();

			int x = 0;
			for (int i = backtrace.count(); i != 0; ) {
				StackFrame frame = backtrace.get(--i);
				InstanceValue element = helper.newStackTraceElement(frame, true);
				arr.setReference(x++, element);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(stackTraceElement, "isHashedInJavaBase", "(Ljava/lang/Module;)Z", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});
	}
}
