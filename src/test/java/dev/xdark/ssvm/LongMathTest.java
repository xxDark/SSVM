package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class LongMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test
	public void testLongAdd() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a + b, doLongOp(a, b, LADD));
	}

	@Test
	public void testLongSub() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a - b, doLongOp(a, b, LSUB));
	}

	@Test
	public void testLongMul() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a * b, doLongOp(a, b, LMUL));
	}

	@Test
	public void testLongDiv() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a / b, doLongOp(a, b, LDIV));
	}

	@Test
	public void testLongRem() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a % b, doLongOp(a, b, LREM));
	}

	@Test
	public void testLongShl() {
		long a = nextLong();
		int b = nextInt();
		assertEquals(a << b, doLongOp(a, b, LSHL));
	}

	@Test
	public void testLongShr() {
		long a = nextLong();
		int b = nextInt();
		assertEquals(a >> b, doLongOp(a, b, LSHR));
	}

	@Test
	public void testLongUshr() {
		long a = nextLong();
		int b = nextInt();
		assertEquals(a >>> b, doLongOp(a, b, LUSHR));
	}

	@Test
	public void testLongAnd() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a & b, doLongOp(a, b, LAND));
	}

	@Test
	public void testLongOr() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a | b, doLongOp(a, b, LOR));
	}

	@Test
	public void testLongXor() {
		long a = nextLong();
		long b = nextLong();
		assertEquals(a ^ b, doLongOp(a, b, LXOR));
	}

	@Test
	public void testLongNeg() {
		long v = nextLong();
		assertEquals(-v, doLongOp(v, LNEG));
	}

	private static long doLongOp(long a, long b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(4, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getPublicLinkResolver().resolveStaticMethod(jc, "test", "()J"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asLong();
	}

	private static long doLongOp(long a, int b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(3, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getPublicLinkResolver().resolveStaticMethod(jc, "test", "()J"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asLong();
	}

	private static long doLongOp(long v, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()J", null, null);
		mv.visitLdcInsn(v);
		mv.visitInsn(opcode);
		mv.visitInsn(LRETURN);
		mv.visitMaxs(2, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getPublicLinkResolver().resolveStaticMethod(jc, "test", "()J"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asLong();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}
}
