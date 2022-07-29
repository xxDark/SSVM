package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class BiObjectComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test // a == b
	public void test_IFACMPEQ() {
		String value = "Hello, World!";
		assertTrue(doValueOp(value, value, IF_ACMPEQ));
	}

	@Test // a != b
	public void test_IFACMPNE() {
		String v1 = "Hello, World!";
		String v2 = "send help";
		assertTrue(doValueOp(v1, v2, IF_ACMPNE));
	}

	private static boolean doValueOp(Object a, Object b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		Label label = new Label();
		TestUtil.visitLdc(mv, a);
		if (a == b) {
			mv.visitInsn(DUP);
		} else {
			TestUtil.visitLdc(mv, b);
		}
		mv.visitJumpInsn(opcode, label);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(label);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(2, 0);
		InstanceJavaClass jc = TestUtil.createClass(vm, node);
		ExecutionContext result = vm.getHelper().invoke(vm.getPublicLinkResolver().resolveStaticMethod(jc, "test", "()Z"), vm.getThreadStorage().newLocals(0));
		return result.getResult().asBoolean();
	}
}
