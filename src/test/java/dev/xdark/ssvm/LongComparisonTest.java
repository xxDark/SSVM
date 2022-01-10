package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class LongComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test // a < b (< 0)
	public void test_LCMP_LT() {
		var a = nextLong();
		var b = a + 1;
		assertTrue(doLongOp(a, b, IFLT));
	}

	@Test // a <= b (<= 0)
	public void test_LCMP_LE() {
		var a = nextLong();
		var b = a + 1;
		assertTrue(doLongOp(a, b, IFLE));
		assertTrue(doLongOp(a, a, IFLE));
	}

	@Test // a > b (> 0)
	public void test_LCMP_GT() {
		var a = nextLong();
		var b = a - 1;
		assertTrue(doLongOp(a, b, IFGT));
	}

	@Test // a >= b (>= 0)
	public void test_LCMP_GE() {
		var a = nextLong();
		var b = a - 1;
		assertTrue(doLongOp(a, b, IFGE));
		assertTrue(doLongOp(a, a, IFGE));
	}

	private static boolean doLongOp(long a, long b, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		var label = new Label();
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitInsn(LCMP);
		mv.visitJumpInsn(opcode, label);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(label);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(4, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()Z", new Value[0], new Value[0]);
		return result.getResult().asBoolean();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong(1L, Long.MAX_VALUE - 1L);
	}
}
