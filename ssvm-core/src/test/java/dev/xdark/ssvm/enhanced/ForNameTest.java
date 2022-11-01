package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

public class ForNameTest {

	@Test
	public void doTest() {
		TestUtil.test(ForNameTest.class, true);
	}

	@VMTest
	private static void test() throws ClassNotFoundException {
		Class.forName(System.class.getName());
		Class.forName("[[B");
		Class.forName("[Ljava.lang.String;");
	}
}
