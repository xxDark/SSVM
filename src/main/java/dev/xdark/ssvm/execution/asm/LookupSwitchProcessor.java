package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

import java.util.Collections;

/**
 * Performs LookupSwitch operation.
 *
 * @author xDark
 */
public final class LookupSwitchProcessor implements InstructionProcessor<LookupSwitchInsnNode> {

	@Override
	public Result execute(LookupSwitchInsnNode insn, ExecutionContext ctx) {
		int index = Collections.binarySearch(insn.keys, ctx.getStack().pop().asInt());
		if (index >= 0) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.labels.get(index)));
		} else {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.dflt));
		}
		return Result.CONTINUE;
	}
}
