package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClassPrepare;
import dev.xdark.ssvm.jvmti.event.MethodEnter;
import dev.xdark.ssvm.jvmti.event.MethodExit;

/**
 * All VM events.
 *
 * @author xDark
 */
public interface VMEventCollection {

	/**
	 * @return Class prepare hook.
	 */
	ClassPrepare getClassPrepare();

	/**
	 * @return Class link hook.
	 */
	ClassLink getClassLink();

	/**
	 * @return Method enter hook.
	 */
	MethodEnter getMethodEnter();

	/**
	 * @return Method exit hook.
	 */
	MethodExit getMethodExit();
}
