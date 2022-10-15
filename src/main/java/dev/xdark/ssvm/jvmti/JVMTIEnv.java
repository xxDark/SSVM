package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.jvmti.event.ClassFileLoad;
import dev.xdark.ssvm.jvmti.event.ClassFilePrepare;
import dev.xdark.ssvm.util.Disposable;

/**
 * JVMTI environment.
 *
 * @author xDark
 */
public interface JVMTIEnv extends VMEventCollection, Disposable {

	/**
	 * @return VM associated with this environment.
	 */
	VirtualMachine vm();

	/**
	 * @param cfl Class file load hook.
	 */
	void setClassFileLoad(ClassFileLoad cfl);

	/**
	 * @param cfp Class file prepare hook.
	 */
	void setClassFilePrepare(ClassFilePrepare cfp);

	@Override
	void dispose();
}
