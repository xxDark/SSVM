package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;

/**
 * Java thread.
 *
 * @author xDark
 */
public interface JavaThread {

	/**
	 * @return Thread oop.
	 */
	InstanceValue getOop();

	/**
	 * @return OS thread.
	 */
	OSThread getOsThread();
}
