package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class FloatMathTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testFloatAdd() {
		val a = nextFloat();
		val b = nextFloat();
		assertEquals(a + b, doFloatOp(a, b, FADD));
	}

	@Test
	public void testFloatSub() {
		val a = nextFloat();
		val b = nextFloat();
		assertEquals(a - b, doFloatOp(a, b, FSUB));
	}

	@Test
	public void testFloatMul() {
		val a = nextFloat();
		val b = nextFloat();
		assertEquals(a * b, doFloatOp(a, b, FMUL));
	}

	@Test
	public void testFloatDiv() {
		val a = nextFloat();
		val b = nextFloat();
		assertEquals(a / b, doFloatOp(a, b, FDIV));
	}

	@Test
	public void testFloatRem() {
		val a = nextFloat();
		val b = nextFloat();
		assertEquals(a % b, doFloatOp(a, b, FREM));
	}

	private static float doFloatOp(float a, float b, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "dev.Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()F", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(FRETURN);
		mv.visitMaxs(2, 0);
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()F", new Value[0], new Value[0]);
		return result.getResult().asFloat();
	}

	private static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}
}
