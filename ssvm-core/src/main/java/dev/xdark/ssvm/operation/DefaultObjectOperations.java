package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation.
 *
 * @author Matt Coley
 */
@RequiredArgsConstructor
public class DefaultObjectOperations implements ObjectOperations {
	private final Symbols symbols;
	private final VMOperations ops;
	private final ThreadManager threadManager;
	private final LinkResolver linkResolver;

	@Override
	public boolean equals(ObjectValue value1, ObjectValue value2) {
		JavaMethod equals = linkResolver.resolveVirtualMethod(value1.getJavaClass(), "equals", "(Ljava/lang/Object;)Z");
		Locals locals = threadManager.currentThreadStorage().newLocals(equals);
		locals.setReference(0, value1);
		locals.setReference(1, value2);
		return ops.invokeBoolean(equals, locals);
	}

	@NotNull
	@Override
	public ObjectValue getClass(ObjectValue value) {
		JavaMethod getClass = symbols.java_lang_Object().getMethod("getClass", "()Ljava/lang/Class;");
		Locals locals = threadManager.currentThreadStorage().newLocals(getClass);
		locals.setReference(0, value);
		return ops.invokeReference(getClass, locals);
	}

	@Override
	public int hashCode(ObjectValue value) {
		JavaMethod hashCode = linkResolver.resolveVirtualMethod(value.getJavaClass(), "hashCode", "()I");
		Locals locals = threadManager.currentThreadStorage().newLocals(hashCode);
		locals.setReference(0, value);
		return ops.invokeInt(hashCode, locals);
	}

	@Override
	public String toString(ObjectValue value) {
		JavaMethod toString = linkResolver.resolveVirtualMethod(value.getJavaClass(), "toString", "()Ljava/lang/String;");
		Locals locals = threadManager.currentThreadStorage().newLocals(toString);
		locals.setReference(0, value);
		ObjectValue returnValue = ops.invokeReference(toString, locals);
		return ops.readUtf8(returnValue);
	}
}
