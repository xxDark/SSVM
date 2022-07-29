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

public class DoubleMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test
	public void testDoubleAdd() {
		double a = nextDouble();
		double b = nextDouble();
		assertEquals(a + b, doDoubleOp(a, b, DADD));
	}

	@Test
	public void testDoubleSub() {
		double a = nextDouble();
		double b = nextDouble();
		assertEquals(a - b, doDoubleOp(a, b, DSUB));
	}

	@Test
	public void testDoubleMul() {
		double a = nextDouble();
		double b = nextDouble();
		assertEquals(a * b, doDoubleOp(a, b, DMUL));
	}

	@Test
	public void testDoubleDiv() {
		double a = nextDouble();
		double b = nextDouble();
		assertEquals(a / b, doDoubleOp(a, b, DDIV));
	}

	@Test
	public void testDoubleRem() {
		double a = nextDouble();
		double b = nextDouble();
		assertEquals(a % b, doDoubleOp(a, b, DREM));
	}

	private static double doDoubleOp(double a, double b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()D", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(DRETURN);
		mv.visitMaxs(4, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getPublicLinkResolver().resolveStaticMethod(jc, "test", "()D"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asDouble();
	}

	private static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}
}
