package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

// Java 8 imports
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

@EnabledOnJre(JRE.JAVA_8)
public class CallerTest {

	@Test
	public void doTest() {
		TestUtil.test(CallerTest.class, true);
	}

	@CallerSensitive
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

	@CallerSensitive
	private static void callerTestInner() {
		if (Reflection.getCallerClass() != CallerTest.class) {
			throw new IllegalStateException();
		}
	}
}
