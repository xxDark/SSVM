package dev.xdark.ssvm.memory.allocation;

import java.nio.ByteBuffer;

/**
 * Memory data slice.
 *
 * @author xDark
 */
public final class SliceMemoryData implements MemoryData {
	private MemoryData backing;
	private long offset;
	private long length;

	public SliceMemoryData(MemoryData backing, long offset, long length) {
		init(backing, offset, length);
	}

	public SliceMemoryData() {
	}

	public void init(MemoryData backing, long offset, long length) {
		this.backing = backing;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public long readLong(long offset) {
		return backing.readLong(offset(offset));
	}

	@Override
	public int readInt(long offset) {
		return backing.readInt(offset(offset));
	}

	@Override
	public char readChar(long offset) {
		return backing.readChar(offset(offset));
	}

	@Override
	public short readShort(long offset) {
		return backing.readShort(offset(offset));
	}

	@Override
	public byte readByte(long offset) {
		return backing.readByte(offset(offset));
	}

	@Override
	public void writeLong(long offset, long value) {
		backing.writeLong(offset(offset), value);
	}

	@Override
	public void writeInt(long offset, int value) {
		backing.writeInt(offset(offset), value);
	}

	@Override
	public void writeChar(long offset, char value) {
		backing.writeChar(offset(offset), value);
	}

	@Override
	public void writeShort(long offset, short value) {
		backing.writeShort(offset(offset), value);
	}

	@Override
	public void writeByte(long offset, byte value) {
		backing.writeByte(offset(offset), value);
	}

	@Override
	public long readLongVolatile(long offset) {
		return backing.readLongVolatile(offset(offset));
	}

	@Override
	public int readIntVolatile(long offset) {
		return backing.readIntVolatile(offset(offset));
	}

	@Override
	public char readCharVolatile(long offset) {
		return backing.readCharVolatile(offset(offset));
	}

	@Override
	public short readShortVolatile(long offset) {
		return backing.readShortVolatile(offset(offset));
	}

	@Override
	public byte readByteVolatile(long offset) {
		return backing.readByteVolatile(offset(offset));
	}

	@Override
	public void writeLongVolatile(long offset, long value) {
		backing.writeLongVolatile(offset(offset), value);
	}

	@Override
	public void writeIntVolatile(long offset, int value) {
		backing.writeIntVolatile(offset(offset), value);
	}

	@Override
	public void writeCharVolatile(long offset, char value) {
		backing.writeCharVolatile(offset(offset), value);
	}

	@Override
	public void writeShortVolatile(long offset, short value) {
		backing.writeShortVolatile(offset(offset), value);
	}

	@Override
	public void writeByteVolatile(long offset, byte value) {
		backing.writeByteVolatile(offset(offset), value);
	}

	@Override
	public void set(long offset, long bytes, byte value) {
		backing.set(offset(offset), bytes, value);
	}

	@Override
	public void copy(long srcOffset, MemoryData dst, long dstOffset, long bytes) {
		backing.copy(offset(srcOffset), dst, dstOffset, bytes);
	}

	@Override
	public void write(long offset, ByteBuffer buffer) {
		backing.write(offset(offset), buffer);
	}

	@Override
	public void write(long dstOffset, byte[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, long[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, double[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, int[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, float[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, char[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, short[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, boolean[] array, int arrayOffset, int length) {
		backing.write(offset(dstOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, byte[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, long[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, double[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, int[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, float[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, char[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, short[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, boolean[] array, int arrayOffset, int length) {
		backing.read(offset(srcOffset), array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, MemoryData data, long dataOffset, int length) {
		backing.read(offset(srcOffset), data, dataOffset, length);
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public MemoryData slice(long offset, long bytes) {
		return new SliceMemoryData(backing, offset(offset), bytes);
	}

	@Override
	public void transferTo(MemoryData other) {

	}

	private long offset(long pos) {
		return offset + pos;
	}
}
