package dev.xdark.ssvm.execution;

import org.jetbrains.annotations.NotNull;

public interface StackDecorator {
	@NotNull
	Stack decorateStack(@NotNull Stack stack);
}
