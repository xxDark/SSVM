package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Simple mirror factory.
 *
 * @author xDark
 */
public class SimpleMirrorFactory implements MirrorFactory {
	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public SimpleMirrorFactory(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public InstanceJavaClass newInstanceClass(ObjectValue classLoader, ClassReader classReader, ClassNode node) {
		return new SimpleInstanceJavaClass(vm, classLoader, classReader, node);
	}

	@Override
	public JavaField newField(InstanceJavaClass owner, FieldNode node, int slot, long offset) {
		return new SimpleJavaField(owner, node, slot, offset);
	}

	@Override
	public JavaMethod newMethod(InstanceJavaClass owner, MethodNode node, int slot) {
		return new SimpleJavaMethod(owner, node, node.desc, slot);
	}

	@Override
	public JavaMethod newPolymorphicMethod(JavaMethod method, String desc) {
		return new SimpleJavaMethod(method.getOwner(), method.getNode(), desc, method.getSlot());
	}
}
