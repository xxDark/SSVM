package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.util.VolatileBufferAccess;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Memory data backed by byte buffer.
 *
 * @author xDark
 */
@RequiredArgsConstructor
final class BufferMemoryData implements MemoryData {

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
		return volatileAccess().getLong(checkIndex(offset));
	}

	@Override
	public int readIntVolatile(long offset) {
		return volatileAccess().getInt(checkIndex(offset));
	}

	@Override
	public char readCharVolatile(long offset) {
		return volatileAccess().getChar(checkIndex(offset));
	}

	@Override
	public short readShortVolatile(long offset) {
		return volatileAccess().getShort(checkIndex(offset));
	}

	@Override
	public byte readByteVolatile(long offset) {
		return volatileAccess().getByte(checkIndex(offset));
	}

	@Override
	public void writeLongVolatile(long offset, long value) {
		volatileAccess().putLong(checkIndex(offset), value);
	}

	@Override
	public void writeIntVolatile(long offset, int value) {
		volatileAccess().putInt(checkIndex(offset), value);
	}

	@Override
	public void writeCharVolatile(long offset, char value) {
		volatileAccess().putChar(checkIndex(offset), value);
	}

	@Override
	public void writeShortVolatile(long offset, short value) {
		volatileAccess().putShort(checkIndex(offset), value);
	}

	@Override
	public void writeByteVolatile(long offset, byte value) {
		volatileAccess().putByte(checkIndex(offset), value);
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
			while($bytes != 0) {
				int len = Math.min($bytes, MEMSET_THRESHOLD);
				slice.put(buf, 0, len);
				$bytes -= len;
			}
		} else {
			while($bytes-- != 0) {
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
			while($bytes-- != 0) {
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

	private int checkIndex(long offset) {
		if (offset > buffer.limit() || offset < 0L) {
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
}
