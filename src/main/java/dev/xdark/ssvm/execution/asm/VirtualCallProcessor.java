package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes virtual method.
 *
 * @author xDark
 */
public final class VirtualCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(insn.desc);
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = vm.getHelper().invokeVirtual(insn.name, insn.desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
		return Result.CONTINUE;
	}
}
