package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

public class CharacterTest {

	@Test
	public void doTest() {
		TestUtil.test(CharacterTest.class, true);
	}

	@VMTest
	private static void testLowerCase() {
		for (char c = 'A'; c <= 'Z'; c++) {
			char lowerCase = (char) ('a' + (c - 'A'));
			if (Character.toLowerCase(c) != lowerCase || Character.toLowerCase((int) c) != lowerCase) {
				throw new IllegalStateException();
			}
		}
	}

	@VMTest
	private static void toUpperCase() {
		for (char c = 'a'; c <= 'z'; c++) {
			char upperCase = (char) ('A' + (c - 'a'));
			if (Character.toUpperCase(c) != upperCase || Character.toUpperCase((int) c) != upperCase) {
				throw new IllegalStateException();
			}
		}
	}
}
