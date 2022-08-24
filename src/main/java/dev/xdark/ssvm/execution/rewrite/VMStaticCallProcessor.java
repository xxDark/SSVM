package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Fast-path for INVOKESTATIC.
 *
 * @author xDark
 */
public class VMStaticCallProcessor extends AbstractVMCallProcessor {
	@Override
	protected JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext<?> ctx) {
		MethodInsnNode callInfo = insn.getDelegate();
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), callInfo.owner, true);
		JavaMethod method = ctx.getLinkResolver().resolveStaticMethod(klass, callInfo.name, callInfo.desc);
		insn.setResolved(method);
		return method;
	}
}
