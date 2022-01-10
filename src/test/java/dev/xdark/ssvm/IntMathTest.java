package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.objectweb.asm.Opcodes.*;
import static org.junit.jupiter.api.Assertions.*;

public class IntMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testIntAdd() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a + b, doIntOp(a, b, IADD));
	}

	@Test
	public void testIntSub() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a - b, doIntOp(a, b, ISUB));
	}

	@Test
	public void testIntMul() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a * b, doIntOp(a, b, IMUL));
	}

	@Test
	public void testIntDiv() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a / b, doIntOp(a, b, IDIV));
	}

	@Test
	public void testIntRem() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a % b, doIntOp(a, b, IREM));
	}

	@Test
	public void testIntShl() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a << b, doIntOp(a, b, ISHL));
	}

	@Test
	public void testIntShr() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a >> b, doIntOp(a, b, ISHR));
	}

	@Test
	public void testIntUshr() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a >>> b, doIntOp(a, b, IUSHR));
	}

	@Test
	public void testIntAnd() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a & b, doIntOp(a, b, IAND));
	}

	@Test
	public void testIntOr() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a | b, doIntOp(a, b, IOR));
	}

	@Test
	public void testIntXor() {
		var a = nextInt();
		var b = nextInt();
		assertEquals(a ^ b, doIntOp(a, b, IXOR));
	}

	@Test
	public void testIntNeg() {
		var v = nextInt();
		assertEquals(-v, doIntOp(v, INEG));
	}

	private static int doIntOp(int a, int b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()I", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(2, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()I", new Value[0], new Value[0]);
		return result.getResult().asInt();
	}

	private static int doIntOp(int v, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()I", null, null);
		mv.visitLdcInsn(v);
		mv.visitInsn(opcode);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(1, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()I", new Value[0], new Value[0]);
		return result.getResult().asInt();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}
}
