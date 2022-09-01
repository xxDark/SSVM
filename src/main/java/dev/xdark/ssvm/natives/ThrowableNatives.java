package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.SimpleBacktrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Throwable.
 *
 * @author xDark
 */
@UtilityClass
public class ThrowableNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass throwable = symbols.java_lang_Throwable();
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			InstanceValue exception = ctx.getLocals().loadReference(0);
			ThreadManager threadManager = vm.getThreadManager();
			Backtrace vmBacktrace = threadManager.currentThread().getBacktrace();
			SimpleBacktrace copy = new SimpleBacktrace();
			for (StackFrame frame : vmBacktrace) {
				ExecutionContext<?> frameCtx = frame.getExecutionContext();
				if (frameCtx != null && Modifier.isHiddenFrame(frameCtx.getMethod().getModifiers())) {
					continue;
				}
				copy.push(frame.freeze());
			}
			JavaValue<SimpleBacktrace> backtrace = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object(), copy);
			vm.getPublicOperations().putReference(exception, "backtrace", "Ljava/lang/Object;", backtrace);
			long depth = exception.getFieldOffset("depth", "I");
			if (depth != -1L) {
				exception.getData().writeInt(depth, copy.count());
			}
			ctx.setResult(exception);
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceDepth", "()I", ctx -> {
			Backtrace backtrace = ((JavaValue<Backtrace>) vm.getTrustedOperations().getReference((ObjectValue) ctx.getLocals().loadReference(0), "backtrace", "Ljava/lang/Object;")).getValue();
			ctx.setResult(backtrace.count());
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceElement", "(I)Ljava/lang/StackTraceElement;", ctx -> {
			Locals locals = ctx.getLocals();
			Backtrace backtrace = ((JavaValue<Backtrace>) ((InstanceValue) locals.loadReference(0)).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			int idx = locals.loadInt(1);
			VMHelper helper = vm.getHelper();
			int len = backtrace.count();
			helper.rangeCheck(idx, 0, len);
			InstanceValue element = helper.newStackTraceElement(backtrace.get(len - idx - 1), false);
			ctx.setResult(element);
			return Result.ABORT;
		});
	}
}
