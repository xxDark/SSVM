package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes interface method.
 *
 * @author xDark
 */
public final class InterfaceCallProcessor extends CallProcessor {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getClassLoader(), insn.owner, true);
		Stack stack = ctx.getStack();
		Type[] args = Type.getArgumentTypes(insn.desc);
		int localsLength = 1;
		for (Type arg : args) {
			localsLength += arg.getSize();
		}
		Value[] locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		ExecutionContext result = helper.invokeInterface(klass, insn.name, insn.desc, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
		return Result.CONTINUE;
	}
}
