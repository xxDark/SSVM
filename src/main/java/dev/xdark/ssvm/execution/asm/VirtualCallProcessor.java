package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
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
		var vm = ctx.getVM();
		var owner = vm.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		var stack = ctx.getStack();
		var args = Type.getArgumentTypes(insn.desc);
		var localsLength = args.length + 1;
		var locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		var result = vm.getHelper().invokeVirtual((InstanceJavaClass) owner, insn.name, insn.desc, new Value[0], locals);
		if (Type.getReturnType(insn.desc) != Type.VOID_TYPE) {
			stack.pushGeneric(result.getResult());
		}
		return Result.CONTINUE;
	}
}
