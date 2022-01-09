package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes exact method.
 *
 * @author xDark
 */
public final class SpecialCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var owner = (InstanceJavaClass) vm.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		var stack = ctx.getStack();
		var args = Type.getArgumentTypes(insn.desc);
		var localsLength = args.length + 1;
		var locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		var helper = vm.getHelper();
		if ("<init>".equals(insn.name)) {
			helper.initializeDefaultValues((InstanceValue) locals[0], owner);
		}
		var result = helper.invokeExact(owner, insn.name, insn.desc, new Value[0], locals);
		if (Type.getReturnType(insn.desc) != Type.VOID_TYPE) {
			stack.pushGeneric(result.getResult());
		}
		return Result.CONTINUE;
	}
}
