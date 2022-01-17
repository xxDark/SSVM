package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Throws error.
 *
 * @author xDark
 */
public final class ThrowProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ObjectValue exception = ctx.getStack().pop();
		if (exception.isNull()) {
			// NPE it is then.
			val vm = ctx.getVM();
			val exceptionClass = vm.getSymbols().java_lang_NullPointerException;
			exceptionClass.initialize();
			exception = vm.getMemoryManager().newInstance(exceptionClass);
			vm.getHelper().invokeExact(exceptionClass, "<init>", "()V", new Value[0], new Value[]{exception});
		}
		throw new VMException((InstanceValue) exception);
	}
}
