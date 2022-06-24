package dev.xdark.ssvm.execution;

import lombok.Builder;
import lombok.Builder.Default;

/**
 * Execution context options.
 *
 * @author xDark
 */
@Builder
public final class ExecutionOptions {
	/**
	 * Whether line numbers should be updated.
	 */
	@Default
	private final boolean setLineNumbers = true;
	/**
	 * Whether the VM should search for VMI invokers.
	 */
	@Deprecated
	@Default
	private final boolean useInvokers = true;
	/**
	 * Whether the VM should notify the hooks
	 * about method enter/exit.
	 */
	@Default
	private final boolean useInvocationHooks = true;
	/**
	 * Whether the VM should acquire lock on an instance/class
	 * if method is marked as synchronized.
	 */
	@Default
	private final boolean useEnterLocking = true;

	public boolean setLineNumbers() {
		return setLineNumbers;
	}

	@Deprecated
	public boolean searchForHooks() {
		return useInvokers;
	}

	public boolean useInvocationHooks() {
		return useInvocationHooks;
	}

	public boolean useEnterLocking() {
		return useEnterLocking;
	}
}
