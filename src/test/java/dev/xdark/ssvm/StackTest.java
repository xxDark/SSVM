package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.MemoryStack;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class StackTest {
	private static final int MAX_STACK_SIZE = 6;

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup(Stack stack) {
		stack.pushInt(5);
		stack.dup();
		assertEquals(5, stack.popInt());
		assertEquals(5, stack.popInt());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDupX1(Stack stack) {
		stack.pushInt(2);
		stack.pushInt(1);
		stack.dupx1();
		assertEquals(1, stack.popInt());
		assertEquals(2, stack.popInt());
		assertEquals(1, stack.popInt());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDupX2Form1(Stack stack) {
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

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDupX2Form2(Stack stack) {
		stack.pushLong(2L);
		stack.pushInt(1);
		stack.dupx2();
		assertEquals(1, stack.popInt());
		assertEquals(2L, stack.popLong());
		assertEquals(1, stack.popInt());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2Form1(Stack stack) {
		stack.pushInt(2);
		stack.pushInt(1);
		stack.dup2();
		assertEquals(1, stack.popInt());
		assertEquals(2, stack.popInt());
		assertEquals(1, stack.popInt());
		assertEquals(2, stack.popInt());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2Form2(Stack stack) {
		stack.pushLong(1L);
		stack.dup2();
		assertEquals(1L, stack.popLong());
		assertEquals(1L, stack.popLong());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X1Form1(Stack stack) {
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

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X1Form2(Stack stack) {
		stack.pushInt(2);
		stack.pushLong(1L);
		stack.dup2x1();
		assertEquals(1L, stack.popLong());
		assertEquals(2, stack.popInt());
		assertEquals(1L, stack.popLong());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X2Form1(Stack stack) {
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

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X2Form2(Stack stack) {
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

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X2Form3(Stack stack) {
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

	@MethodSource("stacks")
	@ParameterizedTest
	public void testDup2X2Form4(Stack stack) {
		stack.pushLong(2L);
		stack.pushLong(1L);
		stack.dup2x2();
		assertEquals(1L, stack.popLong());
		assertEquals(2L, stack.popLong());
		assertEquals(1L, stack.popLong());
		assertTrue(stack.isEmpty());
	}

	@MethodSource("stacks")
	@ParameterizedTest
	public void testSwap(Stack stack) {
		stack.pushInt(0);
		stack.pushInt(1);
		stack.swap();
		assertEquals(0, stack.popInt());
		assertEquals(1, stack.popInt());
		assertTrue(stack.isEmpty());
	}

	private static List<Stack> stacks() {
		MemoryData memory = MemoryData.buffer(ByteBuffer.allocate(MAX_STACK_SIZE * 8));
		return Collections.singletonList(new MemoryStack(null, new ThreadMemoryData() {
			@Override
			public MemoryData data() {
				return memory;
			}

			@Override
			public void reclaim() {

			}
		}));
	}
}
