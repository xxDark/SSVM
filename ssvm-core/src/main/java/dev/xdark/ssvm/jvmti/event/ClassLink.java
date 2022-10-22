package dev.xdark.ssvm.jvmti.event;

import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Fired after class has been linked.
 * At this point, all class members are set, super classes
 * are loaded, and class can be used.
 *
 * @apiNote xDark
 */
@FunctionalInterface
public interface ClassLink {

	/**
	 * @param klass Linked class.
	 */
	void invoke(InstanceClass klass);
}
