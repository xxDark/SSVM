package dev.xdark.ssvm.thread.heap;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.ReferenceMap;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import dev.xdark.ssvm.util.SafeCloseable;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

/**
 * Stack implementation that uses heap memory.
 *
 * @author xDark
 */
public final class HeapStack implements Stack, SafeCloseable {
	private final ReferenceMap referenceMap;
	private ThreadMemoryData threadMemoryData;
	private long pointer;

	/**
	 * Visible only for testing.
	 *
	 * @param referenceMap     Reference map.
	 * @param threadMemoryData Memory data.
	 */
	public HeapStack(ReferenceMap referenceMap, ThreadMemoryData threadMemoryData) {
		this.referenceMap = referenceMap;
		this.threadMemoryData = threadMemoryData;
	}

	@Override
	public void pushLong(long value) {
		pushWide(value);
	}

	@Override
	public void pushDouble(double value) {
		pushWide(Double.doubleToRawLongBits(value));
	}

	@Override
	public void pushInt(int value) {
		pushNormal(value);
	}

	@Override
	public void pushFloat(float value) {
		pushInt(Float.floatToRawIntBits(value));
	}

	@Override
	public void pushReference(ObjectValue value) {
		pushNormal(value.getMemory().getAddress());
	}

	@Override
	public <V extends ObjectValue> V popReference() {
		return (V) referenceMap.getReference(popCategory1());
	}

	@Override
	public long popLong() {
		return popCategory2();
	}

	@Override
	public double popDouble() {
		return Double.longBitsToDouble(popCategory2());
	}

	@Override
	public int popInt() {
		return (int) popCategory1();
	}

	@Override
	public float popFloat() {
		return Float.intBitsToFloat(popInt());
	}

	@Override
	public char popChar() {
		return (char) popInt();
	}

	@Override
	public short popShort() {
		return (short) popInt();
	}

	@Override
	public byte popByte() {
		return (byte) popInt();
	}

	@Override
	public <V extends ObjectValue> V peekReference() {
		return (V) referenceMap.getReference(peekCategory1());
	}

	@Override
	public long peekLong() {
		return peekCategory2();
	}

	@Override
	public long peekLong(int topOffset) {
		return peekCategory2(topOffset);
	}

	@Override
	public double peekDouble() {
		return Double.longBitsToDouble(peekCategory2());
	}

	@Override
	public double peekDouble(int topOffset) {
		return Double.longBitsToDouble(peekCategory2(topOffset));
	}

	@Override
	public int peekInt() {
		return (int) peekCategory1();
	}

	@Override
	public int peekInt(int topOffset) {
		return (int) peekCategory1(topOffset);
	}

	@Override
	public float peekFloat() {
		return Float.intBitsToFloat((int) peekCategory1());
	}

	@Override
	public float peekFloat(int topOffset) {
		return Float.intBitsToFloat((int) peekCategory1(topOffset));
	}

	@Override
	public char peekChar() {
		return (char) peekCategory1();
	}

	@Override
	public char peekChar(int topOffset) {
		return (char) peekCategory1(topOffset);
	}

	@Override
	public short peekShort() {
		return (short) peekCategory1();
	}

	@Override
	public short peekShort(int topOffset) {
		return (short) peekCategory1(topOffset);
	}

	@Override
	public byte peekByte() {
		return (byte) peekCategory1();
	}

	@Override
	public byte peekByte(int topOffset) {
		return (byte) peekCategory1(topOffset);
	}

	@Override
	public void swap() {
		long v1 = popCategory1();
		long v2 = popCategory1();
		pushNormal(v1);
		pushNormal(v2);
	}

	@Override
	public void dup() {
		move(-1, 0);
		movePointer(1);
	}

	@Override
	public void dupx1() {
		move(-1, 0);
		move(-2, -1);
		move(0, -2);
		movePointer(1);
	}

	@Override
	public void dupx2() {
		move(-1, 0);
		move(-2, -1);
		move(-3, -2);
		move(0, -3);
		movePointer(1);
	}

	@Override
	public void dup2() {
		move(-2, 0);
		move(-1, 1);
		movePointer(2);
	}

	@Override
	public void dup2x1() {
		move(-1, 1);
		move(-2, 0);
		move(-3, -1);
		move(1, -2);
		move(0, -3);
		movePointer(2);
	}

	@Override
	public void dup2x2() {
		move(-1, 1);
		move(-2, 0);
		move(-3, -1);
		move(-4, -2);
		move(1, -3);
		move(0, -4);
		movePointer(2);
	}

	@Override
	public void pushGeneric(Value value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void pop() {
		movePointer(-1);
	}

	@Override
	public void clear() {
		pointer = 0L;
	}

	@Override
	public int position() {
		return (int) (pointer >>> 3L);
	}

	@Override
	public boolean isEmpty() {
		return pointer == 0L;
	}

	@Override
	public <V extends ObjectValue> V getReferenceAt(int index) {
		return (V) referenceMap.getReference(region().readLong(index * 8L));
	}

	@Override
	public long getLongAt(int index) {
		return region().readLong(index * 8L);
	}

	@Override
	public double getDoubleAt(int index) {
		return Double.longBitsToDouble(getLongAt(index));
	}

	@Override
	public int getIntAt(int index) {
		return region().readInt(index * 8L);
	}

	@Override
	public float getFloatAt(int index) {
		return Float.intBitsToFloat(getIntAt(index));
	}

	@Override
	public char getCharAt(int index) {
		return region().readChar(index * 8L);
	}

	@Override
	public short getShortAt(int index) {
		return region().readShort(index * 8L);
	}

	@Override
	public byte getByteAt(int index) {
		return region().readByte(index * 8L);
	}

	@Override
	public void sinkInto(Locals locals, int count) {
		if (count == 0) {
			return;
		}
		long pointer = this.pointer - count * 8L;
		region().read(pointer, ((HeapLocals) locals).region(), 0L, count * 8);
		this.pointer = pointer;
	}

	@Override
	public void sinkInto(Locals locals, int dst, int count) {
		if (count == 0) {
			return;
		}
		long pointer = this.pointer - count * 8L;
		region().read(pointer, ((HeapLocals) locals).region(), dst * 8L, count * 8);
		this.pointer = pointer;
	}

	@Override
	public void acceptReference(ObjectValue value) {
		pushReference(value);
	}

	@Override
	public void acceptLong(long value) {
		pushLong(value);
	}

	@Override
	public void acceptDouble(double value) {
		pushDouble(value);
	}

	@Override
	public void acceptInt(int value) {
		pushInt(value);
	}

	@Override
	public void acceptFloat(float value) {
		pushFloat(value);
	}

	@Override
	public void close() {
		threadMemoryData.reclaim();
	}

	private long peekCategory1() {
		return region().readLong(pointer - 8L);
	}

	private long peekCategory1(int topOffset) {
		return region().readLong(pointer - 8L - (8L * topOffset));
	}

	private long popCategory1() {
		long value = peekCategory1();
		movePointer(-1);
		return value;
	}

	private long peekCategory2() {
		return region().readLong(pointer - 16L);
	}

	private long peekCategory2(int topOffset) {
		return region().readLong(pointer - 16L - (8L * topOffset));
	}

	private long popCategory2() {
		long value = peekCategory2();
		movePointer(-2);
		return value;
	}

	private void pushNormal(long value) {
		region().writeLong(pointer, value);
		movePointer(1);
	}

	private void pushWide(long value) {
		region().writeLong(pointer, value);
		movePointer(2);
	}

	private void move(int from, int to) {
		MemoryData region = region();
		long pointer = this.pointer;
		region.writeLong(pointer + 8L * to, region.readLong(pointer + 8L * from));
	}

	private void movePointer(int count) {
		pointer += count * 8L;
	}

	private MemoryData region() {
		return threadMemoryData.data();
	}

	void reset(ThreadMemoryData data) {
		threadMemoryData = data;
		pointer = 0L;
	}
}
