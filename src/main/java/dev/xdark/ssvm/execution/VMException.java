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
	 * @param message
	 * 		Exception message.
	 * @param oop
	 * 		Throwable oop.
	 */
	public VMException(String message, InstanceValue oop) {
		super(message, null, true, true);
		this.oop = oop;
	}

	/**
	 * @param oop
	 * 		Throwable oop.
	 */
	public VMException(InstanceValue oop) {
		this(null, oop);
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
