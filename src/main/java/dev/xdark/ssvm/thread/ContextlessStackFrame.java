package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A frame that has no context attached to it.
 *
 * @author xDark
 */

@Getter
@RequiredArgsConstructor
public class ContextlessStackFrame implements StackFrame {

	private final InstanceJavaClass declaringClass;
	private final String methodName;
	private final String sourceFile;
	private final int lineNumber;

	@Override
	public ExecutionContext getExecutionContext() {
		return null;
	}

	@Override
	public StackFrame freeze() {
		return this;
	}
}
