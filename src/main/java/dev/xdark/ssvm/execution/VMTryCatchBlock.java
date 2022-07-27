package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.LabelNode;

/**
 * VM try/catch block.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class VMTryCatchBlock {
	private final LabelNode start, end, handler;
	private final String type;
	private final VirtualMachine vm;
	private final ObjectValue loader;
	private InstanceJavaClass jc;

	/**
	 * @return block start.
	 */
	public LabelNode getStart() {
		return start;
	}

	/**
	 * @return block end.
	 */
	public LabelNode getEnd() {
		return end;
	}

	/**
	 * @return exception handler.
	 */
	public LabelNode getHandler() {
		return handler;
	}

	/**
	 * @return exception type.
	 */
	public InstanceJavaClass getType() {
		String type = this.type;
		if (type == null) {
			return null;
		}
		InstanceJavaClass jc = this.jc;
		if (jc == null) {
			return this.jc = (InstanceJavaClass) vm.findClass(loader, type, false);
		}
		return jc;
	}
}
