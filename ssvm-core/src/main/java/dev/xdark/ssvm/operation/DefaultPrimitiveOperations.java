package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.DoubleValueSink;
import dev.xdark.ssvm.value.sink.FloatValueSink;
import dev.xdark.ssvm.value.sink.IntValueSink;
import dev.xdark.ssvm.value.sink.LongValueSink;
import dev.xdark.ssvm.value.sink.ValueSink;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultPrimitiveOperations implements PrimitiveOperations {

	private final Symbols symbols;
	private final ThreadManager threadManager;
	private final LinkResolver linkResolver;
	private final VMOperations ops;

	@Override
	public long unboxLong(ObjectValue value) {
		ops.checkNotNull(value);
		return invokeUnbox(value, "longValue", "()J", new LongValueSink()).getValue();
	}

	@Override
	public double unboxDouble(ObjectValue value) {
		ops.checkNotNull(value);
		return invokeUnbox(value, "doubleValue", "()D", new DoubleValueSink()).getValue();
	}

	@Override
	public int unboxInt(ObjectValue value) {
		ops.checkNotNull(value);
		return invokeUnbox(value, "intValue", "()I", new IntValueSink()).getValue();
	}

	@Override
	public float unboxFloat(ObjectValue value) {
		ops.checkNotNull(value);
		return invokeUnbox(value, "floatValue", "()F", new FloatValueSink()).getValue();
	}

	@Override
	public char unboxChar(ObjectValue value) {
		ops.checkNotNull(value);
		return (char) invokeUnbox(value, "charValue", "()C", new IntValueSink()).getValue();
	}

	@Override
	public short unboxShort(ObjectValue value) {
		ops.checkNotNull(value);
		return (short) invokeUnbox(value, "shortValue", "()S", new IntValueSink()).getValue();
	}

	@Override
	public byte unboxByte(ObjectValue value) {
		ops.checkNotNull(value);
		return (byte) invokeUnbox(value, "byteValue", "()B", new IntValueSink()).getValue();
	}

	@Override
	public boolean unboxBoolean(ObjectValue value) {
		ops.checkNotNull(value);
		return invokeUnbox(value, "booleanValue", "()Z", new IntValueSink()).getValue() != 0;
	}

	@Override
	public ObjectValue boxLong(long value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Long(),
			"valueOf",
			"(J)Ljava/lang/Long;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setLong(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxDouble(double value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Double(),
			"valueOf",
			"(D)Ljava/lang/Double;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setDouble(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxInt(int value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Integer(),
			"valueOf",
			"(I)Ljava/lang/Integer;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxFloat(float value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Float(),
			"valueOf",
			"(F)Ljava/lang/Float;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setFloat(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxChar(char value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Character(),
			"valueOf",
			"(C)Ljava/lang/Character;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxShort(short value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Short(),
			"valueOf",
			"(S)Ljava/lang/Short;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxByte(byte value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Byte(),
			"valueOf",
			"(B)Ljava/lang/Byte;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return ops.invokeReference(method, locals);
	}

	@Override
	public ObjectValue boxBoolean(boolean value) {
		JavaMethod method = linkResolver.resolveStaticMethod(
			symbols.java_lang_Boolean(),
			"valueOf",
			"(Z)Ljava/lang/Boolean;"
		);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setInt(0, value ? 1 : 0);
		return ops.invokeReference(method, locals);
	}

	private <R extends ValueSink> R invokeUnbox(ObjectValue value, String name, String desc, R sink) {
		JavaMethod method = linkResolver.resolveVirtualMethod(value, name, desc);
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setReference(0, value);
		ops.invoke(method, locals, sink);
		return sink;
	}
}
