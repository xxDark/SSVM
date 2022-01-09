package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.InstanceValue;

/**
 * Thrown when error
 * occurs in interpreted code.
 *
 * @author xDark
 */
public final class VMException extends RuntimeException {

	private final InstanceValue oop;

	/**
	 * @param oop
	 * 		Throwable oop.
	 */
	public VMException(InstanceValue oop) {
		//super(null, null, false, false);
		this.oop = oop;
	}

	/**
	 * Returns throwable oop.
	 *
	 * @return throwable oop.
	 */
	public InstanceValue getOop() {
		return oop;
	}
}
