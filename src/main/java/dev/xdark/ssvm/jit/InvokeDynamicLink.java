package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * InvokeDynamic linking info.
 *
 * @author xDark
 */
public class InvokeDynamicLink {
	InvokeDynamicInsnNode node;
	volatile InstanceValue mh;

	public InvokeDynamicLink(InvokeDynamicInsnNode node) {
		this.node = node;
	}

	InstanceValue resolveInvokeDynamic(ExecutionContext<?> ctx) {
		InstanceValue mh = this.mh;
		if (mh == null) {
			synchronized (this) {
				mh = this.mh;
				if (mh == null) {
					mh = ctx.getInvokeDynamicLinker().linkCall(node, ctx.getOwner());
					node = null;
					this.mh = mh;
				}
			}
		}
		return mh;
	}
}
