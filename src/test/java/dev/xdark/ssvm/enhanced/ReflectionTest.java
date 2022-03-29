package dev.xdark.ssvm.enhanced;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ReflectionTest {

	@Test
	public void doTest() {
		TestUtil.test(InnerReflectionTest.class, true);
	}

	private static final class InnerReflectionTest {

		private static long value;
		private static String str;

		@VMTest
		private static void testLong() throws NoSuchFieldException, IllegalAccessException {
			val field = InnerReflectionTest.class.getDeclaredField("value");
			if (field.getLong(null) != value) {
				throw new IllegalStateException();
			}
			val newValue = System.currentTimeMillis();
			field.setLong(null, newValue);
			if (field.getLong(null) != newValue) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testString() throws NoSuchFieldException, IllegalAccessException {
			val field = InnerReflectionTest.class.getDeclaredField("str");
			if (field.get(null) != str) {
				throw new IllegalStateException();
			}
			val newValue = UUID.randomUUID().toString();
			field.set(null, newValue);
			if (field.get(null) != newValue) {
				throw new IllegalStateException();
			}
		}

		static {
			val r = ThreadLocalRandom.current();
			value = r.nextLong();
			val bytes = new byte[32];
			r.nextBytes(bytes);
			str = new String(bytes, StandardCharsets.UTF_8);
		}
	}
}
