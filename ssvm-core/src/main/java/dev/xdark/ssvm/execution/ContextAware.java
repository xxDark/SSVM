package dev.xdark.ssvm.execution;

public interface ContextAware {
	default void preInterpret(ExecutionContext<?> ctx) {}

	default void postInterpret(ExecutionContext<?> ctx) {}
}
