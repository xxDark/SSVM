package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.BlackholeValueSink;
import dev.xdark.ssvm.value.sink.DoubleValueSink;
import dev.xdark.ssvm.value.sink.FloatValueSink;
import dev.xdark.ssvm.value.sink.IntValueSink;
import dev.xdark.ssvm.value.sink.LongValueSink;
import dev.xdark.ssvm.value.sink.ReferenceValueSink;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Invocation util.
 *
 * @author xDark
 */
public final class InvocationUtil {

	private final VirtualMachine vm;

	private InvocationUtil(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * @param method     Method to invoke.
	 * @param returnSink Invocation result sink.
	 * @param arguments  Method arguments.
	 * @return Invocation result.
	 */
	public <R extends ValueSink> R invoke(JavaMethod method, R returnSink, Argument... arguments) {
		Locals locals = vm.getThreadStorage().newLocals(method);
		int index = 0;
		for (Argument argument : arguments) {
			index += argument.store(locals, index);
		}
		return vm.getOperations().invoke(method, locals, returnSink);
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 */
	public void invokeVoid(JavaMethod method, Argument... arguments) {
		invoke(method, BlackholeValueSink.INSTANCE, arguments);
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 * @return Invocation result.
	 */
	public ObjectValue invokeReference(JavaMethod method, Argument... arguments) {
		return invoke(method, new ReferenceValueSink(), arguments).getValue();
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 * @return Invocation result.
	 */
	public long invokeLong(JavaMethod method, Argument... arguments) {
		return invoke(method, new LongValueSink(), arguments).getValue();
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 * @return Invocation result.
	 */
	public double invokeDouble(JavaMethod method, Argument... arguments) {
		return invoke(method, new DoubleValueSink(), arguments).getValue();
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 * @return Invocation result.
	 */
	public int invokeInt(JavaMethod method, Argument... arguments) {
		return invoke(method, new IntValueSink(), arguments).getValue();
	}

	/**
	 * @param method    Method to invoke.
	 * @param arguments Method arguments.
	 * @return Invocation result.
	 */
	public float invokeFloat(JavaMethod method, Argument... arguments) {
		return invoke(method, new FloatValueSink(), arguments).getValue();
	}

	/**
	 * @param vm VM instance.
	 * @return Invocation util.
	 */
	public static InvocationUtil create(VirtualMachine vm) {
		return new InvocationUtil(vm);
	}
}
