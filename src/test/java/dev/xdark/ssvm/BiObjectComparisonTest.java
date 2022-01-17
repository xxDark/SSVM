package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class BiObjectComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test // a == b
	public void test_IFACMPEQ() {
		val value = "Hello, World!";
		assertTrue(doValueOp(value, value, IF_ACMPEQ));
	}

	@Test // a != b
	public void test_IFACMPNE() {
		val v1 = "Hello, World!";
		val v2 = "send help";
		assertTrue(doValueOp(v1, v2, IF_ACMPNE));
	}

	private static boolean doValueOp(Object a, Object b, int opcode) {
		val node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		val mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		val label = new Label();
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
		val jc = TestUtil.createClass(vm, node);
		val result = vm.getHelper().invokeStatic(jc, "test", "()Z", new Value[0], new Value[0]);
		return result.getResult().asBoolean();
	}
}
