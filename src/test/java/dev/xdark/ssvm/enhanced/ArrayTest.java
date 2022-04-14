package dev.xdark.ssvm.enhanced;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ArrayTest {

	@Test
	public void doTest() {
		TestUtil.test(ArrayTest.class, true);
	}

	@VMTest
	private static void testArrays() {
		val array = new long[8];
		for (int i = 0; i < 8; i++) {
			int value = 8 - i;
			array[i] = value;
			if (array[i] != value) {
				throw new IllegalStateException();
			}
		}
	}

	@VMTest
	private static void testCopy() {
		val src = new long[8];
		for (int i = 0; i < 8; src[i] = i++) ;
		val dst = new long[8];
		System.arraycopy(src, 0, dst, 0, 8);
		if (!Arrays.equals(src, dst)) {
			throw new IllegalStateException();
		}
	}


	@VMTest
	private static void testCopyOf() {
		val array = new String[]{"1", "2", "3"};
		val copy = Arrays.copyOf(array, 1);
		if (copy.length != 1 || !"1".equals(copy[0])) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testClone() {
		val array = new long[32];
		for (int i = 0; i < 32; array[i++] = System.currentTimeMillis()) ;
		val clone = array.clone();
		if (!Arrays.equals(array, clone)) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testStoreException() {
		try {
			((Object[]) new String[1])[0] = 1;
			throw new IllegalStateException();
		} catch (ArrayStoreException ignored) {
		}
	}
	
	@VMTest
	private static void testOutOfBounds() {
		val array = new Object[0];
		try {
			val __ = array[-1];
			throw new IllegalStateException();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
		try {
			val __ = array[1];
			throw new IllegalStateException();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
	}
}
