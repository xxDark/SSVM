package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.api.InstructionInterceptor;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.*;

import java.util.List;

/**
 * {@link ExecutionContext} processor.
 *
 * @author xDark
 */
@UtilityClass
public class Interpreter {
	private static int maxIterations = Integer.MAX_VALUE - 1;

	/**
	 * Processes {@link ExecutionContext}.
	 *
	 * @param ctx Context to process.
	 */
	public void execute(ExecutionContext<?> ctx) {
		JavaMethod jm = ctx.getMethod();
		VMInterface vmi = ctx.getVM().getInterface();
		MethodNode mn = jm.getNode();
		InsnList instructions = mn.instructions;
		List<InstructionInterceptor> interceptors = vmi.getInstructionInterceptors();
		int iter = 0;
		exec:
		while (true) {
			try {
				if (iter++ >= maxIterations) {
					handleMaxIterations(ctx);
					break;
				}
				int pos = ctx.getInsnPosition();
				ctx.setInsnPosition(pos + 1);
				AbstractInsnNode insn = instructions.get(pos);
				if (insn instanceof LineNumberNode) {
					ctx.setLineNumber(((LineNumberNode) insn).line);
				}
				for (int i = 0, j = interceptors.size(); i < j; i++) {
					if (interceptors.get(i).intercept(ctx, insn) == Result.ABORT) {
						break exec;
					}
				}
				if (insn.getOpcode() == -1 || !AsmUtil.isValid(insn)) {
					continue;
				}
				InstructionProcessor<AbstractInsnNode> processor = vmi.getProcessor(insn);
				if (processor.execute(insn, ctx) == Result.ABORT) {
					break;
				}
			} catch (VMException ex) {
				handleExceptionCaught(ctx, ex);
			}
		}
	}

	/**
	 * @param maxIterations Max number of instructions to interpret/iterate over before aborting.
	 */
	public static void setMaxIterations(int maxIterations) {
		Interpreter.maxIterations = maxIterations;
	}

	/**
	 * @return Max number of instructions to interpret/iterate over before aborting.
	 */
	public static int getMaxIterations() {
		return maxIterations;
	}

	private static void handleMaxIterations(ExecutionContext<?> ctx) {
		ctx.getVM().getInterface().handleMaxInterations(ctx);
	}

	private static void handleExceptionCaught(ExecutionContext<?> ctx, VMException ex) {
		Stack stack = ctx.getStack();
		stack.clear();
		InstanceValue oop = ex.getOop();
		InstanceClass exceptionType = oop.getJavaClass();
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
				InstanceClass candidate = block.getType();
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
					stack.pushReference(oop);
					ctx.setInsnPosition(AsmUtil.getIndex(block.getHandler()));
					return;
				}
			}
		} while (shouldRepeat);
		throw ex;
	}
}
