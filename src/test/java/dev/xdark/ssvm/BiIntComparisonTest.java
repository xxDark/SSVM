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

public class BiIntComparisonTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).initialize();
	}

	@Test // a != b
	public void test_IFICMPNE() {
		int a = nextInt();
		int b = nextInt(a);
		assertTrue(doIntJump(a, b, IF_ICMPNE));
	}

	@Test // a == b
	public void test_IFICMPEQ() {
		int v = nextInt();
		assertTrue(doIntJump(v, v, IF_ICMPEQ));
	}

	@Test // a < b
	public void test_IFICMPLT() {
		int a = nextInt();
		assertTrue(doIntJump(a, a + 1, IF_ICMPLT));
	}

	@Test // a < = b
	public void test_IFICMPLE() {
		int v = nextInt();
		assertTrue(doIntJump(v, v + 1, IF_ICMPLE));
		assertTrue(doIntJump(v, v, IF_ICMPLE));
	}

	@Test // a > b
	public void test_IFICMPGT() {
		int v = nextInt();
		assertTrue(doIntJump(v, v - 1, IF_ICMPGT));
	}

	@Test // a >= b
	public void test_IFICMPGE() {
		int v = nextInt();
		assertTrue(doIntJump(v, v - 1, IF_ICMPGE));
		assertTrue(doIntJump(v, v, IF_ICMPGE));
	}

	private static boolean doIntJump(int a, int b, int opcode) {
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "test", "()Z", null, null);
		Label label = new Label();
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
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

	private static int nextInt(int except) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		int i;
		do {
			i = rng.nextInt(1, Integer.MAX_VALUE - 1);
		} while (i == except); // bad numbers!
		return i;
	}

	private static int nextInt() {
		return nextInt(0);
	}
}
