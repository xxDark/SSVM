package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ArrayTest {

	@Test
	public void doTest() {
		TestUtil.test(ArrayTest.class, true);
	}

	@dev.xdark.ssvm.VMTest
	private static void testArrays() {
		long[] array = new long[8];
		for (int i = 0; i < 8; i++) {
			int value = 8 - i;
			array[i] = value;
			if (array[i] != value) {
				throw new IllegalStateException();
			}
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testCopy() {
		long[] src = new long[8];
		for (int i = 0; i < 8; src[i] = i++) ;
		long[] dst = new long[8];
		System.arraycopy(src, 0, dst, 0, 8);
		if (!Arrays.equals(src, dst)) {
			throw new IllegalStateException();
		}
	}


	@dev.xdark.ssvm.VMTest
	private static void testCopyOf() {
		String[] array = new String[]{"1", "2", "3"};
		String[] copy = Arrays.copyOf(array, 1);
		if (copy.length != 1 || !"1".equals(copy[0])) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testClone() {
		long[] array = new long[32];
		for (int i = 0; i < 32; array[i++] = System.currentTimeMillis()) ;
		long[] clone = array.clone();
		if (!Arrays.equals(array, clone)) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testStoreException() {
		try {
			((Object[]) new String[1])[0] = 1;
			throw new IllegalStateException();
		} catch (ArrayStoreException ignored) {
		}
	}
	
	@VMTest
	private static void testOutOfBounds() {
		Object[] array = new Object[0];
		try {
			Object __ = array[-1];
			throw new IllegalStateException();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
		try {
			Object __ = array[1];
			throw new IllegalStateException();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
	}
}
