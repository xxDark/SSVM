package dev.xdark.ssvm.value;

/**
 * Basic VM representation for numeric value, e.g.
 * long, int, double, float, etc.
 *
 * @author xDark
 */
public abstract class NumericValue implements Value {

	@Override
	public <T> T as(Class<T> type) {
		Object result;
		if (type == long.class) result = asLong();
		else if (type == double.class) result = asDouble();
		else if (type == int.class) result = asInt();
		else if (type == float.class) result = asFloat();
		else if (type == char.class) result = asChar();
		else if (type == short.class) result = asShort();
		else if (type == byte.class) result = asByte();
		else if (type == boolean.class) result = asBoolean();
		else throw new IllegalStateException("Cannot represent " + this + " as " + type);
		return (T) result;
	}

	@Override
	public boolean asBoolean() {
		return asByte() != 0;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isVoid() {
		return false;
	}
}
