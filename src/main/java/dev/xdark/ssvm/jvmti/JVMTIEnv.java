package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClsasPrepare;
import dev.xdark.ssvm.util.SafeCloseable;

/**
 * JVMTI environment.
 *
 * @author xDark
 */
public interface JVMTIEnv extends VMEventCollection, SafeCloseable {

	/**
	 * @return VM associated with this environment.
	 */
	VirtualMachine vm();

	/**
	 * @param cfp Class prepare hook.
	 */
	void setClassPrepare(ClsasPrepare cfp);

	/**
	 * @param cl Class link hook.
	 */
	void setClassLink(ClassLink cl);

	@Override
	void close();
}
