package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.val;
import org.objectweb.asm.tree.IntInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Allocates new array.
 *
 * @author xDark
 */
public final class PrimitiveArrayProcessor implements InstructionProcessor<IntInsnNode> {

	@Override
	public Result execute(IntInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int length = stack.pop().asInt();
		val vm = ctx.getVM();
		vm.getHelper().checkArrayLength(length);
		val operand = insn.operand;
		val primitives = vm.getPrimitives();
		val helper = vm.getHelper();
		ArrayValue array;
		switch (operand) {
			case T_LONG:
				array = helper.newArray(primitives.longPrimitive, length);
				break;
			case T_DOUBLE:
				array = helper.newArray(primitives.doublePrimitive, length);
				break;
			case T_INT:
				array = helper.newArray(primitives.intPrimitive, length);
				break;
			case T_FLOAT:
				array = helper.newArray(primitives.floatPrimitive, length);
				break;
			case T_CHAR:
				array = helper.newArray(primitives.charPrimitive, length);
				break;
			case T_SHORT:
				array = helper.newArray(primitives.shortPrimitive, length);
				break;
			case T_BYTE:
				array = helper.newArray(primitives.bytePrimitive, length);
				break;
			case T_BOOLEAN:
				array = helper.newArray(primitives.booleanPrimitive, length);
				break;
			default:
				throw new IllegalStateException("Illegal array type: " + operand);
		}
		stack.push(array);
		return Result.CONTINUE;
	}
}
