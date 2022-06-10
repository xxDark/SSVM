package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * VM call instruction.
 *
 * @author xDark
 */
public final class VMCallInsnNode extends DelegatingInsnNode<MethodInsnNode> {

	private final Type[] args;
	private JavaMethod resolved;
	private int argCount = -1;
	private JavaClass javaClass;

	/**
	 * @param delegate      Backing instruction.
	 * @param virtualOpcode VM specific opcode.
	 */
	public VMCallInsnNode(MethodInsnNode delegate, int virtualOpcode) {
		super(delegate, virtualOpcode);
		args = Type.getArgumentTypes(delegate.desc);
	}

	/**
	 * @return resolved method.
	 */
	public JavaMethod getResolved() {
		return resolved;
	}

	/**
	 * @param resolved New resolved method.
	 */
	public void setResolved(JavaMethod resolved) {
		this.resolved = resolved;
	}

	/**
	 * @return argument types.
	 */
	public Type[] getArgs() {
		return args;
	}

	/**
	 * @return argument count.
	 */
	public int getArgCount() {
		int argCount = this.argCount;
		if (argCount == -1) {
			argCount = 0;
			Type[] args = this.args;
			for (Type type : args) {
				argCount += type.getSize();
			}
			return this.argCount = argCount;
		}
		return argCount;
	}

	// For INVOKEINTERFACE
	public JavaClass getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(JavaClass javaClass) {
		this.javaClass = javaClass;
	}
}
