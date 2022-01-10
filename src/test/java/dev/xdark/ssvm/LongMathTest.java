package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class LongMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testLongAdd() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a + b, doLongOp(a, b, LADD));
	}

	@Test
	public void testLongSub() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a - b, doLongOp(a, b, LSUB));
	}

	@Test
	public void testLongMul() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a * b, doLongOp(a, b, LMUL));
	}

	@Test
	public void testLongDiv() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a / b, doLongOp(a, b, LDIV));
	}

	@Test
	public void testLongRem() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a % b, doLongOp(a, b, LREM));
	}

	@Test
	public void testLongShl() {
		var a = nextLong();
		var b = nextInt();
		assertEquals(a << b, doLongOp(a, b, LSHL));
	}

	@Test
	public void testLongShr() {
		var a = nextLong();
		var b = nextInt();
		assertEquals(a >> b, doLongOp(a, b, LSHR));
	}

	@Test
	public void testLongUshr() {
		var a = nextLong();
		var b = nextInt();
		assertEquals(a >>> b, doLongOp(a, b, LUSHR));
	}

	@Test
	public void testLongAnd() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a & b, doLongOp(a, b, LAND));
	}

	@Test
	public void testLongOr() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a | b, doLongOp(a, b, LOR));
	}

	@Test
	public void testLongXor() {
		var a = nextLong();
		var b = nextLong();
		assertEquals(a ^ b, doLongOp(a, b, LXOR));
	}

	@Test
	public void testLongNeg() {
		var v = nextLong();
		assertEquals(-v, doLongOp(v, LNEG));
	}

	private static long doLongOp(long a, long b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(4, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static long doLongOp(long a, int b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(3, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static long doLongOp(long v, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(v);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(2, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}
}
