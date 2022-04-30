package dev.xdark.ssvm.nt;

/**
 * Native library load result.
 *
 * @apiNote xDark
 */
public final class LibraryLoadResult {
	private final long handle;
	private final int jniVersion;
	private final String errorMessage;

	/**
	 * @param handle
	 * 		Library handle.
	 * @param jniVersion
	 * 		JNI version return by {@code JNI_OnLoad}.
	 * @param errorMessage
	 * 		Error message if library failed to load.
	 */
	public LibraryLoadResult(long handle, int jniVersion, String errorMessage) {
		this.handle = handle;
		this.jniVersion = jniVersion;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return library handle.
	 */
	public long getHandle() {
		return handle;
	}

	/**
	 * @return JNI version return by {@code JNI_OnLoad}.
	 */
	public int getJniVersion() {
		return jniVersion;
	}

	/**
	 * @return error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
