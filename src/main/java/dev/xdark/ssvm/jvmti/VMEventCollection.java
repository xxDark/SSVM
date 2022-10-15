package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.jvmti.event.ClassFileLoad;
import dev.xdark.ssvm.jvmti.event.ClassFilePrepare;

/**
 * All VM events.
 *
 * @author xDark
 */
public interface VMEventCollection {

	/**
	 * @return Class file load hook.
	 */
	ClassFileLoad getClassFileLoad();

	/**
	 * @return Class file prepare hook.
	 */
	ClassFilePrepare getClassFilePrepare();
}
