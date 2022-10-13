package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default operations.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultVerificationOperations implements VerificationOperations {

	private final Symbols symbols;
	private final ExceptionOperations exceptionOperations;

	@Override
	public ObjectValue checkCast(ObjectValue value, JavaClass klass) {
		if (!value.isNull()) {
			JavaClass against = value.getJavaClass();
			if (!klass.isAssignableFrom(against)) {
				exceptionOperations.throwException(symbols.java_lang_ClassCastException(), against.getName() + " cannot be cast to " + klass.getName());
			}
		}
		return value;
	}

	@Override
	public <V extends ObjectValue> V checkNotNull(ObjectValue value) {
		if (value.isNull()) {
			exceptionOperations.throwException(symbols.java_lang_NullPointerException());
		}
		return (V) value;
	}

	@Override
	public void arrayRangeCheck(int index, int length) {
		if (index < 0 || index >= length) {
			exceptionOperations.throwException(symbols.java_lang_ArrayIndexOutOfBoundsException());
		}
	}

	@Override
	public void arrayLengthCheck(int length) {
		if (length < 0) {
			exceptionOperations.throwException(symbols.java_lang_NegativeArraySizeException());
		}
	}

	@Override
	public void checkEquals(int a, int b) {
		if (a != b) {
			exceptionOperations.throwException(symbols.java_lang_IllegalStateException());
		}
	}

	@Override
	public void checkEquals(long a, long b) {
		if (a != b) {
			exceptionOperations.throwException(symbols.java_lang_IllegalStateException());
		}
	}
}
