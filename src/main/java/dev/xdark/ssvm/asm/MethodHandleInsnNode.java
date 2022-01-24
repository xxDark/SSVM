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
	private final InstanceValue methodHandle;

	/**
	 * @param delegate
	 * 		Backing instruction.
	 * @param methodHandle
	 * 		CallSite linked method handle.
	 */
	public MethodHandleInsnNode(InvokeDynamicInsnNode delegate, InstanceValue methodHandle) {
		super(delegate, VMOpcodes.METHOD_HANDLE);
		this.methodHandle = methodHandle;
	}
}
