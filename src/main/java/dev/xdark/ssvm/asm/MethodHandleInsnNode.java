package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.Getter;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * VM wrapped instruction for {@link InvokeDynamicInsnNode}.
 *
 * @author xDark
 */
public final class MethodHandleInsnNode extends DelegatingInsnNode<InvokeDynamicInsnNode> {

	@Getter
	private final InstanceValue linker;

	/**
	 * @param delegate
	 * 		Backing instruction.
	 * @param linker
	 * 		CallSite linker.
	 */
	public MethodHandleInsnNode(InvokeDynamicInsnNode delegate, InstanceValue linker) {
		super(delegate, VMOpcodes.METHOD_HANDLE);
		this.linker = linker;
	}
}
