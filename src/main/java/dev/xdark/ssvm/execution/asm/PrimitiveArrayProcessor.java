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
		var memoryManager = vm.getMemoryManager();
		ArrayValue array;
		switch (operand) {
			case T_LONG:
				array = memoryManager.newArray(primitives.longPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(long.class));
				break;
			case T_DOUBLE:
				array = memoryManager.newArray(primitives.doublePrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(double.class));
				break;
			case T_INT:
				array = memoryManager.newArray(primitives.intPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(int.class));
				break;
			case T_FLOAT:
				array = memoryManager.newArray(primitives.floatPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(float.class));
				break;
			case T_CHAR:
				array = memoryManager.newArray(primitives.charPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(char.class));
				break;
			case T_SHORT:
				array = memoryManager.newArray(primitives.shortPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(short.class));
				break;
			case T_BYTE:
				array = memoryManager.newArray(primitives.bytePrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(byte.class));
				break;
			case T_BOOLEAN:
				array = memoryManager.newArray(primitives.booleanPrimitive.newArrayClass(), length, memoryManager.arrayIndexScale(boolean.class));
				break;
			default:
				throw new IllegalStateException("Illegal array type: " + operand);
		}
		stack.push(array);
		return Result.CONTINUE;
	}
}
