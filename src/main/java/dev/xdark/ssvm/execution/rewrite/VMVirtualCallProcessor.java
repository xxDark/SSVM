package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Fast-path for INVOKEVIRTUAL.
 *
 * @author xDark
 */
public final class VMVirtualCallProcessor extends AbstractVMCallProcessor {
	@Override
	protected JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext<?> ctx) {
		MethodInsnNode callInfo = insn.getDelegate();
		int args = insn.getArgCount();
		VirtualMachine vm = ctx.getVM();
		Stack stack = ctx.getStack();
		ObjectValue instance = stack.getReferenceAt(stack.position() - args - 1);
		vm.getHelper().checkNotNull(instance);
		return vm.getPublicLinkResolver().resolveVirtualMethod(instance, callInfo.name, callInfo.desc);
	}
}
