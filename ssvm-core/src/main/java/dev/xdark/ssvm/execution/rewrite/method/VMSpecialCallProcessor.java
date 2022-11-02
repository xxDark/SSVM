package dev.xdark.ssvm.execution.rewrite.method;

import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Fast-path for INVOKESPECIAL.
 *
 * @author xDark
 */
public final class VMSpecialCallProcessor extends AbstractVMCallProcessor {
	@Override
	protected JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext<?> ctx) {
		MethodInsnNode callInfo = insn.getDelegate();
		InstanceClass klass = (InstanceClass) ctx.getOperations().findClass(ctx.getOwner(), callInfo.owner, true);
		JavaMethod method = ctx.getLinkResolver().resolveVirtualMethod(klass, callInfo.name, callInfo.desc);
		insn.setResolved(method);
		return method;
	}
}
