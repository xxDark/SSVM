package dev.xdark.ssvm;

/**
 * All possible VM initialization states.
 *
 * @author xDark
 */
public enum InitializationState {
	UNINITIALIZED,
	INITIALIZING,
	INITIALIZED,
	BOOTING,
	BOOTED,
	FAILED,
}
