package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * represents {@code null} value.
 *
 * @author xDark
 */
public final class NullValue implements ObjectValue {

	private final MemoryBlock memory;

	public NullValue(MemoryBlock memory) {
		this.memory = memory;
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public boolean isVoid() {
		return false;
	}

	@Override
	public JavaClass getJavaClass() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public MemoryBlock getMemory() {
		return memory;
	}

	@Override
	public void monitorEnter() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public void monitorExit() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public void monitorWait(long timeoutMillis) throws InterruptedException {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public void monitorNotify() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public void monitorNotifyAll() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public boolean isHeldByCurrentThread() {
		throw new UnsupportedOperationException("Null value");
	}
}
