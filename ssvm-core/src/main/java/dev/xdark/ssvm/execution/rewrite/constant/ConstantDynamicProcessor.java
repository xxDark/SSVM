package dev.xdark.ssvm.execution.rewrite.constant;

import dev.xdark.ssvm.asm.ConstantDynamicInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.Type;

public class ConstantDynamicProcessor implements InstructionProcessor<ConstantDynamicInsnNode> {
    @Override
    public Result execute(ConstantDynamicInsnNode insn, ExecutionContext<?> ctx) {
        InstanceValue result = insn.getResult();
        VMOperations ops = ctx.getOperations();

        Type condyType = Type.getType(insn.getConstantDynamic().getDescriptor());
        switch (condyType.getSort()) {
            case Type.BOOLEAN:
                ctx.getStack().pushInt(ops.unboxBoolean(result) ? 1 : 0);
                break;
            case Type.CHAR:
                ctx.getStack().pushInt(ops.unboxChar(result));
                break;
            case Type.BYTE:
                ctx.getStack().pushInt(ops.unboxByte(result));
                break;
            case Type.SHORT:
                ctx.getStack().pushInt(ops.unboxShort(result));
                break;
            case Type.INT:
                ctx.getStack().pushInt(ops.unboxInt(result));
                break;
            case Type.LONG:
                ctx.getStack().pushLong(ops.unboxLong(result));
                break;
            case Type.FLOAT:
                ctx.getStack().pushFloat(ops.unboxFloat(result));
                break;
            case Type.DOUBLE:
                ctx.getStack().pushDouble(ops.unboxDouble(result));
                break;
            case Type.OBJECT:
                ctx.getStack().pushReference(result);
                break;
        }
        return Result.CONTINUE;
    }
}
