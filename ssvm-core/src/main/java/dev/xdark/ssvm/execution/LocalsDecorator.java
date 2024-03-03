package dev.xdark.ssvm.execution;

import org.jetbrains.annotations.NotNull;

public interface LocalsDecorator {
	@NotNull
	Locals decorateLocals(@NotNull Locals locals);
}
