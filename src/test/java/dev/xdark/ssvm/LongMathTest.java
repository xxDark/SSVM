package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import lombok.val;
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
		val a = nextLong();
		val b = nextLong();
		assertEquals(a + b, doLongOp(a, b, LADD));
	}

	@Test
	public void testLongSub() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a - b, doLongOp(a, b, LSUB));
	}

	@Test
	public void testLongMul() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a * b, doLongOp(a, b, LMUL));
	}

	@Test
	public void testLongDiv() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a / b, doLongOp(a, b, LDIV));
	}

	@Test
	public void testLongRem() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a % b, doLongOp(a, b, LREM));
	}

	@Test
	public void testLongShl() {
		val a = nextLong();
		val b = nextInt();
		assertEquals(a << b, doLongOp(a, b, LSHL));
	}

	@Test
	public void testLongShr() {
		val a = nextLong();
		val b = nextInt();
		assertEquals(a >> b, doLongOp(a, b, LSHR));
	}

	@Test
	public void testLongUshr() {
		val a = nextLong();
		val b = nextInt();
		assertEquals(a >>> b, doLongOp(a, b, LUSHR));
	}

	@Test
	public void testLongAnd() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a & b, doLongOp(a, b, LAND));
	}

	@Test
	public void testLongOr() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a | b, doLongOp(a, b, LOR));
	}

	@Test
	public void testLongXor() {
		val a = nextLong();
		val b = nextLong();
		assertEquals(a ^ b, doLongOp(a, b, LXOR));
	}

	@Test
	public void testLongNeg() {
		val v = nextLong();
		assertEquals(-v, doLongOp(v, LNEG));
	}

	private static long doLongOp(long a, long b, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "dev.Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(4, 0);
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static long doLongOp(long a, int b, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "dev.Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(3, 0);
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static long doLongOp(long v, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "dev.Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(v);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(2, 0);
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()J", new Value[0], new Value[0]);
		return result.getResult().asLong();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}
}
