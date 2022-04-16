package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.tree.LineNumberNode;

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
	 * @param ctx
	 * 		Context to process.
	 */
	public void execute(ExecutionContext ctx) {
		val jm = ctx.getMethod();
		val vmi = ctx.getVM().getInterface();
		val mn = jm.getNode();
		val instructions = mn.instructions;
		val interceptors = vmi.getInterceptors();
		exec:
		while (true) {
			try {
				val pos = ctx.getInsnPosition();
				ctx.setInsnPosition(pos + 1);
				val insn = instructions.get(pos);
				if (insn instanceof LineNumberNode) ctx.setLineNumber(((LineNumberNode) insn).line);
				for (int i = 0; i < interceptors.size(); i++) {
					if (interceptors.get(i).intercept(ctx, insn) == Result.ABORT)
						break exec;
				}
				if (insn.getOpcode() == -1) continue;
				val processor = vmi.getProcessor(insn);
				if (processor == null) {
					ctx.getHelper().throwException(ctx.getSymbols().java_lang_InternalError, "No implemented processor for " + insn.getOpcode());
					continue;
				}
				if (processor.execute(insn, ctx) == Result.ABORT) break;
			} catch (VMException ex) {
				val stack = ctx.getStack();
				Value value;
				while ((value = stack.poll()) != null) {
					if (value instanceof ObjectValue) {
						val obj = (ObjectValue) value;
						if (obj.isHeldByCurrentThread()) {
							obj.monitorExit();
						}
					}
				}
				val oop = ex.getOop();
				val exceptionType = oop.getJavaClass();
				val tryCatchBlocks = mn.tryCatchBlocks;
				int index = ctx.getInsnPosition() - 1;
				val vm = ctx.getVM();
				// int lastIndex = -1;
				boolean shouldRepeat;
				search:
				do {
					shouldRepeat = false;
					for (int i = 0, j = tryCatchBlocks.size(); i < j; i++) {
						val block = tryCatchBlocks.get(i);
						if (index < AsmUtil.getIndex(block.start) || index > AsmUtil.getIndex(block.end)) continue;
						String type = block.type;
						boolean handle = type == null;
						if (!handle) {
							try {
								val candidate = vm.findClass(ctx.getOwner().getClassLoader(), type, false);
								handle = candidate.isAssignableFrom(exceptionType);
							} catch (VMException hex) {
								index = AsmUtil.getIndex(block.handler);
								/*
								if (lastIndex == index) {
									throw ex;
								}
								lastIndex = index;
								*/
								shouldRepeat = true;
								continue search;
							}
						}
						if (handle) {
							stack.push(oop);
							ctx.setInsnPosition(AsmUtil.getIndex(block.handler));
							continue exec;
						}
					}
				} while (shouldRepeat);
				throw ex;
			}
		}
	}
}
