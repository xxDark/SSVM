package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.memory.allocation.MemoryData;

public interface ThreadMemoryData {

	MemoryData data();

	void reclaim();
}
