package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * VM invocation operations.
 *
 * @author xDark
 */
public interface InvocationOperations {

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @param sink   Invocation result sink.
	 * @return Invocation result.
	 */
	<R extends ValueSink> R invoke(JavaMethod method, Locals locals, R sink);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 */
	void invokeVoid(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	ObjectValue invokeReference(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	long invokeLong(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	double invokeDouble(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	int invokeInt(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	float invokeFloat(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	short invokeShort(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	char invokeChar(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	byte invokeByte(JavaMethod method, Locals locals);

	/**
	 * @param method Method to invoke.
	 * @param locals Method arguments.
	 * @return Invocation result.
	 */
	boolean invokeBoolean(JavaMethod method, Locals locals);
}
