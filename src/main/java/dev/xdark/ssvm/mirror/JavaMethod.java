package dev.xdark.ssvm.mirror;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Java method.
 *
 * @author xDark
 */
public interface JavaMethod {

	/**
	 * Returns method owner.
	 *
	 * @return method owner.
	 */
	InstanceJavaClass getOwner();

	/**
	 * Returns ASM method info.
	 *
	 * @return ASM method info.
	 */
	MethodNode getNode();

	/**
	 * Returns method slot.
	 *
	 * @return method slot.
	 */
	int getSlot();

	/**
	 * Returns method name.
	 *
	 * @return method name.
	 */
	default String getName() {
		return getNode().name;
	}

	/**
	 * Returns method descriptor.
	 *
	 * @return method descriptor.
	 */
	default String getDesc() {
		return getNode().desc;
	}

	/**
	 * Returns method access.
	 *
	 * @return method access.
	 */
	default int getAccess() {
		return getNode().access;
	}

	/**
	 * Returns method signature.
	 *
	 * @return method signature.
	 */
	default String getSignature() {
		return getNode().signature;
	}

	/**
	 * Returns method type.
	 *
	 * @return method type.
	 */
	Type getType();

	/**
	 * Returns array of types of arguments.
	 *
	 * @return array of types of arguments.
	 */
	Type[] getArgumentTypes();

	/**
	 * Returns method return type.
	 *
	 * @return method return type.
	 */
	Type getReturnType();

	/**
	 * @return {@code  true} if this method is polymorphic,
	 * {@code false} otherwise.
	 */
	boolean isPolymorphic();

	/**
	 * @return the maximum amount of arguments,
	 * including {@code this}.
	 */
	int getMaxArgs();

	/**
	 * @return the maximum amount of stack values.
	 */
	int getMaxStack();

	/**
	 * @return the maximum amount of local variables.
	 */
	int getMaxLocals();

	/**
	 * @return amount of times
	 * this method was invoked.
	 */
	int getInvocationCount();

	/**
	 * Increases invocation count.
	 */
	void increaseInvocation();

	/**
	 * @return {@code true} if this method is caller sensitive,
	 * {@code false} otherwise.
	 */
	boolean isCallerSensitive();

	/**
	 * @return {@code true} if this method is a constructor,
	 * {@code false} otherwise.
	 */
	boolean isConstructor();
}
