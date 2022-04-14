package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.Getter;
import lombok.val;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * VM wrapped instruction for {@link InvokeDynamicInsnNode}.
 *
 * @author xDark
 */
public final class LinkedDynamicCallNode extends DelegatingInsnNode<InvokeDynamicInsnNode> {

	@Getter
	private final InstanceValue methodHandle;
	@Getter
	private final Type[] descriptorArgs;
	private int descriptorArgsSize = -1;

	/**
	 * @param delegate
	 * 		Backing instruction.
	 * @param methodHandle
	 * 		CallSite linked method handle.
	 */
	public LinkedDynamicCallNode(InvokeDynamicInsnNode delegate, InstanceValue methodHandle) {
		super(delegate, VMOpcodes.DYNAMIC_CALL);
		this.methodHandle = methodHandle;
		descriptorArgs = Type.getArgumentTypes(delegate.desc);
	}

	/**
	 * @return size of descriptor arguments.
	 */
	public int getDescriptorArgsSize() {
		int descriptorArgsSize = this.descriptorArgsSize;
		if (descriptorArgsSize == -1 ) {
			descriptorArgsSize = 0;
			for (val arg : descriptorArgs) {
				descriptorArgsSize += arg.getSize();
			}
			return this.descriptorArgsSize = descriptorArgsSize;
		}
		return descriptorArgsSize;
	}
}
