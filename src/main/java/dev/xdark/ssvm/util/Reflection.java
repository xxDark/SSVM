package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.StackFrame;

/**
 * Reflection utilities.
 *
 * @author xDark
 */
public final class Reflection {

	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public Reflection(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Returns caller frame.
	 *
	 * @param offset Caller offset.
	 * @return caller frame.
	 */
	public StackFrame getCallerFrame(int offset) {
		Backtrace backtrace = vm.currentThread().getBacktrace();
		int count = backtrace.count();
		JavaMethod caller = backtrace.get(count - offset++).getExecutionContext().getMethod();
		if (caller.isCallerSensitive()) {
			while (true) {
				StackFrame frame = backtrace.get(count - offset);
				ExecutionContext frameCtx = frame.getExecutionContext();
				if (frameCtx == null) {
					break;
				}
				JavaMethod method = frameCtx.getMethod();
				if (Modifier.isCallerSensitive(method.getAccess())) {
					offset++;
				} else {
					break;
				}
			}
		}
		return backtrace.get(count - offset);
	}
}
