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
		exec:
		while (true) {
			try {
				val pos = ctx.getInsnPosition();
				ctx.setInsnPosition(pos + 1);
				val insn = instructions.get(pos);
				if (insn instanceof LineNumberNode) ctx.setLineNumber(((LineNumberNode) insn).line);
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
				val index = ctx.getInsnPosition() - 1;
				val vm = ctx.getVM();
				for (int i = 0, j = tryCatchBlocks.size(); i < j; i++) {
					val block = tryCatchBlocks.get(i);
					String type = block.type;
					if (type == null) type = "java/lang/Throwable";
					val candidate = vm.findClass(ctx.getOwner().getClassLoader(), type, false);
					if (index < AsmUtil.getIndex(block.start) || index > AsmUtil.getIndex(block.end)) continue;
					if (candidate.isAssignableFrom(exceptionType)) {
						stack.push(oop);
						ctx.setInsnPosition(AsmUtil.getIndex(block.handler));
						continue exec;
					}
				}
				throw ex;
			}
		}
	}
}
