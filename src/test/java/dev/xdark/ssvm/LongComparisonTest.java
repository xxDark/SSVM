package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class LongComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test // a < b (< 0)
	public void test_LCMP_LT() {
		long a = nextLong();
		long b = a + 1;
		assertTrue(doLongOp(a, b, IFLT));
	}

	@Test // a <= b (<= 0)
	public void test_LCMP_LE() {
		long a = nextLong();
		long b = a + 1;
		assertTrue(doLongOp(a, b, IFLE));
		assertTrue(doLongOp(a, a, IFLE));
	}

	@Test // a > b (> 0)
	public void test_LCMP_GT() {
		long a = nextLong();
		long b = a - 1;
		assertTrue(doLongOp(a, b, IFGT));
	}

	@Test // a >= b (>= 0)
	public void test_LCMP_GE() {
		long a = nextLong();
		long b = a - 1;
		assertTrue(doLongOp(a, b, IFGE));
		assertTrue(doLongOp(a, a, IFGE));
	}

	private static boolean doLongOp(long a, long b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		Label label = new Label();
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
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getLinkResolver().resolveStaticMethod(jc, "test", "()Z"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asBoolean();
	}

	private static long nextLong() {
		return ThreadLocalRandom.current().nextLong(1L, Long.MAX_VALUE - 1L);
	}
}
