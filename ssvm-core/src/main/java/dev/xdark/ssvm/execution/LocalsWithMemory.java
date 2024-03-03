package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.thread.ThreadMemoryData;

public interface LocalsWithMemory extends Locals {
	MemoryData region();

	void reset(ThreadMemoryData data);
}
