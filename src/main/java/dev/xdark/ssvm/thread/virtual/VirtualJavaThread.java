package dev.xdark.ssvm.thread.virtual;

import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.thread.OSThread;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Virtual Java thread.
 *
 * @author xDark
 */
final class VirtualJavaThread implements JavaThread {
	private final InstanceValue oop;
	final VirtualOSThread osThread;
	boolean attached;
	Thread foreign;
	// Scheduler state
	boolean interrupted;
	InstanceValue exception; // Thread#stop0/interrupt
	long timeout;

	VirtualJavaThread(InstanceValue oop, VirtualOSThread osThread) {
		this.oop = oop;
		this.osThread = osThread;
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public OSThread getOsThread() {
		return osThread;
	}
}
