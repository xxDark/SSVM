package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.*;
import lombok.val;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Pushes field value of an object.
 *
 * @author xDark
 */
public final class GetFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val owner = helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		if (owner == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.owner);
		}
		val stack = ctx.getStack();
		val instance = helper.<InstanceValue>checkNotNull(stack.pop());
		long offset = helper.getFieldOffset((InstanceJavaClass) owner, instance.getJavaClass(), insn.name, insn.desc);
		if (offset == -1L) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, insn.name);
		}
		Value value;
		val manager = vm.getMemoryManager();
		offset += manager.valueBaseOffset(instance);
		switch (insn.desc) {
			case "J":
				value = LongValue.of(manager.readLong(instance, offset));
				break;
			case "D":
				value = new DoubleValue(manager.readDouble(instance, offset));
				break;
			case "I":
				value = IntValue.of(manager.readInt(instance, offset));
				break;
			case "F":
				value = new FloatValue(manager.readFloat(instance, offset));
				break;
			case "C":
				value = IntValue.of(manager.readChar(instance, offset));
				break;
			case "S":
				value = IntValue.of(manager.readShort(instance, offset));
				break;
			case "B":
				value = IntValue.of(manager.readByte(instance, offset));
				break;
			case "Z":
				value = manager.readBoolean(instance, offset) ? IntValue.ONE : IntValue.ZERO;
				break;
			default:
				value = manager.readValue(instance, offset);
		}
		stack.pushGeneric(value);
		return Result.CONTINUE;
	}
}
