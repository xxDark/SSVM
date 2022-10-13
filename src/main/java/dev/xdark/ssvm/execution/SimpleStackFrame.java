package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.thread.backtrace.ContextFrozenStackFrame;
import dev.xdark.ssvm.thread.backtrace.StackFrame;

final class SimpleStackFrame implements StackFrame {
	private final ExecutionContext<?> context;

	SimpleStackFrame(ExecutionContext<?> context) {
		this.context = context;
	}

	@Override
	public InstanceClass getDeclaringClass() {
		return context.getOwner();
	}

	@Override
	public String getMethodName() {
		return context.getMethod().getName();
	}

	@Override
	public String getSourceFile() {
		return getDeclaringClass().getNode().sourceFile;
	}

	@Override
	public int getLineNumber() {
		return context.getLineNumber();
	}

	@Override
	public ExecutionContext<?> getExecutionContext() {
		return context;
	}

	@Override
	public StackFrame freeze() {
		ExecutionContext<?> ctx = context;
		return new ContextFrozenStackFrame(ctx, ctx.getLineNumber());
	}
}
