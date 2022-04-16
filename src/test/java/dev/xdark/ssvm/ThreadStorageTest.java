package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.TopValue;
import lombok.val;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class ThreadStorageTest {

	@Test
	public void testSharedRegion() {
		int maxStack = 3;
		int maxLocals = 5;
		try (val region = SimpleThreadStorage.threadPush(maxStack + maxLocals)) {
			val stackRegion = region.slice(0, maxStack);
			val localsRegion = region.slice(maxStack, maxStack + maxLocals);
			val stack = new Stack(stackRegion);
			val locals = new Locals(localsRegion);
			assertDoesNotThrow(() -> {
				stack.pushWide(LongValue.ZERO);
				stack.push(IntValue.ZERO);
			});
			assertDoesNotThrow(() -> {
				for (int i = 0; i < maxLocals; i++) {
					locals.set(i, IntValue.of(i));
				}
			});
			for (int i = 0; i < maxLocals; i++) {
				assertEquals(i, locals.load(i).asInt());
			}
			val view = stack.view();
			assertEquals(3, view.size());
			assertEquals(0L, view.get(0).asLong());
			assertEquals(TopValue.INSTANCE, view.get(1));
			assertEquals(0, view.get(2).asInt());
			stack.clear();
			assertTrue(stack.isEmpty());
			assertTrue(stack.view().isEmpty());
			assertThrows(IndexOutOfBoundsException.class, () -> locals.set(-1, IntValue.M_ONE));
		}
	}
}
