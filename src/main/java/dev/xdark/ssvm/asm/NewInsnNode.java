package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * {@code NEW} instruction.
 *
 * @author xDark
 */
public final class NewInsnNode extends DelegatingInsnNode<TypeInsnNode> {

	private final InstanceJavaClass type;

	/**
	 * @param delegate
	 * 		Backing instruction.
	 * @param type
	 * 		Class type.
	 */
	public NewInsnNode(TypeInsnNode delegate, InstanceJavaClass type) {
		super(delegate, VMOpcodes.NEW);
		this.type = type;
	}

	/**
	 * @return class type.
	 */
	public InstanceJavaClass getJavaType() {
		return type;
	}
}
