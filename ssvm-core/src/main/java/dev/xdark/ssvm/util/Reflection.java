package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
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
		while (true) {
			ExecutionContext<?> frame = backtrace.at(offset);
			if (frame == null) {
				return null;
			}
			JavaMethod method = frame.getMethod();
			if (method.isHidden()) {
				offset++;
			} else {
				return frame;
			}
		}
	}
}
