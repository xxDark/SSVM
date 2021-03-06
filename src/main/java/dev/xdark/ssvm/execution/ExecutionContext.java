package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.gc.GarbageCollector;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

/**
 * Execution context of a method.
 *
 * @author xDark
 */
public interface ExecutionContext {

	/**
	 * @return VM instance.
	 */
	VirtualMachine getVM();

	/**
	 * @return owner's class loader.
	 */
	default ObjectValue getClassLoader() {
		return getOwner().getClassLoader();
	}

	/**
	 * @return VM helper.
	 */
	default VMHelper getHelper() {
		return getVM().getHelper();
	}

	/**
	 * @return VM symbols.
	 */
	default VMSymbols getSymbols() {
		return getVM().getSymbols();
	}

	/**
	 * @return VM primitives.
	 */
	default VMPrimitives getPrimitives() {
		return getVM().getPrimitives();
	}

	/**
	 * @return VM operations.
	 */
	default VMOperations getOperations() {
		return getVM().getPublicOperations();
	}

	/**
	 * @return invokedynamic linker.
	 */
	default InvokeDynamicLinker getInvokeDynamicLinker() {
		return getVM().getInvokeDynamicLinker();
	}

	/**
	 * @return link resolver.
	 */
	default LinkResolver getLinkResolver() {
		return getVM().getPublicLinkResolver();
	}

	/**
	 * @return memory manager.
	 */
	default MemoryManager getMemoryManager() {
		return getVM().getMemoryManager();
	}

	/**
	 * @return safepoint.
	 */
	default SafePoint getSafePoint() {
		return getVM().getSafePoint();
	}

	/**
	 * @return garbage collector.
	 */
	default GarbageCollector getGarbageCollector() {
		return getMemoryManager().getGarbageCollector();
	}

	/**
	 * @return thread storage.
	 */
	default ThreadStorage getThreadStorage() {
		return getVM().getThreadStorage();
	}

	/**
	 * @see SafePoint#poll()
	 */
	default boolean pollSafePoint() {
		return getSafePoint().poll();
	}

	/**
	 * @see SafePoint#pollAndSuspend()
	 */
	default boolean pollSafePointAndSuspend() {
		return getSafePoint().pollAndSuspend();
	}

	/**
	 * @return method being executed.
	 */
	JavaMethod getMethod();

	/**
	 * @return owner of the method.
	 */
	default InstanceJavaClass getOwner() {
		return getMethod().getOwner();
	}

	/**
	 * @return execution stack.
	 */
	Stack getStack();

	/**
	 * @return local variables table.
	 */
	Locals getLocals();

	/**
	 * @return position of currently executing instruction.
	 */
	int getInsnPosition();

	/**
	 * Sets the position of currently executing instruction.
	 *
	 * @param insnPosition New position.
	 */
	void setInsnPosition(int insnPosition);

	/**
	 * @return current line number.
	 */
	int getLineNumber();

	/**
	 * Sets current line number.
	 *
	 * @param lineNumber Line number to set.
	 */
	void setLineNumber(int lineNumber);

	/**
	 * @return execution result.
	 */
	Value getResult();

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	void setResult(Value result);

	/**
	 * Attempts to acquire monitor on an object.
	 *
	 * @param value Object to acquire monitor on.
	 */
	void monitorEnter(ObjectValue value);

	/**
	 * Unlocks monitor of the object.
	 *
	 * @param value Object which monitor to unlock.
	 */
	void monitorExit(ObjectValue value);

	/**
	 * Used by the VM on method exit
	 * to verify that all monitors were unlocked.
	 */
	void verifyMonitors();

	/**
	 * Unwinds the stack,
	 * unlocks object monitors, if any.
	 */
	void unwind();

	/**
	 * @return execution options.
	 */
	ExecutionOptions getOptions();

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(long result) {
		setResult(LongValue.of(result));
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(double result) {
		setResult(new DoubleValue(result));
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(int result) {
		setResult(IntValue.of(result));
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(float result) {
		setResult(new FloatValue(result));
	}
}
