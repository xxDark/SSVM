package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Performs TableSwitch operation.
 *
 * @author xDark
 */
public final class TableSwitchProcessor implements InstructionProcessor<TableSwitchInsnNode> {

	@Override
	public Result execute(TableSwitchInsnNode insn, ExecutionContext ctx) {
		int value = ctx.getStack().popInt();
		if (value < insn.min || value > insn.max) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.dflt));
		} else {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.labels.get(value - insn.min)));
		}
		return Result.CONTINUE;
	}
}
