package dev.xdark.ssvm.util;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

/**
 * Volatile methods for {@link ByteBuffer}.
 *
 * @author xDark
 */
public interface VolatileBufferAccess {

	/**
	 * Puts long at the specific offset.
	 *
	 * @param offset Data offset.
	 * @param value  Long value.
	 */
	void putLong(int offset, long value);

	/**
	 * Puts int at the specific offset.
	 *
	 * @param offset Data offset.
	 * @param value  Int value.
	 */
	void putInt(int offset, int value);

	/**
	 * Puts char at the specific offset.
	 *
	 * @param offset Data offset.
	 * @param value  Char value.
	 */
	void putChar(int offset, char value);

	/**
	 * Puts short at the specific offset.
	 *
	 * @param offset Data offset.
	 * @param value  Short value.
	 */
	void putShort(int offset, short value);

	/**
	 * Puts byte at the specific offset.
	 *
	 * @param offset Data offset.
	 * @param value  Byte value.
	 */
	void putByte(int offset, byte value);

	/**
	 * Reads long at the specific offset.
	 *
	 * @param offset Data offset.
	 * @return long value.
	 */
	long getLong(int offset);

	/**
	 * Reads int at the specific offset.
	 *
	 * @param offset Data offset.
	 * @return int value.
	 */
	int getInt(int offset);

	/**
	 * Reads char at the specific offset.
	 *
	 * @param offset Data offset.
	 * @return char value.
	 */
	char getChar(int offset);

	/**
	 * Reads short at the specific offset.
	 *
	 * @param offset Data offset.
	 * @return short value.
	 */
	short getShort(int offset);

	/**
	 * Reads byte at the specific offset.
	 *
	 * @param offset Data offset.
	 * @return byte value.
	 */
	byte getByte(int offset);

	/**
	 * Wraps byte buffer.
	 *
	 * @param buffer Byte buffer to wrap.
	 * @return interface providing volatile operations for this buffer.
	 */
	static VolatileBufferAccess wrap(ByteBuffer buffer) {
		if (buffer.hasArray()) {
			return new VolatileArrayAccess(buffer);
		}
		return new VolatileAddressAccess(buffer);
	}

	final class VolatileArrayAccess implements VolatileBufferAccess {

		private static final Unsafe U = UnsafeUtil.get();
		private final byte[] array;
		private final int offset;

		VolatileArrayAccess(ByteBuffer buffer) {
			array = buffer.array();
			offset = UnsafeUtil.ARRAY_BYTE_BASE_OFFSET + buffer.position() + buffer.arrayOffset();
		}

		@Override
		public void putLong(int offset, long value) {
			U.putLongVolatile(array, this.offset + offset, value);
		}

		@Override
		public void putInt(int offset, int value) {
			U.putIntVolatile(array, this.offset + offset, value);
		}

		@Override
		public void putChar(int offset, char value) {
			U.putCharVolatile(array, this.offset + offset, value);
		}

		@Override
		public void putShort(int offset, short value) {
			U.putShortVolatile(array, this.offset + offset, value);
		}

		@Override
		public void putByte(int offset, byte value) {
			U.putByteVolatile(array, this.offset + offset, value);
		}

		@Override
		public long getLong(int offset) {
			return U.getLongVolatile(array, this.offset + offset);
		}

		@Override
		public int getInt(int offset) {
			return U.getIntVolatile(array, this.offset + offset);
		}

		@Override
		public char getChar(int offset) {
			return U.getCharVolatile(array, this.offset + offset);
		}

		@Override
		public short getShort(int offset) {
			return U.getShortVolatile(array, this.offset + offset);
		}

		@Override
		public byte getByte(int offset) {
			return U.getByteVolatile(array, this.offset + offset);
		}
	}

	final class VolatileAddressAccess implements VolatileBufferAccess {

		private static final Unsafe U = UnsafeUtil.get();
		private final long address;

		VolatileAddressAccess(ByteBuffer buffer) {
			this.address = ((DirectBuffer) buffer).address();
		}

		@Override
		public void putLong(int offset, long value) {
			U.putLongVolatile(null, address + offset, value);
		}

		@Override
		public void putInt(int offset, int value) {
			U.putIntVolatile(null, address + offset, value);
		}

		@Override
		public void putChar(int offset, char value) {
			U.putCharVolatile(null, address + offset, value);
		}

		@Override
		public void putShort(int offset, short value) {
			U.putShortVolatile(null, address + offset, value);
		}

		@Override
		public void putByte(int offset, byte value) {
			U.putByteVolatile(null, address + offset, value);
		}

		@Override
		public long getLong(int offset) {
			return U.getLongVolatile(null, address + offset);
		}

		@Override
		public int getInt(int offset) {
			return U.getIntVolatile(null, address + offset);
		}

		@Override
		public char getChar(int offset) {
			return U.getCharVolatile(null, address + offset);
		}

		@Override
		public short getShort(int offset) {
			return U.getShortVolatile(null, address + offset);
		}

		@Override
		public byte getByte(int offset) {
			return U.getByteVolatile(null, address + offset);
		}
	}
}
