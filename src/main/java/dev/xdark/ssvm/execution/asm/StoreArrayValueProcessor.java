package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores value into an array.
 *
 * @author xDark
 */
public final class StoreArrayValueProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val value = stack.<ObjectValue>pop();
		int index = stack.pop().asInt();
		val array = stack.<ArrayValue>pop();
		val helper = ctx.getHelper();
		helper.rangeCheck(array, index);

		if (!value.isNull()) {
			val type = array.getJavaClass().getComponentType();
			val valueType = value.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				val symbols = ctx.getVM().getSymbols();
				helper.throwException(symbols.java_lang_ArrayStoreException, valueType.getName());
			}
		}

		array.setValue(index, value);
		return Result.CONTINUE;
	}
}
