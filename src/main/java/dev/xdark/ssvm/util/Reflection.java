package dev.xdark.ssvm.util;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;

/**
 * Reflection utilities.
 *
 * @author xDark
 */
public final class Reflection {

	private final ThreadManager threadManager;

	public Reflection(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}

	/**
	 * Returns caller frame.
	 *
	 * @param offset Caller offset.
	 * @return caller frame.
	 */
	public ExecutionContext<?> getCallerFrame(int offset) {
		Backtrace backtrace = threadManager.currentOsThread().getBacktrace();
		int count = backtrace.depth();
		ExecutionContext<?> frame = backtrace.at(count - offset++);
		JavaMethod caller = frame.getMethod();
		if (caller.isCallerSensitive()) {
			while (true) {
				frame = backtrace.at(count - offset);
				JavaMethod method = frame.getMethod();
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
