package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import lombok.val;
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
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val owner = helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		if (owner == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.owner);
		}
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(insn.desc);
		int localsLength = args.length;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeStatic((InstanceJavaClass) owner, insn.name, insn.desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
		return Result.CONTINUE;
	}
}
