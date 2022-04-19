package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/StackTraceElement.
 *
 * @author xDark
 */
@UtilityClass
public class StackTraceElementNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val stackTraceElement = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		vmi.setInvoker(stackTraceElement, "initStackTraceElements", "([Ljava/lang/StackTraceElement;Ljava/lang/Throwable;)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val arr = helper.checkArray(locals.load(0));
			val ex = helper.<InstanceValue>checkNotNull(locals.load(1));
			val backtrace = ((JavaValue<Backtrace>) ex.getValue("backtrace", "Ljava/lang/Object;")).getValue();

			int x = 0;
			for (int i = backtrace.count(); i != 0; ) {
				val frame = backtrace.get(--i);
				val element = helper.newStackTraceElement(frame, true);
				arr.setValue(x++, element);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(stackTraceElement, "isHashedInJavaBase", "(Ljava/lang/Module;)Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
	}
}
