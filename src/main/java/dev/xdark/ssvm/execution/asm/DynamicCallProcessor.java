package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Type;

/**
 * Processor for linked invokedynamic instructions.
 *
 * @author xDark
 */
public final class DynamicCallProcessor implements InstructionProcessor<LinkedDynamicCallNode> {

	@Override
	public Result execute(LinkedDynamicCallNode insn, ExecutionContext ctx) {
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
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceValue handle = insn.getMethodHandle();
		if (vm.getSymbols().java_lang_invoke_CallSite.isAssignableFrom(handle.getJavaClass())) {
			handle = helper.checkNotNull(helper.invokeVirtual("getTarget", "()Ljava/lang/invoke/MethodHandle;", new Value[0], new Value[]{handle}).getResult());
		}
		locals[0] = handle;
		val invoked = helper.invokeVirtual("invokeExact", desc, new Value[0], locals).getResult();
		if (!invoked.isVoid()) {
			stack.pushGeneric(invoked);
		}
		return Result.CONTINUE;
	}
}
