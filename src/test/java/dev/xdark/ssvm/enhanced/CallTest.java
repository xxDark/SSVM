package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class CallTest {

	@Test
	public void doTest() {
		TestUtil.test(InnerCallTest.class, true);
	}
	
	private static final class InnerCallTest {
		
		private static int field1, field2;
		private static long field3;
		private static double field4;
		private static String field5;
		private static long field6;

		@VMTest
		private static void doCall1() {
			ThreadLocalRandom r = ThreadLocalRandom.current();
			field1 = r.nextInt();
			field2 = r.nextInt();
			field3 = r.nextLong();
			field4 = r.nextDouble();
			field5 = Long.toHexString(r.nextLong());
			field6 = r.nextLong(Long.MIN_VALUE, 0L);
			doCall1Impl(field1, field2, field3, field4, field5, field6);
		}

		private static void doCall1Impl(int a, int b, long c, double d, String e, long f) {
			if (a != InnerCallTest.field1) {
				throw new IllegalStateException();
			}
			if (b != InnerCallTest.field2) {
				throw new IllegalStateException();
			}
			if (c != InnerCallTest.field3) {
				throw new IllegalStateException();
			}
			if (d != InnerCallTest.field4) {
				throw new IllegalStateException();
			}
			if (!Objects.equals(e, InnerCallTest.field5)) {
				throw new IllegalStateException();
			}
			if (f != InnerCallTest.field6) {
				throw new IllegalStateException();
			}
		}
	}
}
