package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.value.IntValue;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.concurrent.ThreadLocalRandom;

public class UnsafeTest {

	@Test
	public void doTest() {
		TestUtil.test(InnerUnsafeTest.class, true, c -> {
			c.setStaticFieldValue("addressSize", "I", IntValue.of(c.getVM().getMemoryAllocator().addressSize()));
		});
	}

	private static final class InnerUnsafeTest {

		private static final Unsafe U = Unsafe.getUnsafe();
		private long field = -4215678911358L;
		private static int addressSize; // injected by the VM

		private static void testAddressSize() {
			if (U.addressSize() != addressSize) {
				throw new IllegalStateException(Integer.toString(U.addressSize()));
			}
		}

		@VMTest
		private static void testLong() {
			testAddressSize();
			long address = U.allocateMemory(8L);
			long v = System.currentTimeMillis();
			U.putLong(address, v);
			if (v != U.getLong(address)) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testLong2() {
			testAddressSize();
			long address = U.allocateMemory(8L);
			long v = System.currentTimeMillis();
			U.putLong(null, address, v);
			if (v != U.getLong(null, address)) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testLongWithByte() {
			testAddressSize();
			long address = U.allocateMemory(8L);
			long v = System.currentTimeMillis();
			U.putByte(address, (byte) v);
			U.putByte(address + 1, (byte) (v >> 8));
			U.putByte(address + 2, (byte) (v >> 16));
			U.putByte(address + 3, (byte) (v >> 24));
			U.putByte(address + 4, (byte) (v >> 32));
			U.putByte(address + 5, (byte) (v >> 40));
			U.putByte(address + 6, (byte) (v >> 48));
			U.putByte(address + 7, (byte) (v >> 56));
			long read = ((long) U.getByte(address + 7) << 56)
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
			testAddressSize();
			Unsafe unsafe = U;
			Object[] array = new Object[16];
			int base = Unsafe.ARRAY_OBJECT_BASE_OFFSET;
			for (int i = 0; i < 16; i++) {
				String str = Integer.toString(i);
				unsafe.putObject(array, base + i * 8L, str);
			}
			for (int i = 0; i < 16; i++) {
				String str = Integer.toString(i);
				if (!str.equals(unsafe.getObject(array, base + i * 8L)) || !str.equals(array[i])) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemorySet() {
			testAddressSize();
			Unsafe unsafe = U;
			byte v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			long a = unsafe.allocateMemory(64L);
			unsafe.setMemory(a, 64L, v);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(a + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemorySet2() {
			testAddressSize();
			Unsafe unsafe = U;
			byte v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			long a = unsafe.allocateMemory(1027L);
			unsafe.setMemory(a, 1027L, v);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(a + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testMemoryCopy() {
			testAddressSize();
			byte v = (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			Unsafe unsafe = U;
			long a = unsafe.allocateMemory(64L);
			unsafe.setMemory(a, 64L, v);
			long b = unsafe.allocateMemory(64L);
			unsafe.copyMemory(a, b, 64L);
			for (int i = 0; i < 64; i++) {
				if (unsafe.getByte(b + i) != v) {
					throw new IllegalStateException();
				}
			}
		}

		@VMTest
		private static void testRandomAccess() {
			testAddressSize();
			ThreadLocalRandom r = ThreadLocalRandom.current();
			byte v = (byte) r.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
			long addr = U.allocateMemory(64L);
			U.setMemory(addr, 64, v);
			int idx = r.nextInt(64);
			if (U.getByte(addr + idx) != v) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testAllocate() throws InstantiationException {
			testAddressSize();
			InnerUnsafeTest obj = (InnerUnsafeTest) U.allocateInstance(InnerUnsafeTest.class);
			if (obj.field != 0L) {
				throw new IllegalStateException();
			}
		}
	}
}
