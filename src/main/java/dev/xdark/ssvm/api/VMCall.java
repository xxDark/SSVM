package dev.xdark.ssvm.api;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.MethodNode;

/**
 * Used as a key to locate method invokers.
 *
 * @author xDark
 */
public final class VMCall {

	private final InstanceJavaClass owner;
	private final MethodNode method;

	public VMCall(InstanceJavaClass owner, MethodNode method) {
		this.owner = owner;
		this.method = method;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		VMCall vmCall = (VMCall) o;

		if (!owner.equals(vmCall.owner)) return false;
		return method.equals(vmCall.method);
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + method.hashCode();
		return result;
	}
}
