package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;

public class ExtTest {

	@Test
	public void doTest() {
		TestUtil.test(ExtTest.class, TestUtil.BOOTSTRAP | TestUtil.SYSTEM);
	}

	@VMTest
	private static void testEC() throws Exception {
		SecretKeyFactory.getInstance("DES");
	}
}
