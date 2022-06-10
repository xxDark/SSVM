package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Fast-path for INVOKEVIRTUAL.
 *
 * @author xDark
 */
public final class VMVirtualCallProcessor extends AbstractVMCallProcessor {
	@Override
	protected JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext ctx) {
		MethodInsnNode callInfo = insn.getDelegate();
		int args = insn.getArgCount();
		VirtualMachine vm = ctx.getVM();
		Stack stack = ctx.getStack();
		ObjectValue instance = stack.getAt(stack.position() - args - 1);
		vm.getHelper().checkNotNull(instance);
		InstanceJavaClass javaClass;
		if (instance instanceof ArrayValue) {
			javaClass = vm.getSymbols().java_lang_Object();
		} else {
			javaClass = ((InstanceValue) instance).getJavaClass();
		}
		return vm.getLinkResolver().resolveVirtualMethod(javaClass, javaClass, callInfo.name, callInfo.desc);
	}

	@Override
	protected boolean alwaysResolve() {
		return true;
	}
}
