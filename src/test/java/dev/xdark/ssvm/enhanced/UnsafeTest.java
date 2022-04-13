package dev.xdark.ssvm.enhanced;

import lombok.val;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.concurrent.ThreadLocalRandom;

public class UnsafeTest {

	@Test
	public void doTest() {
		TestUtil.test(InnerUnsafeTest.class, true);
	}

	private static final class InnerUnsafeTest {

		private static final Unsafe U = Unsafe.getUnsafe();
		private long field = -4215678911358L;

		@VMTest
		private static void testLong() {
			val address = U.allocateMemory(8L);
			val v = System.currentTimeMillis();
			U.putLong(address, v);
			if (v != U.getLong(address)) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testLong2() {
			val address = U.allocateMemory(8L);
			val v = System.currentTimeMillis();
			U.putLong(null, address, v);
			if (v != U.getLong(null, address)) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testLongWithByte() {
			val address = U.allocateMemory(8L);
			val v = System.currentTimeMillis();
			U.putByte(address, (byte) v);
			U.putByte(address + 1, (byte) (v >> 8));
			U.putByte(address + 2, (byte) (v >> 16));
			U.putByte(address + 3, (byte) (v >> 24));
			U.putByte(address + 4, (byte) (v >> 32));
			U.putByte(address + 5, (byte) (v >> 40));
			U.putByte(address + 6, (byte) (v >> 48));
			U.putByte(address + 7, (byte) (v >> 56));
			val read = ((long) U.getByte(address + 7) << 56)
					| ((long) U.getByte(address + 6) & 0xff) << 48
					| ((long) U.getByte(address + 5) & 0xff) << 40
					| ((long) U.getByte(address + 4) & 0xff) << 32
					| ((long) U.getByte(address + 3) & 0xff) << 24
					| ((long) U.getByte(address + 2) & 0xff) << 16
					| ((long) U.getByte(address + 1) & 0xff) << 8
					| ((long) U.getByte(address) & 0xff);
			if (v != read || v != U.getLong(address)) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testArray() {
			val unsafe = U;
			val array = new Object[16];
			val base = Unsafe.ARRAY_OBJECT_BASE_OFFSET;
			for (int i = 0; i < 16; i++) {
				val str = Integer.toString(i);
				unsafe.putObject(array, base + i * 8L, str);
			}
			for (int i = 0; i < 16; i++) {
				val str = Integer.toString(i);
				if (!str.equals(unsafe.getObject(array, base + i * 8L)) || !str.equals(array[i])) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemorySet() {
			val unsafe = U;
			val v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			val a = unsafe.allocateMemory(64L);
			unsafe.setMemory(a, 64L, v);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(a + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemorySet2() {
			val unsafe = U;
			val v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			val a = unsafe.allocateMemory(1027L);
			unsafe.setMemory(a, 1027L, v);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(a + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemoryCopy() {
			val v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			val unsafe = U;
			val a = unsafe.allocateMemory(64L);
			unsafe.setMemory(a, 64L, v);
			val b = unsafe.allocateMemory(64L);
			unsafe.copyMemory(a, b, 64L);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(b + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testRandomAccess() {
			val r = ThreadLocalRandom.current();
			val v = (byte) r.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			val addr = U.allocateMemory(64L);
			U.setMemory(addr, 64, v);
			val idx = r.nextInt(64);
			if (U.getByte(addr + idx) != v) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testAllocate() throws InstantiationException {
			val obj = (InnerUnsafeTest) U.allocateInstance(InnerUnsafeTest.class);
			if (obj.field != 0L) {
				throw new IllegalStateException();
			}
		}
	}
}
