package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * VM InvokeDynamic operations.
 *
 * @author xDark
 */
public interface InvokeDynamicOperations {

	/**
	 * Links {@link InvokeDynamicInsnNode}.
	 *
	 * @param insn   Node to link.
	 * @param caller Method caller.
	 * @return Linked method handle or call site.
	 */
	InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceClass caller);

	/**
	 * Invokes linked dynamic call.
	 *
	 * @param stack  Stack to sink arguments from.
	 * @param desc   Call descriptor.
	 * @param handle Call site or method handle.
	 * @param sink   Result sink.
	 */
	void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink);
}
