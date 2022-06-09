package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.ThreadStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class StackTest {

	@Test
	public void testDup() {
		try (ThreadStack stack = new ThreadStack(2)) {
			stack.pushInt(5);
			stack.dup();
			assertEquals(5, stack.popInt());
			assertEquals(5, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDupX1() {
		try (ThreadStack stack = new ThreadStack(3)) {
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dupx1();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(1, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDupX2Form1() {
		try (ThreadStack stack = new ThreadStack(4)) {
			stack.pushInt(3);
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dupx2();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(3, stack.popInt());
			assertEquals(1, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDupX2Form2() {
		try (ThreadStack stack = new ThreadStack(4)) {
			stack.pushLong(2L);
			stack.pushInt(1);
			stack.dupx2();
			assertEquals(1, stack.popInt());
			assertEquals(2L, stack.popLong());
			assertEquals(1, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2Form1() {
		try (ThreadStack stack = new ThreadStack(4)) {
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dup2();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2Form2() {
		try (ThreadStack stack = new ThreadStack(4)) {
			stack.pushLong(1L);
			stack.dup2();
			assertEquals(1L, stack.popLong());
			assertEquals(1L, stack.popLong());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X1Form1() {
		try (ThreadStack stack = new ThreadStack(5)) {
			stack.pushInt(3);
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dup2x1();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(3, stack.popInt());
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X1Form2() {
		try (ThreadStack stack = new ThreadStack(5)) {
			stack.pushInt(2);
			stack.pushLong(1L);
			stack.dup2x1();
			assertEquals(1L, stack.popLong());
			assertEquals(2, stack.popInt());
			assertEquals(1L, stack.popLong());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X2Form1() {
		try (ThreadStack stack = new ThreadStack(6)) {
			stack.pushInt(4);
			stack.pushInt(3);
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dup2x2();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(3, stack.popInt());
			assertEquals(4, stack.popInt());
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X2Form2() {
		try (ThreadStack stack = new ThreadStack(6)) {
			stack.pushInt(3);
			stack.pushInt(2);
			stack.pushLong(1L);
			stack.dup2x2();
			assertEquals(1L, stack.popLong());
			assertEquals(2, stack.popInt());
			assertEquals(3, stack.popInt());
			assertEquals(1L, stack.popLong());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X2Form3() {
		try (ThreadStack stack = new ThreadStack(6)) {
			stack.pushLong(3L);
			stack.pushInt(2);
			stack.pushInt(1);
			stack.dup2x2();
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertEquals(3L, stack.popLong());
			assertEquals(1, stack.popInt());
			assertEquals(2, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testDup2X2Form4() {
		try (ThreadStack stack = new ThreadStack(6)) {
			stack.pushLong(2L);
			stack.pushLong(1L);
			stack.dup2x2();
			assertEquals(1L, stack.popLong());
			assertEquals(2L, stack.popLong());
			assertEquals(1L, stack.popLong());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testSwap() {
		try (ThreadStack stack = new ThreadStack(2)) {
			stack.pushInt(0);
			stack.pushInt(1);
			stack.swap();
			assertEquals(0, stack.popInt());
			assertEquals(1, stack.popInt());
			assertTrue(stack.isEmpty());
		}
	}

	@Test
	public void testEquality() {
		try (ThreadStack stack1 = new ThreadStack(8);
			 ThreadStack stack2 = new ThreadStack(8)) {
			filLStack(stack1);
			filLStack(stack2);
			assertEquals(stack1, stack2);
		}
	}
	
	private static void filLStack(Stack stack) {
		stack.pushInt(1);
		stack.pushLong(0L);
		stack.pushDouble(1.3D);
		stack.pushLong(1L);
	}
}
