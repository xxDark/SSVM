package dev.xdark.ssvm.jvmti;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClassPrepare;
import dev.xdark.ssvm.jvmti.event.MethodEnter;
import dev.xdark.ssvm.jvmti.event.MethodExit;
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
	void setClassPrepare(ClassPrepare cfp);

	/**
	 * @param cl Class link hook.
	 */
	void setClassLink(ClassLink cl);

	/**
	 * @param enter Method enter hook.
	 */
	void setMethodEnter(MethodEnter enter);

	/**
	 * @param exit Method exit hook.
	 */
	void setMethodExit(MethodExit exit);

	@Override
	void close();
}
