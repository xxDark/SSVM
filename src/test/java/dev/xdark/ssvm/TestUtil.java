package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

final class TestUtil {

	static InstanceJavaClass createClass(VirtualMachine vm, ClassNode node) {
		val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		val bytes = writer.toByteArray();
		val result = vm.getClassDefiner().parseClass(node.name, bytes, 0, bytes.length, "");
		if (result == null) throw new IllegalStateException();
		val klass = vm.getHelper().newInstanceClass(NullValue.INSTANCE, NullValue.INSTANCE, result.getClassReader(), result.getNode());
		vm.getBootClassLoaderData().forceLinkClass(klass);
		klass.initialize();
		return klass;
	}

	static void visitLdc(MethodVisitor mv, Object ldc) {
		if (ldc == null) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(ldc);
		}
	}
}
