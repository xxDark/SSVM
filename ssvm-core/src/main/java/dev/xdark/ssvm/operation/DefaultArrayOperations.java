package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultArrayOperations implements ArrayOperations {

	private final Symbols symbols;
	private final VMOperations ops;

	@Override
	public int getArrayLength(ObjectValue value) {
		return ops.<ArrayValue>checkNotNull(value).getLength();
	}

	@Override
	public ObjectValue arrayLoadReference(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getReference(index);
	}

	@Override
	public long arrayLoadLong(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getLong(index);
	}

	@Override
	public double arrayLoadDouble(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getDouble(index);
	}

	@Override
	public int arrayLoadInt(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getInt(index);
	}

	@Override
	public float arrayLoadFloat(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getFloat(index);
	}

	@Override
	public char arrayLoadChar(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getChar(index);
	}

	@Override
	public short arrayLoadShort(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getShort(index);
	}

	@Override
	public byte arrayLoadByte(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getByte(index);
	}

	@Override
	public boolean arrayLoadBoolean(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getBoolean(index);
	}
	//</editor-fold>

	//<editor-fold desc="Array store methods">

	@Override
	public void arrayStoreReference(ObjectValue array, int index, ObjectValue value) {
		ArrayValue arrayValue = verifyArrayAccess(array, index);
		if (!value.isNull()) {
			JavaClass type = arrayValue.getJavaClass().getComponentType();
			JavaClass valueType = value.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				ops.throwException(symbols.java_lang_ArrayStoreException(), valueType.getName());
			}
		}
		arrayValue.setReference(index, value);
	}

	@Override
	public void arrayStoreLong(ObjectValue array, int index, long value) {
		verifyArrayAccess(array, index).setLong(index, value);
	}

	@Override
	public void arrayStoreDouble(ObjectValue array, int index, double value) {
		verifyArrayAccess(array, index).setDouble(index, value);
	}

	@Override
	public void arrayStoreInt(ObjectValue array, int index, int value) {
		verifyArrayAccess(array, index).setInt(index, value);
	}

	@Override
	public void arrayStoreFloat(ObjectValue array, int index, float value) {
		verifyArrayAccess(array, index).setFloat(index, value);
	}

	@Override
	public void arrayStoreChar(ObjectValue array, int index, char value) {
		verifyArrayAccess(array, index).setChar(index, value);
	}

	@Override
	public void arrayStoreShort(ObjectValue array, int index, short value) {
		verifyArrayAccess(array, index).setShort(index, value);
	}

	@Override
	public void arrayStoreByte(ObjectValue array, int index, byte value) {
		verifyArrayAccess(array, index).setByte(index, value);
	}

	private ArrayValue verifyArrayAccess(ObjectValue value, int index) {
		VMOperations ops = this.ops;
		ArrayValue array = ops.checkNotNull(value);
		ops.arrayRangeCheck(index, array.getLength());
		return array;
	}
}
