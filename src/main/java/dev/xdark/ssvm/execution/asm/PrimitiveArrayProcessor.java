package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
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
		var stack = ctx.getStack();
		var length = stack.pop().asInt();
		var vm = ctx.getVM();
		vm.getHelper().checkArrayLength(length);
		var operand = insn.operand;
		var primitives = vm.getPrimitives();
		var helper = vm.getHelper();
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
