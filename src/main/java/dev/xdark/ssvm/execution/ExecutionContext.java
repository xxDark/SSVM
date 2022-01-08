package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.MethodNode;

public final class ExecutionContext {

	private final VirtualMachine virtualMachine;
	private final InstanceJavaClass owner;
	private final MethodNode method;
	private final Stack stack;
	private final Locals locals;
	private int insnPosition;
	private int lineNumber;
	private Value result;

	/**
	 * @param virtualMachine
	 * 		VM instance.
	 * @param owner
	 * 		Owner of the method.
	 * @param method
	 * 		Method being executed.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 */
	public ExecutionContext(VirtualMachine virtualMachine, InstanceJavaClass owner, MethodNode method, Stack stack, Locals locals) {
		this.virtualMachine = virtualMachine;
		this.owner = owner;
		this.method = method;
		this.stack = stack;
		this.locals = locals;
	}

	/**
	 * Returns VM instance.
	 *
	 * @return VM instance.
	 */
	public VirtualMachine getVM() {
		return virtualMachine;
	}

	/**
	 * Returns VM helper.
	 *
	 * @return VM helper.
	 */
	public VMHelper getHelper() {
		return virtualMachine.getHelper();
	}

	/**
	 * Returns owner of the method.
	 *
	 * @return owner of the method.
	 */
	public InstanceJavaClass getOwner() {
		return owner;
	}

	/**
	 * Returns method being executed.
	 *
	 * @return method being executed.
	 */
	public MethodNode getMethod() {
		return method;
	}

	/**
	 * Returns execution stack.
	 *
	 * @return execution stack.
	 */
	public Stack getStack() {
		return stack;
	}

	/**
	 * Returns local variables table.
	 *
	 * @return local variables table.
	 */
	public Locals getLocals() {
		return locals;
	}

	/**
	 * Returns the position of currently executing instruction.
	 *
	 * @return position of currently executing instruction.
	 */
	public int getInsnPosition() {
		return insnPosition;
	}

	/**
	 * Sets the position of currently executing instruction.
	 *
	 * @param insnPosition
	 * 		New position.
	 */
	public void setInsnPosition(int insnPosition) {
		this.insnPosition = insnPosition;
	}

	/**
	 * Returns current line number.
	 *
	 * @return current line number.
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets current line number.
	 *
	 * @param lineNumber
	 * 		Line number to set.
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Returns execution result.
	 *
	 * @return execution result.
	 */
	public Value getResult() {
		return result;
	}

	/**
	 * Sets execution result.
	 *
	 * @param result
	 * 		Value to set.
	 */
	public void setResult(Value result) {
		this.result = result;
	}
}
