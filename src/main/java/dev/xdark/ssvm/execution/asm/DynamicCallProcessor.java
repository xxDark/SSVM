package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.MethodHandleInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Type;

/**
 * Processor for linked invokedynamic instructions.
 *
 * @author xDark
 */
public final class DynamicCallProcessor implements InstructionProcessor<MethodHandleInsnNode> {

	@Override
	public Result execute(MethodHandleInsnNode insn, ExecutionContext ctx) {
		val delegate = insn.getDelegate();
		val desc = delegate.desc;
		val args = Type.getArgumentTypes(desc);
		int localsLength = args.length;
		int x = localsLength + 1;
		val locals = new Value[x];
		val stack = ctx.getStack();
		while (localsLength-- != 0) {
			locals[--x] = stack.popGeneric();
		}
		locals[0] = insn.getMethodHandle();
		val invoked = ctx.getHelper().invokeVirtual("invokeExact", desc, new Value[0], locals).getResult();
		if (!invoked.isVoid()) {
			stack.pushGeneric(invoked);
		}
		return Result.CONTINUE;
	}
}
