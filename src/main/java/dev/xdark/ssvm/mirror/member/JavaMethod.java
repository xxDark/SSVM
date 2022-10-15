package dev.xdark.ssvm.mirror.member;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.VMTryCatchBlock;
import dev.xdark.ssvm.mirror.type.JavaClass;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Java method.
 *
 * @author xDark
 */
public interface JavaMethod extends JavaMember {

	@Override
	default String getName() {
		return getNode().name;
	}

	@Override
	default String getDesc() {
		return getNode().desc;
	}

	@Override
	default int getModifiers() {
		return getNode().access;
	}

	@Override
	default String getSignature() {
		return getNode().signature;
	}

	/**
	 * Returns ASM method info.
	 *
	 * @return ASM method info.
	 */
	MethodNode getNode();

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
	JavaClass[] getArgumentTypes();

	/**
	 * Returns method return type.
	 *
	 * @return method return type.
	 */
	JavaClass getReturnType();

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
	 * @return {@code true} if this method is hidden from the stack trace,
	 * {@code false} otherwise.
	 */
	boolean isHidden();

	/**
	 * @return {@code true} if this method is a constructor,
	 * {@code false} otherwise.
	 */
	boolean isConstructor();

	/**
	 * @return a list of try/catch blocks.
	 */
	List<VMTryCatchBlock> getTryCatchBlocks();
}
