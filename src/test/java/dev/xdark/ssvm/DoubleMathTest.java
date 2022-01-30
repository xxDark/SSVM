package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class DoubleMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testDoubleAdd() {
		val a = nextDouble();
		val b = nextDouble();
		assertEquals(a + b, doDoubleOp(a, b, DADD));
	}

	@Test
	public void testDoubleSub() {
		val a = nextDouble();
		val b = nextDouble();
		assertEquals(a - b, doDoubleOp(a, b, DSUB));
	}

	@Test
	public void testDoubleMul() {
		val a = nextDouble();
		val b = nextDouble();
		assertEquals(a * b, doDoubleOp(a, b, DMUL));
	}

	@Test
	public void testDoubleDiv() {
		val a = nextDouble();
		val b = nextDouble();
		assertEquals(a / b, doDoubleOp(a, b, DDIV));
	}

	@Test
	public void testDoubleRem() {
		val a = nextDouble();
		val b = nextDouble();
		assertEquals(a % b, doDoubleOp(a, b, DREM));
	}

	private static double doDoubleOp(double a, double b, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "dev.Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()D", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(DRETURN);
		mv.visitMaxs(4, 0);
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()D", new Value[0], new Value[0]);
		return result.getResult().asDouble();
	}

	private static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}
}
