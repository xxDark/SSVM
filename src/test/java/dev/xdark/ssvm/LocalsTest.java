package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.NullValue;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalsTest {
	
	@Test
	public void testEquality() {
		try (val locals1 = new Locals(6); val locals2 = new Locals(6)) {
			fillLocals(locals1);
			fillLocals(locals2);
			assertEquals(locals1, locals2);
		}
	}

	private static void fillLocals(Locals locals) {
		locals.set(0, IntValue.ONE);
		locals.setWide(1, LongValue.ONE);
		locals.set(3, IntValue.of(5));
		locals.setWide(4, new DoubleValue(6.7D));
	}
}
