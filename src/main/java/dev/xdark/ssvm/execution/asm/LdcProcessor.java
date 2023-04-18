package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.ConstantDoubleInsnNode;
import dev.xdark.ssvm.asm.ConstantFloatInsnNode;
import dev.xdark.ssvm.asm.ConstantIntInsnNode;
import dev.xdark.ssvm.asm.ConstantLongInsnNode;
import dev.xdark.ssvm.asm.ConstantReferenceInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Processor for {@link LdcInsnNode}.
 * <p>
 * Pushes generic ldc onto the stack.
 *
 * @author xDark
 */
public final class LdcProcessor implements InstructionProcessor<LdcInsnNode> {

	@Override
	public Result execute(LdcInsnNode insn, ExecutionContext<?> ctx) {
		if (AsmUtil.isValid(insn)) {
			InsnList list = ctx.getMethod().getNode().instructions;
			Object cst = insn.cst;
			if (cst instanceof ObjectValue) {
				list.set(insn, new ConstantReferenceInsnNode(insn, (ObjectValue) cst));
			} else if (cst instanceof Long) {
				list.set(insn, new ConstantLongInsnNode(insn, (long) cst));
			} else if (cst instanceof Double) {
				list.set(insn, new ConstantDoubleInsnNode(insn, (double) cst));
			} else if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte) {
				list.set(insn, new ConstantIntInsnNode(insn, (int) (Number) cst));
			} else if (cst instanceof Float) {
				list.set(insn, new ConstantFloatInsnNode(insn, (float) cst));
			} else if (cst instanceof Character) {
				list.set(insn, new ConstantIntInsnNode(insn, (char) cst));
			} else if (cst instanceof String) {
				list.set(insn, new ConstantReferenceInsnNode(insn, ctx.getVM().getStringPool().intern((String) cst)));
			} else {
				Value value = ctx.getHelper().referenceFromLdc(cst);
				if (!(value instanceof ObjectValue)) {
					throw new PanicException("Bad valueFromLdc value: " + value);
				}
				ObjectValue ref = (ObjectValue) value;
				ctx.getGarbageCollector().makeGlobalReference(ref);
				list.set(insn, new ConstantReferenceInsnNode(insn, ref));
			}
			ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		}
		return Result.CONTINUE;
	}
}
