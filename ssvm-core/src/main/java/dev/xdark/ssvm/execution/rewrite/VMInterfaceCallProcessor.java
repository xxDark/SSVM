package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Fast-path for INVOKEINTERFACE.
 *
 * @author xDark
 */
public final class VMInterfaceCallProcessor extends AbstractVMCallProcessor {
	@Override
	protected JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext<?> ctx) {
		MethodInsnNode callInfo = insn.getDelegate();
		VirtualMachine vm = ctx.getVM();
		VMOperations helper = vm.getOperations();
		JavaClass javaClass = insn.getJavaClass();
		if (javaClass == null) {
			javaClass = helper.findClass(ctx.getOwner(), callInfo.owner, true);
			insn.setJavaClass(javaClass);
		}
		int args = insn.getArgCount();
		Stack stack = ctx.getStack();
		ObjectValue instance = stack.getReferenceAt(stack.position() - args - 1);
		return vm.getRuntimeResolver().resolveInterfaceMethod(instance, callInfo.name, callInfo.desc);
	}
}
