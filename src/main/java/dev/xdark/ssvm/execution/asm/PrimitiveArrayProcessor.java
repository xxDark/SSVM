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
		var operand = insn.operand;
		var vm = ctx.getVM();
		var primitives = vm.getPrimitives();
		var memoryManager = vm.getMemoryManager();
		ArrayValue array;
		switch (operand) {
			case T_LONG:
				array = memoryManager.newArray(primitives.longPrimitive.newArrayClass(), length, 8L);
				break;
			case T_DOUBLE:
				array = memoryManager.newArray(primitives.doublePrimitive.newArrayClass(), length, 8L);
				break;
			case T_INT:
				array = memoryManager.newArray(primitives.intPrimitive.newArrayClass(), length, 4L);
				break;
			case T_FLOAT:
				array = memoryManager.newArray(primitives.floatPrimitive.newArrayClass(), length, 4L);
				break;
			case T_CHAR:
				array = memoryManager.newArray(primitives.charPrimitive.newArrayClass(), length, 2L);
				break;
			case T_SHORT:
				array = memoryManager.newArray(primitives.shortPrimitive.newArrayClass(), length, 2L);
				break;
			case T_BYTE:
				array = memoryManager.newArray(primitives.bytePrimitive.newArrayClass(), length, 1L);
				break;
			case T_BOOLEAN:
				array = memoryManager.newArray(primitives.booleanPrimitive.newArrayClass(), length, 1L);
				break;
			default:
				throw new IllegalStateException("Illegal array type: " + operand);
		}
		stack.push(array);
		return Result.CONTINUE;
	}
}
