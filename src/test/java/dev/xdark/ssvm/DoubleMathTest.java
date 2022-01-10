package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
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
		var a = nextDouble();
		var b = nextDouble();
		assertEquals(a + b, doDoubleOp(a, b, DADD));
	}

	@Test
	public void testDoubleSub() {
		var a = nextDouble();
		var b = nextDouble();
		assertEquals(a - b, doDoubleOp(a, b, DSUB));
	}

	@Test
	public void testDoubleMul() {
		var a = nextDouble();
		var b = nextDouble();
		assertEquals(a * b, doDoubleOp(a, b, DMUL));
	}

	@Test
	public void testDoubleDiv() {
		var a = nextDouble();
		var b = nextDouble();
		assertEquals(a / b, doDoubleOp(a, b, DDIV));
	}

	@Test
	public void testDoubleRem() {
		var a = nextDouble();
		var b = nextDouble();
		assertEquals(a % b, doDoubleOp(a, b, DREM));
	}

	private static double doDoubleOp(double a, double b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()D", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(DRETURN);
		mv.visitMaxs(4, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()D", new Value[0], new Value[0]);
		return result.getResult().asDouble();
	}

	private static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}
}
