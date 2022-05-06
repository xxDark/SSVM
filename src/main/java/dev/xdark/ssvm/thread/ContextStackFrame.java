package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Stack frame that has context attached to it.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public class ContextStackFrame implements StackFrame {

	@Getter
	private final ExecutionContext executionContext;

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
	public int getLineNumber() {
		return executionContext.getLineNumber();
	}

	@Override
	public StackFrame freeze() {
		ExecutionContext executionContext = this.executionContext;
		return new ContextFrozenStackFrame(executionContext, executionContext.getLineNumber());
	}
}
