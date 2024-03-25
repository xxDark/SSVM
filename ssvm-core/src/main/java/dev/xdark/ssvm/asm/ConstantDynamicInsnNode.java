package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ConstantDynamicInsnNode extends DelegatingInsnNode<AbstractInsnNode> {

    private final ConstantDynamic constantDynamic;
    private final InstanceValue result;

    public ConstantDynamicInsnNode(AbstractInsnNode delegate, InstanceValue result, ConstantDynamic constantDynamic) {
        super(delegate, VMOpcodes.VM_CONSTANT_DYNAMIC);
        this.constantDynamic = constantDynamic;
        this.result = result;
    }

    public ConstantDynamic getConstantDynamic() {
        return constantDynamic;
    }

    public InstanceValue getResult() {
        return result;
    }

}
