package dev.xdark.ssvm.thread.heap;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.ReferenceMap;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Locals implementation that uses heap memory.
 *
 * @author xDark
 */
final class HeapLocals implements Locals, Disposable {
	private final ReferenceMap referenceMap;
	ThreadMemoryData threadMemoryData;

	/**
	 * @param referenceMap     Reference map.
	 * @param threadMemoryData Memory data.
	 */
	HeapLocals(ReferenceMap referenceMap, ThreadMemoryData threadMemoryData) {
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
		region().writeInt(index * 8L, value);
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
		return region().readInt(index * 8L);
	}

	@Override
	public float loadFloat(int index) {
		return Float.intBitsToFloat(loadInt(index));
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		MemoryData from = ((HeapLocals) locals).region();
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

	void reset(ThreadMemoryData data) {
		threadMemoryData = data;
	}
}
