package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.mirror.JavaClass;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Wrapper for type instructions.
 *
 * @author xDark
 */
public final class VMTypeInsnNode extends DelegatingInsnNode<TypeInsnNode> {

	private final JavaClass type;

	/**
	 * @param delegate      Backing instruction.
	 * @param virtualOpcode VM specific opcode.
	 * @param type          Class type.
	 */
	public VMTypeInsnNode(TypeInsnNode delegate, int virtualOpcode, JavaClass type) {
		super(delegate, virtualOpcode);
		this.type = type;
	}

	/**
	 * @return class type.
	 */
	public JavaClass getJavaType() {
		return type;
	}
}
