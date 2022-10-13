package dev.xdark.ssvm.jvmti;

/**
 * Thrown when JVMTI code returns abnormally.
 *
 * @author xDark
 */
public final class ToolInterfaceException extends RuntimeException {
	private final Code code;

	/**
	 * @param code JVMTI error code.
	 */
	public ToolInterfaceException(Code code) {
		assert code != Code.JVMTI_ERROR_NONE;
		this.code = code;
	}

	/**
	 * @return Error code.
	 */
	public Code getCode() {
		return code;
	}
}
