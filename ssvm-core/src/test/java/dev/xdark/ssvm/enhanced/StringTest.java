package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import org.junit.jupiter.api.Test;

public class StringTest {

	@Test
	public void doTest() {
		TestUtil.test(StringTest.class, true);
	}

	@dev.xdark.ssvm.VMTest
	private static void testContains() {
		if (!"Hello World".contains("World")) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testIndexOf() {
		String str = "A B C D E F";
		int idx = 0;
		for (int i = 0; i < 6; i++) {
			if (str.charAt(idx) != ('A' + i)) {
				throw new IllegalStateException();
			}
			idx += 2;
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testIndexOf2() {
		String str = "Random text";
		if (str.indexOf("text") != 7) {
			throw new IllegalStateException();
		}
		if (str.indexOf("texT") != -1) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testLastIndexOf() {
		String str = "part1 part2 part3 part1";
		if (str.lastIndexOf("part1") != 18) {
			throw new IllegalStateException();
		}
		if (str.lastIndexOf("parT1") != -1) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testLength() {
		String str = "string to check length for";
		if (str.length() != 26) {
			throw new IllegalStateException();
		}
	}
}
