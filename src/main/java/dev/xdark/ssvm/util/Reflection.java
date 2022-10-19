package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.backtrace.Backtrace;

/**
 * Reflection utilities.
 *
 * @author xDark
 */
public final class Reflection {

	private final VirtualMachine vm;

	public Reflection(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Returns caller frame.
	 *
	 * @param offset Caller offset.
	 * @return caller frame.
	 */
	public ExecutionContext<?> getCallerFrame(int offset) {
		Backtrace backtrace = vm.getThreadManager().currentOsThread().getBacktrace();
		ExecutionContext<?> frame = backtrace.at(offset++);
		JavaMethod caller = frame.getMethod();
		if (caller.isCallerSensitive()) {
			while (true) {
				frame = backtrace.at(offset);
				JavaMethod method = frame.getMethod();
				if (method.isCallerSensitive()) {
					offset++;
				} else {
					break;
				}
			}
		}
		return frame;
	}
}
