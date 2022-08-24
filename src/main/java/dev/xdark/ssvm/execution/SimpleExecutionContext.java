package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;

import java.util.IdentityHashMap;
import java.util.Map;


public final class SimpleExecutionContext<R extends ValueSink> implements ExecutionContext<R>, Disposable {

	private final Map<ObjectValue, LockCount> lockMap = new IdentityHashMap<>();
	private final VirtualMachine virtualMachine;
	private final ExecutionOptions options;
	private final JavaMethod method;
	private final Stack stack;
	private final Locals locals;
	private final R sink;
	private int insnPosition;
	private int lineNumber = -1;

	/**
	 * @param method Method being executed.
	 * @param stack  Execution stack.
	 * @param locals Local variable table.
	 * @param sink   Value sink, where the result will be put.
	 */
	public SimpleExecutionContext(ExecutionOptions options, JavaMethod method, Stack stack, Locals locals, R sink) {
		this.options = options;
		this.virtualMachine = method.getOwner().getVM();
		this.method = method;
		this.stack = stack;
		this.locals = locals;
		this.sink = sink;
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
	public R getResult() {
		return sink;
	}

	@Override
	public void monitorEnter(ObjectValue value) {
		virtualMachine.getHelper().checkNotNull(value);
		lockMap.compute(value, (v, count) -> {
			v.monitorEnter();
			if (count == null) {
				return new LockCount();
			}
			count.value++;
			return count;
		});
	}

	@Override
	public void monitorExit(ObjectValue value) {
		VirtualMachine vm = virtualMachine;
		VMHelper helper = vm.getHelper();
		helper.checkNotNull(value);
		Map<ObjectValue, LockCount> lockMap = this.lockMap;
		LockCount count = lockMap.get(value);
		if (count == null) {
			helper.throwException(vm.getSymbols().java_lang_IllegalMonitorStateException());
		}
		if (--count.value == 0) {
			lockMap.remove(value);
		}
		vm.getPublicOperations().monitorExit(value);
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
						break;
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
	public ExecutionOptions getOptions() {
		return options;
	}

	@Override
	public void setResult(ObjectValue result) {
		sink.acceptReference(result);
	}

	@Override
	public void setResult(long result) {
		sink.acceptLong(result);
	}

	@Override
	public void setResult(double result) {
		sink.acceptDouble(result);
	}

	@Override
	public void setResult(int result) {
		sink.acceptInt(result);
	}

	@Override
	public void setResult(float result) {
		sink.acceptFloat(result);
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
