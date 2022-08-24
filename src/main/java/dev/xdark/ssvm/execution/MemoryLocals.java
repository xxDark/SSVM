package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.ReferenceMap;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.value.ObjectValue;

public final class MemoryLocals implements Locals, Disposable {
	private final ReferenceMap referenceMap;
	final ThreadMemoryData threadMemoryData;

	public MemoryLocals(ReferenceMap referenceMap, ThreadMemoryData threadMemoryData) {
		this.referenceMap = referenceMap;
		this.threadMemoryData = threadMemoryData;
	}

	@Override
	public void setReference(int index, ObjectValue value) {
		region().writeLong(index * 8L, value.getMemory().getAddress());
	}

	@Override
	public void setLong(int index, long value) {
		region().writeLong(index * 8L, value);
	}

	@Override
	public void setDouble(int index, double value) {
		setLong(index, Double.doubleToRawLongBits(value));
	}

	@Override
	public void setInt(int index, int value) {
		setLong(index, value);
	}

	@Override
	public void setFloat(int index, float value) {
		setInt(index, Float.floatToRawIntBits(value));
	}

	@Override
	public <V extends ObjectValue> V loadReference(int index) {
		return (V) referenceMap.getReference(region().readLong(index * 8L));
	}

	@Override
	public long loadLong(int index) {
		return region().readLong(index * 8L);
	}

	@Override
	public double loadDouble(int index) {
		return Double.longBitsToDouble(loadLong(index));
	}

	@Override
	public int loadInt(int index) {
		return (int) loadLong(index);
	}

	@Override
	public float loadFloat(int index) {
		return Float.intBitsToFloat(loadInt(index));
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		MemoryData from = ((MemoryLocals) locals).region();
		from.read(srcOffset * 8L, region(), destOffset * 8L, length * 8);
	}

	@Override
	public int maxSlots() {
		return (int) (region().length() >>> 3L);
	}

	@Override
	public void dispose() {
		threadMemoryData.reclaim();
	}

	MemoryData region() {
		return threadMemoryData.data();
	}
}
