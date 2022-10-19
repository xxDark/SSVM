package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClsasPrepare;

/**
 * All VM events.
 *
 * @author xDark
 */
public interface VMEventCollection {

	/**
	 * @return Class prepare hook.
	 */
	ClsasPrepare getClassPrepare();

	/**
	 * @return Class link hook.
	 */
	ClassLink getClassLink();
}
