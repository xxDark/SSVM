package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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
			Field field = InnerReflectionTest.class.getDeclaredField("value");
			if (field.getLong(null) != value) {
				throw new IllegalStateException();
			}
			long newValue = System.currentTimeMillis();
			field.setLong(null, newValue);
			if (field.getLong(null) != newValue) {
				throw new IllegalStateException();
			}
		}

		@VMTest
		private static void testString() throws NoSuchFieldException, IllegalAccessException {
			Field field = InnerReflectionTest.class.getDeclaredField("str");
			if (field.get(null) != str) {
				throw new IllegalStateException();
			}
			String newValue = UUID.randomUUID().toString();
			field.set(null, newValue);
			if (field.get(null) != newValue) {
				throw new IllegalStateException();
			}
		}

		static {
			ThreadLocalRandom r = ThreadLocalRandom.current();
			value = r.nextLong();
			byte[] bytes = new byte[32];
			r.nextBytes(bytes);
			str = new String(bytes, StandardCharsets.UTF_8);
		}
	}
}
