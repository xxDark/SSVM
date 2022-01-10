package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
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
		var a = nextFloat();
		var b = nextFloat();
		assertEquals(a + b, doFloatOp(a, b, FADD));
	}

	@Test
	public void testFloatSub() {
		var a = nextFloat();
		var b = nextFloat();
		assertEquals(a - b, doFloatOp(a, b, FSUB));
	}

	@Test
	public void testFloatMul() {
		var a = nextFloat();
		var b = nextFloat();
		assertEquals(a * b, doFloatOp(a, b, FMUL));
	}

	@Test
	public void testFloatDiv() {
		var a = nextFloat();
		var b = nextFloat();
		assertEquals(a / b, doFloatOp(a, b, FDIV));
	}

	@Test
	public void testFloatRem() {
		var a = nextFloat();
		var b = nextFloat();
		assertEquals(a % b, doFloatOp(a, b, FREM));
	}

	private static float doFloatOp(float a, float b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()F", null, null);
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(opcode);
		mv.visitInsn(FRETURN);
		mv.visitMaxs(2, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()F", new Value[0], new Value[0]);
		return result.getResult().asFloat();
	}

	private static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}
}
