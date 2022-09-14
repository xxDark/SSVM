package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;

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
		StackFrame frame = backtrace.get(count - offset++);
		JavaMethod caller = frame.getExecutionContext().getMethod();
		if (caller.isCallerSensitive()) {
			while (true) {
				frame = backtrace.get(count - offset);
				ExecutionContext<?> frameCtx = frame.getExecutionContext();
				if (frameCtx == null) {
					break;
				}
				JavaMethod method = frameCtx.getMethod();
				if (Modifier.isCallerSensitive(method.getModifiers())) {
					offset++;
				} else {
					break;
				}
			}
		}
		return frame;
	}
}
