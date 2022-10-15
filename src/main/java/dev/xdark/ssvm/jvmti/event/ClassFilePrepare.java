package dev.xdark.ssvm.jvmti.event;

import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Fired when a class is prepared,
 * before the linkage.
 *
 * @author xDark
 */
@FunctionalInterface
public interface ClassFilePrepare {

	/**
	 * @param klass Class being prepared.
	 */
	void invoke(InstanceClass klass);
}
