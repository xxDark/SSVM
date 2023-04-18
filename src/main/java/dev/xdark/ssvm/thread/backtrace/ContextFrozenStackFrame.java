package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A frame that does not pull
 * line number information from execution context.
 *
 * @author xDark
 */
@Getter
@RequiredArgsConstructor
public class ContextFrozenStackFrame implements StackFrame {

	private final ExecutionContext<?> executionContext;
	private final int lineNumber;

	@Override
	public InstanceJavaClass getDeclaringClass() {
		return executionContext.getMethod().getOwner();
	}

	@Override
	public String getMethodName() {
		return executionContext.getMethod().getName();
	}

	@Override
	public String getSourceFile() {
		return getDeclaringClass().getNode().sourceFile;
	}

	@Override
	public StackFrame freeze() {
		return this;
	}
}
