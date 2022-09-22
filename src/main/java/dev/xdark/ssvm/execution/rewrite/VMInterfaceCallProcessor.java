package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
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
		VMHelper helper = vm.getHelper();
		JavaClass javaClass = insn.getJavaClass();
		if (javaClass == null) {
			javaClass = helper.tryFindClass(ctx.getClassLoader(), callInfo.owner, true);
			insn.setJavaClass(javaClass);
		}
		int args = insn.getArgCount();
		Stack stack = ctx.getStack();
		ObjectValue instance = stack.getReferenceAt(stack.position() - args - 1);
		helper.checkNotNull(instance);
		InstanceJavaClass prioritized = ((InstanceValue) instance).getJavaClass();
		return vm.getPublicLinkResolver().resolveVirtualMethod(prioritized, javaClass, callInfo.name, callInfo.desc);
	}
}
