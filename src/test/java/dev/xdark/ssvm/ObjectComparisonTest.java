package dev.xdark.ssvm;

import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

public class ObjectComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test // v == null
	public void test_IFNULL() {
		assertTrue(doValueOp(null, IFNULL));
	}

	@Test // v != null
	public void test_IFNONNULL() {
		assertTrue(doValueOp("send help", IFNONNULL));
	}

	private static boolean doValueOp(Object ldc, int opcode) {
		var node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		var mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		var label = new Label();
		TestUtil.visitLdc(mv, ldc);
		mv.visitJumpInsn(opcode, label);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(label);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(1, 0);
		var jc = TestUtil.createClass(vm, node);
		var result = vm.getHelper().invokeStatic(jc, "test", "()Z", new Value[0], new Value[0]);
		return result.getResult().asBoolean();
	}
}
