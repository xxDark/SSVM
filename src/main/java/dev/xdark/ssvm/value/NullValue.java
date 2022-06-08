package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.MemoryData;
import dev.xdark.ssvm.memory.SimpleMemory;
import dev.xdark.ssvm.mirror.JavaClass;

import java.nio.ByteBuffer;

/**
 * represents {@code null} value.
 *
 * @author xDark
 */
public final class NullValue implements ObjectValue {

	public static final NullValue INSTANCE = new NullValue();
	private final Memory memory = new SimpleMemory(null, MemoryData.buffer(ByteBuffer.allocate(0)), 0L, false);

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
	public Memory getMemory() {
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

	@Override
	public long refCount() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public ReferenceCounted retain(long count) {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public ReferenceCounted retain() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public boolean release(long count) {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public boolean release() {
		throw new UnsupportedOperationException("Null value");
	}
}
