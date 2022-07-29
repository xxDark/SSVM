package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.ThreadLocals;
import dev.xdark.ssvm.execution.ThreadStack;
import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.thread.ThreadRegion;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadStorageTest {

	@Test
	public void testSharedRegion() {
		int maxStack = 3;
		int maxLocals = 5;
		try (ThreadRegion region = SimpleThreadStorage.threadPush(maxStack + maxLocals)) {
			ThreadRegion stackRegion = region.slice(0, maxStack);
			ThreadRegion localsRegion = region.slice(maxStack, maxStack + maxLocals);
			Stack stack = new ThreadStack(stackRegion);
			Locals locals = new ThreadLocals(localsRegion);
			assertDoesNotThrow(() -> {
				stack.pushLong(0L);
				stack.pushInt(0);
			});
			assertDoesNotThrow(() -> {
				for (int i = 0; i < maxLocals; i++) {
					locals.set(i, IntValue.of(i));
				}
			});
			for (int i = 0; i < maxLocals; i++) {
				assertEquals(i, locals.load(i).asInt());
			}
			List<Value> view = stack.view();
			assertEquals(3, view.size());
			assertEquals(0L, view.get(0).asLong());
			assertEquals(TopValue.INSTANCE, view.get(1));
			assertEquals(0, view.get(2).asInt());
			stack.clear();
			assertTrue(stack.isEmpty());
			assertTrue(stack.view().isEmpty());
			assertThrows(IndexOutOfBoundsException.class, () -> locals.setInt(-1, -1));
		}
	}
}
