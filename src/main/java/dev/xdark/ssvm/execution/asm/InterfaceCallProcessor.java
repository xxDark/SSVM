package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

/**
 * Invokes interface method.
 *
 * @author xDark
 */
public final class InterfaceCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val owner = helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(insn.desc);
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeInterface((InstanceJavaClass) owner, insn.name, insn.desc, new Value[0], locals);
		if (Type.getReturnType(insn.desc) != Type.VOID_TYPE) {
			stack.pushGeneric(result.getResult());
		}
		return Result.CONTINUE;
	}
}
