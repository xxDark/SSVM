package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultSynchronizationOperations implements SynchronizationOperations {

	private final Symbols symbols;
	private final MemoryManager memoryManager;
	private final VMOperations ops;

	@Override
	public void monitorEnter(ObjectValue value) {
		ops.checkNotNull(value);
		Mutex mutex = memoryManager.getMutex(value);
		mutex.lock();
	}

	@Override
	public void monitorExit(ObjectValue value) {
		ops.checkNotNull(value);
		Mutex mutex = memoryManager.getMutex(value);
		if (!mutex.tryUnlock()) {
			ops.throwException(symbols.java_lang_IllegalMonitorStateException());
		}
	}
}
