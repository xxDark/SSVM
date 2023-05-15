package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.IntValueSink;
import dev.xdark.ssvm.value.sink.ReferenceValueSink;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author Matt Coley
 */
@RequiredArgsConstructor
public class DefaultValueOperations implements ValueOperations {
	private final Symbols symbols;
	private final VMOperations ops;
	private final ThreadManager threadManager;

	@Override
	public int hashCode(ObjectValue value) {
		JavaMethod hashCode = symbols.java_lang_Object().getMethod("hashCode", "()I");
		Locals locals = threadManager.currentThreadStorage().newLocals(hashCode);
		locals.setReference(0, value);
		return ops.invoke(hashCode, locals, new IntValueSink()).getValue();
	}

	@Override
	public String toString(ObjectValue value) {
		JavaMethod toString = symbols.java_lang_Object().getMethod("toString", "()Ljava/lang/String;");
		Locals locals = threadManager.currentThreadStorage().newLocals(toString);
		locals.setReference(0, value);
		ObjectValue returnValue = ops.invoke(toString, locals, new ReferenceValueSink()).getValue();
		return ops.readUtf8(returnValue);
	}
}
