package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes static method.
 *
 * @author xDark
 */
public final class StaticCallProcessor extends CallProcessor {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
		JavaMethod mn = ctx.getLinkResolver().resolveStaticMethod(klass, insn.name, insn.desc);
		Stack stack = ctx.getStack();
		int localsLength = mn.getMaxArgs();
		Value[] locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		ExecutionContext result = helper.invokeStatic(mn, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
		return Result.CONTINUE;
	}
}
