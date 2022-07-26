package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class IntComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test // != 0
	public void test_IFNE() {
		int v = nextInt();
		assertTrue(doIntJump(v, IFNE));
	}

	@Test // == 0
	public void test_IFEQ() {
		assertTrue(doIntJump(0, IFEQ));
	}

	@Test // < 0
	public void test_IFLT() {
		int v = nextInt();
		assertTrue(doIntJump(-v, IFLT));
	}

	@Test // <= 0
	public void test_IFLE() {
		int v = nextInt();
		assertTrue(doIntJump(-v, IFLE));
		assertTrue(doIntJump(0, IFLE));
	}

	@Test // > 0
	public void test_IFGT() {
		int v = nextInt();
		assertTrue(doIntJump(v, IFGT));
	}

	@Test // > 0
	public void test_IFGE() {
		int v = nextInt();
		assertTrue(doIntJump(v, IFGE));
		assertTrue(doIntJump(0, IFGE));
	}

	private static boolean doIntJump(int value, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		Label label = new Label();
		mv.visitLdcInsn(value);
		mv.visitJumpInsn(opcode, label);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(label);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(1, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invokeDirect(vm.getLinkResolver().resolveStaticMethod(jc, "test", "()Z"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asBoolean();
	}

	private static int nextInt() {
		return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE - 1);
	}
}
