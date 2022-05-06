package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.jit.JitHelper;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import dev.xdark.ssvm.value.VoidValue;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ExecutionContext implements Disposable {

	private final Map<ObjectValue, LockCount> lockMap = new IdentityHashMap<>();
	private final VirtualMachine virtualMachine;
	private final JavaMethod method;
	private final Stack stack;
	private final Locals locals;
	private int insnPosition;
	private int lineNumber = -1;
	private Value result = VoidValue.INSTANCE; // void by default

	/**
	 * @param virtualMachine
	 * 		VM instance.
	 * @param method
	 * 		Method being executed.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 */
	public ExecutionContext(VirtualMachine virtualMachine, JavaMethod method, Stack stack, Locals locals) {
		this.virtualMachine = virtualMachine;
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
	 * Returns VM symbols.
	 *
	 * @return VM symbols.
	 */
	public VMSymbols getSymbols() {
		return virtualMachine.getSymbols();
	}

	/**
	 * Returns owner of the method.
	 *
	 * @return owner of the method.
	 */
	public InstanceJavaClass getOwner() {
		return method.getOwner();
	}

	/**
	 * Returns method being executed.
	 *
	 * @return method being executed.
	 */
	public JavaMethod getMethod() {
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
		if (result == TopValue.INSTANCE) {
			throw new IllegalStateException("Cannot set TOP as the resulting value");
		}
		this.result = result;
	}

	/**
	 * Attempts to acquire monitor on an object.
	 *
	 * @param value
	 * 		Object to acquire monitor on.
	 */
	public void monitorEnter(ObjectValue value) {
		if (value.isNull()) {
			VirtualMachine vm = virtualMachine;
			vm.getHelper().throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		lockMap.compute(value, (v, count) -> {
			v.monitorEnter();
			if (count == null)
				return new LockCount();
			count.value++;
			return count;
		});
	}

	/**
	 * Unlocks monitor of the object.
	 *
	 * @param value
	 * 		Object which monitor to unlock.
	 */
	public void monitorExit(ObjectValue value) {
		if (value.isNull()) {
			VirtualMachine vm = virtualMachine;
			vm.getHelper().throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		Map<ObjectValue, LockCount> lockMap = this.lockMap;
		LockCount count = lockMap.get(value);
		if (count == null) {
			VirtualMachine vm = this.virtualMachine;
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException());
		}
		if (--count.value == 0) {
			lockMap.remove(value);
		}
		JitHelper.tryMonitorExit(value, this);
	}

	/**
	 * Used by the VM on method exit
	 * to verify that all monitors were unlocked.
	 */
	public void verifyMonitors() {
		Map<ObjectValue, LockCount> lockMap = this.lockMap;
		boolean exceptionThrown = !lockMap.isEmpty();
		if (exceptionThrown) {
			for (Map.Entry<ObjectValue, LockCount> entry : lockMap.entrySet()) {
				int count = entry.getValue().value;
				ObjectValue value = entry.getKey();
				while (count-- != 0) {
					try {
						value.monitorExit();
					} catch (IllegalMonitorStateException ignored) {
					}
				}
			}
			lockMap.clear();
		}
		if (exceptionThrown) {
			VirtualMachine vm = virtualMachine;
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException());
		}
	}

	/**
	 * Unwinds the stack,
	 * unlocks object monitors, if any.
	 */
	public void unwind() {
		Stack stack = this.stack;
		Map<ObjectValue, LockCount> lockMap = this.lockMap;
		boolean exceptionThrown = false;
		try {
			Value value;
			while ((value = stack.poll()) != null) {
				if (value instanceof ObjectValue && !value.isNull()) {
					LockCount count = lockMap.remove(value);
					if (count != null) {
						ObjectValue object = (ObjectValue) value;
						int x = count.value;
						while (x-- != 0) {
							try {
								object.monitorExit();
							} catch (IllegalMonitorStateException ignored) {
								exceptionThrown = true;
							}
						}
					}
				}
			}
		} finally {
			stack.clear();
			if (exceptionThrown) {
				VirtualMachine vm = virtualMachine;
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException());
			}
		}
	}

	@Override
	public void dispose() {
		DisposeUtil.dispose(stack);
		DisposeUtil.dispose(locals);
	}

	private static final class LockCount {

		int value;

		LockCount() {
			value = 1;
		}
	}
}
