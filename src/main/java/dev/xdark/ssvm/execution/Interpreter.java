package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.api.InstructionInterceptor;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * {@link ExecutionContext} processor.
 *
 * @author xDark
 */
@UtilityClass
public class Interpreter {

	/**
	 * Processes {@link ExecutionContext}.
	 *
	 * @param ctx Context to process.
	 */
	public void execute(ExecutionContext ctx) {
		JavaMethod jm = ctx.getMethod();
		VMInterface vmi = ctx.getVM().getInterface();
		MethodNode mn = jm.getNode();
		InsnList instructions = mn.instructions;
		List<InstructionInterceptor> interceptors = vmi.getInterceptors();
		ExecutionOptions options = ctx.getOptions();
		boolean updateLineNumbers = options.setLineNumbers();
		exec:
		while (true) {
			try {
				int pos = ctx.getInsnPosition();
				ctx.setInsnPosition(pos + 1);
				AbstractInsnNode insn = instructions.get(pos);
				if (updateLineNumbers && insn instanceof LineNumberNode) {
					ctx.setLineNumber(((LineNumberNode) insn).line);
				}
				for (int i = 0, j = interceptors.size(); i < j; i++) {
					if (interceptors.get(i).intercept(ctx, insn) == Result.ABORT) {
						break exec;
					}
				}
				if (insn.getOpcode() == -1) {
					continue;
				}
				InstructionProcessor<AbstractInsnNode> processor = vmi.getProcessor(insn);
				if (processor.execute(insn, ctx) == Result.ABORT) {
					ctx.pollSafePointAndSuspend();
					break;
				}
			} catch (VMException ex) {
				handleExceptionCaught(ctx, ex);
			}
		}
	}

	private static void handleExceptionCaught(ExecutionContext ctx, VMException ex) {
		ctx.unwind();
		InstanceValue oop = ex.getOop();
		InstanceJavaClass exceptionType = oop.getJavaClass();
		List<VMTryCatchBlock> tryCatchBlocks = ctx.getMethod().getTryCatchBlocks();
		int index = ctx.getInsnPosition() - 1;
		boolean shouldRepeat;
		search:
		do {
			shouldRepeat = false;
			for (int i = 0, j = tryCatchBlocks.size(); i < j; i++) {
				VMTryCatchBlock block = tryCatchBlocks.get(i);
				if (index < AsmUtil.getIndex(block.getStart()) || index > AsmUtil.getIndex(block.getEnd())) {
					continue;
				}
				InstanceJavaClass candidate = block.getType();
				boolean handle = candidate == null;
				if (!handle) {
					try {
						handle = candidate.isAssignableFrom(exceptionType);
					} catch (VMException hex) {
						index = AsmUtil.getIndex(block.getHandler());
						shouldRepeat = true;
						continue search;
					}
				}
				if (handle) {
					ctx.getStack().push(oop);
					ctx.setInsnPosition(AsmUtil.getIndex(block.getHandler()));
					ctx.pollSafePointAndSuspend();
					return;
				}
			}
		} while (shouldRepeat);
		throw ex;
	}
}
