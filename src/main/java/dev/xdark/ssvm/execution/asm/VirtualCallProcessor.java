package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes virtual method.
 *
 * @author xDark
 */
public final class VirtualCallProcessor extends CallProcessor {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
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
		ExecutionContext result = vm.getHelper().invokeVirtual(insn.name, insn.desc, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
		return Result.CONTINUE;
	}
}
