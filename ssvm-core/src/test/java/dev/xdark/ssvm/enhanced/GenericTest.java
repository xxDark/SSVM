package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class GenericTest {

	private long v1, v2, v3;
	private int v4, v5, v6;

	@Test
	public void doTest() {
		TestUtil.test(GenericTest.class, true);
	}

	@dev.xdark.ssvm.VMTest
	private static void testClone() throws CloneNotSupportedException {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		GenericTest obj = new GenericTest();
		obj.v1 = r.nextLong();
		obj.v2 = r.nextLong();
		obj.v3 = r.nextLong();
		obj.v4 = r.nextInt();
		obj.v5 = r.nextInt();
		obj.v6 = r.nextInt();
		GenericTest copy = (GenericTest) obj.clone();
		if (obj.v1 != copy.v1 || obj.v2 != copy.v2 || obj.v3 != copy.v3 || obj.v4 != copy.v4 || obj.v5 != copy.v5 || obj.v6 != copy.v6) {
			throw new IllegalStateException();
		}
	}
	
	@VMTest
	private static void testCastException() {
		String s = "Hello, World";
		try {
			Integer unused = (Integer) (Object) s;
			throw new IllegalStateException();
		} catch (ClassCastException ignored) {}
	}
}
