package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import sun.reflect.Reflection;

@EnabledOnJre(JRE.JAVA_8)
public class CallerTest {

	@Test
	public void doTest() {
		TestUtil.test(CallerTest.class, true);
	}

	@VMTest
	private static void callerTest() {
		if (Reflection.getCallerClass(0) != Reflection.class) {
			throw new IllegalStateException();
		}
		if (Reflection.getCallerClass(1) != CallerTest.class) {
			throw new IllegalStateException();
		}
		callerTestInner();
	}

	private static void callerTestInner() {
		if (Reflection.getCallerClass() != CallerTest.class) {
			throw new IllegalStateException();
		}
	}
}
