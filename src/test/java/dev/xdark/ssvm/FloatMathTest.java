package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class FloatMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test
	public void testFloatAdd() {
		float a = nextFloat();
		float b = nextFloat();
		assertEquals(a + b, doFloatOp(a, b, FADD));
	}

	@Test
	public void testFloatSub() {
		float a = nextFloat();
		float b = nextFloat();
		assertEquals(a - b, doFloatOp(a, b, FSUB));
	}

	@Test
	public void testFloatMul() {
		float a = nextFloat();
		float b = nextFloat();
		assertEquals(a * b, doFloatOp(a, b, FMUL));
	}

	@Test
	public void testFloatDiv() {
		float a = nextFloat();
		float b = nextFloat();
		assertEquals(a / b, doFloatOp(a, b, FDIV));
	}

	@Test
	public void testFloatRem() {
		float a = nextFloat();
		float b = nextFloat();
		assertEquals(a % b, doFloatOp(a, b, FREM));
	}

	private static float doFloatOp(float a, float b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()F", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(FRETURN);
		mv.visitMaxs(2, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invokeStatic(jc, "test", "()F", new Value[0], new Value[0]);
		return result.getResult().asFloat();
	}

	private static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}
}
