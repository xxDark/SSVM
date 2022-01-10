package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Pushes field value of an object.
 *
 * @author xDark
 */
public final class GetFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var stack = ctx.getStack();
		var $instance = stack.pop();
		var helper = vm.getHelper();
		helper.checkNotNull($instance);
		var instance = (InstanceValue) $instance;
		var offset = instance.getJavaClass().getFieldOffsetRecursively(insn.name, insn.desc);
		if (offset == -1L) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, insn.owner + '.' + insn.name + insn.desc);
		}
		Value value;
		var manager = vm.getMemoryManager();
		switch (insn.desc) {
			case "J":
				value = new LongValue(manager.readLong(instance, offset));
				break;
			case "D":
				value = new DoubleValue(manager.readDouble(instance, offset));
				break;
			case "I":
				value = new IntValue(manager.readInt(instance, offset));
				break;
			case "F":
				value = new FloatValue(manager.readFloat(instance, offset));
				break;
			case "C":
				value = new IntValue(manager.readChar(instance, offset));
				break;
			case "S":
				value = new IntValue(manager.readShort(instance, offset));
				break;
			case "B":
				value = new IntValue(manager.readByte(instance, offset));
				break;
			case "Z":
				value = new IntValue(manager.readBoolean(instance, offset) ? 1 : 0);
				break;
			default:
				value = manager.readValue(instance, offset);
		}
		if (value == null) {
			System.err.println("NULL VALUE AT " + insn.owner + '.' + insn.name + insn.desc);
			System.err.println(offset);
			System.exit(1);
		}
		stack.pushGeneric(value);
		return Result.CONTINUE;
	}
}
