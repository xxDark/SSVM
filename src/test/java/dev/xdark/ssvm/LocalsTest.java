package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.ThreadLocals;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalsTest {
	
	@Test
	public void testEquality() {
		try (ThreadLocals locals1 = new ThreadLocals(6);
			 ThreadLocals locals2 = new ThreadLocals(6)) {
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
