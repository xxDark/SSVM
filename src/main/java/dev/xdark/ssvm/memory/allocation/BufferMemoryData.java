package dev.xdark.ssvm.memory.allocation;

import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.util.VolatileBufferAccess;
import lombok.RequiredArgsConstructor;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Memory data backed by byte buffer.
 *
 * @author xDark
 */
@RequiredArgsConstructor
final class BufferMemoryData implements MemoryData {

	private static final Unsafe UNSAFE = UnsafeUtil.get();
	private static final int MEMSET_THRESHOLD = 256;
	private final ByteBuffer buffer;
	private VolatileBufferAccess volatileAccess;

	@Override
	public long readLong(long offset) {
		return buffer.getLong(validate(offset));
	}

	@Override
	public int readInt(long offset) {
		return buffer.getInt(validate(offset));
	}

	@Override
	public char readChar(long offset) {
		return buffer.getChar(validate(offset));
	}

	@Override
	public short readShort(long offset) {
		return buffer.getShort(validate(offset));
	}

	@Override
	public byte readByte(long offset) {
		return buffer.get(validate(offset));
	}

	@Override
	public void writeLong(long offset, long value) {
		buffer.putLong(validate(offset), value);
	}

	@Override
	public void writeInt(long offset, int value) {
		buffer.putInt(validate(offset), value);
	}

	@Override
	public void writeChar(long offset, char value) {
		buffer.putChar(validate(offset), value);
	}

	@Override
	public void writeShort(long offset, short value) {
		buffer.putShort(validate(offset), value);
	}

	@Override
	public void writeByte(long offset, byte value) {
		buffer.put(validate(offset), value);
	}

	@Override
	public long readLongVolatile(long offset) {
		return volatileAccess().getLong(checkIndex(offset, 8));
	}

	@Override
	public int readIntVolatile(long offset) {
		return volatileAccess().getInt(checkIndex(offset, 4));
	}

	@Override
	public char readCharVolatile(long offset) {
		return volatileAccess().getChar(checkIndex(offset, 2));
	}

	@Override
	public short readShortVolatile(long offset) {
		return volatileAccess().getShort(checkIndex(offset, 2));
	}

	@Override
	public byte readByteVolatile(long offset) {
		return volatileAccess().getByte(checkIndex(offset, 1));
	}

	@Override
	public void writeLongVolatile(long offset, long value) {
		volatileAccess().putLong(checkIndex(offset, 8), value);
	}

	@Override
	public void writeIntVolatile(long offset, int value) {
		volatileAccess().putInt(checkIndex(offset, 4), value);
	}

	@Override
	public void writeCharVolatile(long offset, char value) {
		volatileAccess().putChar(checkIndex(offset, 2), value);
	}

	@Override
	public void writeShortVolatile(long offset, short value) {
		volatileAccess().putShort(checkIndex(offset, 2), value);
	}

	@Override
	public void writeByteVolatile(long offset, byte value) {
		volatileAccess().putByte(checkIndex(offset, 1), value);
	}

	@Override
	public void set(long offset, long bytes, byte value) {
		ByteBuffer buffer = this.buffer;
		int $offset = validate(offset);
		int $bytes = validate(bytes);
		if ($bytes >= MEMSET_THRESHOLD) {
			byte[] buf = new byte[MEMSET_THRESHOLD];
			Arrays.fill(buf, value);
			ByteBuffer slice = buffer.slice().order(buffer.order());
			slice.position($offset);
			while ($bytes != 0) {
				int len = Math.min($bytes, MEMSET_THRESHOLD);
				slice.put(buf, 0, len);
				$bytes -= len;
			}
		} else {
			while ($bytes-- != 0) {
				buffer.put($offset++, value);
			}
		}
	}

	@Override
	public void copy(long srcOffset, MemoryData dst, long dstOffset, long bytes) {
		if (dst instanceof BufferMemoryData) {
			ByteBuffer dstBuf = ((BufferMemoryData) dst).buffer;
			int $srcOffset = validate(srcOffset);
			copyOrder(((ByteBuffer) dstBuf.slice().position(validate(dstOffset)))).put((ByteBuffer) buffer.slice().position($srcOffset).limit($srcOffset + validate(bytes)));
		} else {
			int start = validate(dstOffset);
			int $bytes = validate(bytes);
			int $offset = validate(srcOffset);
			ByteBuffer buffer = this.buffer;
			while ($bytes-- != 0) {
				dst.writeByte(start++, buffer.get($offset++));
			}
		}
	}

	@Override
	public void write(long dstOffset, ByteBuffer buffer) {
		copyOrder(((ByteBuffer) this.buffer.slice().position(validate(dstOffset)))).put(buffer);
	}

	@Override
	public void write(long dstOffset, byte[] array, int arrayOffset, int length) {
		copyOrder(((ByteBuffer) buffer.slice().position(validate(dstOffset)))).put(array, arrayOffset, length);
	}

	@Override
	public void write(long dstOffset, long[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 8);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_LONG_BASE_OFFSET + arrayOffset * 8L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 8L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putLong(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, double[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 8);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + arrayOffset * 8L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 8L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putDouble(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, int[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 4);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_INT_BASE_OFFSET + arrayOffset * 4L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 4L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putInt(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, float[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 4);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_FLOAT_BASE_OFFSET + arrayOffset * 4L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 4L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putFloat(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, char[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 2);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_CHAR_BASE_OFFSET + arrayOffset * 2L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 2L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putChar(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, short[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length * 2);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_SHORT_BASE_OFFSET + arrayOffset * 2L, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length * 2L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.putShort(array[arrayOffset++]);
			}
		}
	}

	@Override
	public void write(long dstOffset, boolean[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(dstOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(array, Unsafe.ARRAY_BOOLEAN_BASE_OFFSET + arrayOffset, data, Unsafe.ARRAY_BYTE_BASE_OFFSET + dstOffset + buffer.arrayOffset(), length);
		} else {
			buffer = buffer.slice().order(buffer.order());
			while (length-- != 0) {
				buffer.put((byte) (array[arrayOffset++] ? 1 : 0));
			}
		}
	}

	@Override
	public void read(long srcOffset, byte[] array, int arrayOffset, int length) {
		checkIndex(srcOffset, length);
		ByteBuffer buffer = this.buffer;
		buffer = buffer.slice();
		buffer.position((int) srcOffset);
		buffer.get(array, arrayOffset, length);
	}

	@Override
	public void read(long srcOffset, long[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_LONG_BASE_OFFSET + arrayOffset * 8L, length * 8L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getLong();
			}
		}
	}

	@Override
	public void read(long srcOffset, double[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + arrayOffset * 8L, length * 8L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getDouble();
			}
		}
	}

	@Override
	public void read(long srcOffset, int[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_INT_BASE_OFFSET + arrayOffset * 4L, length * 4L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getInt();
			}
		}
	}

	@Override
	public void read(long srcOffset, float[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_FLOAT_BASE_OFFSET + arrayOffset * 4L, length * 4L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getFloat();
			}
		}
	}

	@Override
	public void read(long srcOffset, char[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_CHAR_BASE_OFFSET + arrayOffset * 2L, length * 2L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getChar();
			}
		}
	}

	@Override
	public void read(long srcOffset, short[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_SHORT_BASE_OFFSET + arrayOffset * 2L, length * 2L);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.getShort();
			}
		}
	}

	@Override
	public void read(long srcOffset, boolean[] array, int arrayOffset, int length) {
		ByteBuffer buffer = this.buffer;
		checkIndex(srcOffset, length);
		if (fastAccess(buffer)) {
			byte[] data = buffer.array();
			UNSAFE.copyMemory(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, array, Unsafe.ARRAY_BOOLEAN_BASE_OFFSET + arrayOffset, length);
		} else {
			buffer = buffer.slice().order(buffer.order());
			buffer.position((int) srcOffset);
			while (length-- != 0) {
				array[arrayOffset++] = buffer.get() != 0;
			}
		}
	}

	@Override
	public long length() {
		return buffer.capacity();
	}

	@Override
	public MemoryData slice(long offset, long bytes) {
		int $offset = validate(offset);
		return MemoryData.buffer(copyOrder(((ByteBuffer) buffer.slice().position($offset).limit($offset + validate(bytes))).slice()));
	}

	@Override
	public void transferTo(MemoryData other) {
		if (other instanceof BufferMemoryData) {
			ByteBuffer buffer = ((BufferMemoryData) other).buffer;
			buffer.slice().put(this.buffer.slice());
			return;
		}
		ByteBuffer buffer = this.buffer;
		if (buffer.hasArray()) {
			other.write(0L, buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
		} else {
			byte[] tmp = new byte[buffer.remaining()];
			buffer.slice().get(tmp);
			other.write(0L, tmp, 0, tmp.length);
		}
	}

	// This is so stupid, calling ByteBuffer#slice()
	// resets buffer's byte order, copy it back
	private ByteBuffer copyOrder(ByteBuffer buffer) {
		return buffer.order(this.buffer.order());
	}

	private VolatileBufferAccess volatileAccess() {
		VolatileBufferAccess volatileAccess = this.volatileAccess;
		if (volatileAccess == null) {
			return this.volatileAccess = VolatileBufferAccess.wrap(buffer);
		}
		return volatileAccess;
	}

	private int checkIndex(long offset, int count) {
		if (offset + count > buffer.limit() || offset < 0L) {
			throw new PanicException("Segfault");
		}
		return (int) offset;
	}

	private static int validate(long offset) {
		if (offset > Integer.MAX_VALUE || offset < 0L) {
			throw new PanicException("Segfault");
		}
		return (int) offset;
	}

	private static boolean fastAccess(ByteBuffer buffer) {
		return buffer.hasArray() && isNativeOrder(buffer);
	}

	private static boolean isNativeOrder(ByteBuffer buffer) {
		return buffer.order() == ByteOrder.nativeOrder();
	}
}
