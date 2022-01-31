package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.SimpleMemory;
import dev.xdark.ssvm.mirror.JavaClass;

import java.nio.ByteBuffer;

/**
 * represents {@code null} value.
 *
 * @author xDark
 */
public final class NullValue extends ObjectValue {

	public static final NullValue INSTANCE = new NullValue();

	private NullValue() {
		super(new SimpleMemory(null, ByteBuffer.allocate(0), 0L, false));
	}

	@Override
	public <T> T as(Class<T> type) {
		return null;
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
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
		return null;
	}
}
