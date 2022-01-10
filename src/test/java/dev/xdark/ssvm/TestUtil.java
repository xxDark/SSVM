package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

final class TestUtil {

	static InstanceJavaClass createClass(VirtualMachine vm, ClassNode node) {
		var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		var bytes = writer.toByteArray();
		var result = vm.getClassDefiner().parseClass(node.name, bytes, 0, bytes.length, "");
		if (result == null) throw new IllegalStateException();
		var cr = result.getClassReader();
		var klass = new InstanceJavaClass(vm, NullValue.INSTANCE, cr, result.getNode());
		var oop = vm.getMemoryManager().setOopForClass(klass);
		klass.setOop(oop);
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
