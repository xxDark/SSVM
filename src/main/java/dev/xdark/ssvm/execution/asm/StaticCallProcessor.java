package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes static method.
 *
 * @author xDark
 */
public final class StaticCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var helper = vm.getHelper();
		var owner = helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		if (owner == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.owner);
		}
		var stack = ctx.getStack();
		var args = Type.getArgumentTypes(insn.desc);
		var localsLength = args.length;
		var locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		var result = helper.invokeStatic((InstanceJavaClass) owner, insn.name, insn.desc, new Value[0], locals);
		if (Type.getReturnType(insn.desc) != Type.VOID_TYPE) {
			stack.pushGeneric(result.getResult());
		}
		return Result.CONTINUE;
	}
}
