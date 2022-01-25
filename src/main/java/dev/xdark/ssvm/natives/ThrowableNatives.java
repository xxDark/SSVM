package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.SimpleBacktrace;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Throwable.
 *
 * @author xDark
 */
@UtilityClass
public class ThrowableNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val throwable = symbols.java_lang_Throwable;
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			val exception = ctx.getLocals().<InstanceValue>load(0);
			val threadManager = vm.getThreadManager();
			val vmBacktrace = threadManager.currentThread().getBacktrace();
			val copy = new SimpleBacktrace();
			for (val frame : vmBacktrace) {
				val frameCtx = frame.getExecutionContext();
				if (frameCtx != null && Modifier.isHiddenFrame(frameCtx.getMethod().getAccess())) {
					continue;
				}
				copy.push(frame.freeze());
			}
			val backtrace = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object, copy);
			exception.setValue("backtrace", "Ljava/lang/Object;", backtrace);
			if (throwable.hasVirtualField("depth", "I")) {
				exception.setInt("depth", copy.count());
			}
			ctx.setResult(exception);
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceDepth", "()I", ctx -> {
			val backtrace = ((JavaValue<Backtrace>) ((InstanceValue) ctx.getLocals().load(0)).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			ctx.setResult(IntValue.of(backtrace.count()));
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceElement", "(I)Ljava/lang/StackTraceElement;", ctx -> {
			val locals = ctx.getLocals();
			val backtrace = ((JavaValue<Backtrace>) ((InstanceValue) locals.load(0)).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			int idx = locals.load(1).asInt();
			val helper = vm.getHelper();
			int len = backtrace.count();
			helper.rangeCheck(idx, 0, len);
			val element = helper.newStackTraceElement(backtrace.get(len - idx - 1), false);
			ctx.setResult(element);
			return Result.ABORT;
		});
	}
}
