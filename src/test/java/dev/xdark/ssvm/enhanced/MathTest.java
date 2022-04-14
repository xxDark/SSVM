package dev.xdark.ssvm.enhanced;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class MathTest {

	@Test
	public void doTest() {
		TestUtil.test(MathTest.class, true);
	}

	@VMTest
	private static void testMin() {
		int a = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE - 1);
		int b = a + 1;
		if (Math.min(a, b) != a) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testMax() {
		int a = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE - 1);
		int b = a - 1;
		if (Math.max(a, b) != a) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testAbs() {
		val v = ThreadLocalRandom.current().nextDouble(0.0D, Double.MAX_VALUE - 1.0D);
		if (v != Math.abs(-v)) {
			throw new IllegalStateException();
		}
	}
}
