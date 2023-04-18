package dev.xdark.ssvm.execution;

/**
 * Thrown by the VM indicating that
 * VM *must* crash due to invalid operation, e.g.
 * invalid memory access.
 *
 * @author xDark
 */
public final class PanicException extends RuntimeException {

	public PanicException() {
	}

	public PanicException(String message) {
		super(message);
	}

	public PanicException(String message, Throwable cause) {
		super(message, cause);
	}
}
