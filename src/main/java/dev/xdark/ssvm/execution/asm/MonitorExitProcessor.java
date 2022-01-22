package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Releases object monitor.
 *
 * @author xDark
 */
public final class MonitorExitProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		try {
			ctx.getStack().<ObjectValue>pop().monitorExit();
		} catch (IllegalMonitorStateException ex) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException);
		}
		return Result.CONTINUE;
	}
}
