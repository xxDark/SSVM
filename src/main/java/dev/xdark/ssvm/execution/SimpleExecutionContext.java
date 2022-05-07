package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.jit.JitHelper;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import dev.xdark.ssvm.value.VoidValue;

import java.util.IdentityHashMap;
import java.util.Map;


public final class SimpleExecutionContext implements ExecutionContext, Disposable {

	private final Map<ObjectValue, LockCount> lockMap = new IdentityHashMap<>();
	private final VirtualMachine virtualMachine;
	private final JavaMethod method;
	private final Stack stack;
	private final Locals locals;
	private int insnPosition;
	private int lineNumber = -1;
	private Value result = VoidValue.INSTANCE; // void by default

	/**
	 * @param method
	 * 		Method being executed.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 */
	public SimpleExecutionContext(JavaMethod method, Stack stack, Locals locals) {
		this.virtualMachine = method.getOwner().getVM();
		this.method = method;
		this.stack = stack;
		this.locals = locals;
	}

	@Override
	public VirtualMachine getVM() {
		return virtualMachine;
	}

	@Override
	public JavaMethod getMethod() {
		return method;
	}

	@Override
	public Stack getStack() {
		return stack;
	}

	@Override
	public Locals getLocals() {
		return locals;
	}

	@Override
	public int getInsnPosition() {
		return insnPosition;
	}

	@Override
	public void setInsnPosition(int insnPosition) {
		this.insnPosition = insnPosition;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public Value getResult() {
		return result;
	}

	@Override
	public void setResult(Value result) {
		if (result == TopValue.INSTANCE) {
			throw new IllegalStateException("Cannot set TOP as the resulting value");
		}
		this.result = result;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public void unwind() {
		stack.clear();
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
