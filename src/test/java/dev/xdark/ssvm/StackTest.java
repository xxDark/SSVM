package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class StackTest {

	@Test
	public void testDup() {
		var stack = new Stack(2);
		stack.push(new IntValue(5));
		stack.dup();
		assertEquals(5, stack.pop().asInt());
		assertEquals(5, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDupX1() {
		var stack = new Stack(3);
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dupx1();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(1, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDupX2Form1() {
		var stack = new Stack(4);
		stack.push(new IntValue(3));
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dupx2();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(3, stack.pop().asInt());
		assertEquals(1, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDupX2Form2() {
		var stack = new Stack(4);
		stack.pushWide(new LongValue(2L));
		stack.push(new IntValue(1));
		stack.dupx2();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2L, stack.popWide().asLong());
		assertEquals(1, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2Form1() {
		var stack = new Stack(4);
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dup2();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2Form2() {
		var stack = new Stack(4);
		stack.pushWide(new LongValue(1L));
		stack.dup2();
		assertEquals(1L, stack.popWide().asLong());
		assertEquals(1L, stack.popWide().asLong());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X1Form1() {
		var stack = new Stack(5);
		stack.push(new IntValue(3));
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dup2x1();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(3, stack.pop().asInt());
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X1Form2() {
		var stack = new Stack(5);
		stack.push(new IntValue(2));
		stack.pushWide(new LongValue(1L));
		stack.dup2x1();
		assertEquals(1L, stack.popWide().asLong());
		assertEquals(2, stack.pop().asInt());
		assertEquals(1L, stack.popWide().asLong());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X2Form1() {
		var stack = new Stack(6);
		stack.push(new IntValue(4));
		stack.push(new IntValue(3));
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dup2x2();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(3, stack.pop().asInt());
		assertEquals(4, stack.pop().asInt());
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X2Form2() {
		var stack = new Stack(6);
		stack.push(new IntValue(3));
		stack.push(new IntValue(2));
		stack.pushWide(new LongValue(1L));
		stack.dup2x2();
		assertEquals(1L, stack.popWide().asLong());
		assertEquals(2, stack.pop().asInt());
		assertEquals(3, stack.pop().asInt());
		assertEquals(1L, stack.popWide().asLong());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X2Form3() {
		var stack = new Stack(6);
		stack.pushWide(new LongValue(3L));
		stack.push(new IntValue(2));
		stack.push(new IntValue(1));
		stack.dup2x2();
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertEquals(3L, stack.popWide().asLong());
		assertEquals(1, stack.pop().asInt());
		assertEquals(2, stack.pop().asInt());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testDup2X2Form4() {
		var stack = new Stack(6);
		stack.pushWide(new LongValue(2L));
		stack.pushWide(new LongValue(1L));
		stack.dup2x2();
		assertEquals(1L, stack.popWide().asLong());
		assertEquals(2L, stack.popWide().asLong());
		assertEquals(1L, stack.popWide().asLong());
		assertTrue(stack.isEmpty());
	}
}
