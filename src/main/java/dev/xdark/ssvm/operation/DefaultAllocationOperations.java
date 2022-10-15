package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.type.SimpleArrayClass;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultAllocationOperations implements AllocationOperations {

	private final MemoryManager memoryManager;
	private final Symbols symbols;
	private final Primitives primitives;
	private final ExceptionOperations exceptionOperations;
	private final VerificationOperations verificationOperations;
	private final ClassOperations classOperations;

	@Override
	public InstanceValue allocateInstance(JavaClass klass) {
		InstanceClass jc;
		if (!(klass instanceof InstanceClass) || !(jc = (InstanceClass) klass).canAllocateInstance()) {
			exceptionOperations.throwException(symbols.java_lang_InstantiationError());
			return null;
		}
		classOperations.initialize(jc);
		return memoryManager.newInstance(jc);
	}

	@Override
	public ArrayValue allocateArray(JavaClass componentType, int length) {
		verificationOperations.arrayLengthCheck(length);
		return memoryManager.newArray(componentType.newArrayClass(), length);
	}

	@Override
	public ArrayValue allocateLongArray(int length) {
		return allocateArray(primitives.longPrimitive(), length);
	}

	@Override
	public ArrayValue allocateDoubleArray(int length) {
		return allocateArray(primitives.doublePrimitive(), length);
	}

	@Override
	public ArrayValue allocateIntArray(int length) {
		return allocateArray(primitives.intPrimitive(), length);
	}

	@Override
	public ArrayValue allocateFloatArray(int length) {
		return allocateArray(primitives.floatPrimitive(), length);
	}

	@Override
	public ArrayValue allocateCharArray(int length) {
		return allocateArray(primitives.charPrimitive(), length);
	}

	@Override
	public ArrayValue allocateShortArray(int length) {
		return allocateArray(primitives.shortPrimitive(), length);
	}

	@Override
	public ArrayValue allocateByteArray(int length) {
		return allocateArray(primitives.bytePrimitive(), length);
	}

	@Override
	public ArrayValue allocateBooleanArray(int length) {
		return allocateArray(primitives.booleanPrimitive(), length);
	}

	@Override
	public ArrayValue allocateMultiArray(ArrayClass type, int[] lengths) {
		return newMultiArrayInner(type, lengths, 0);
	}

	private ArrayValue newMultiArrayInner(ArrayClass type, int[] lengths, int depth) {
		JavaClass newType = type.getComponentType();
		MemoryManager memoryManager = this.memoryManager;
		if (!newType.isArray()) {
			return memoryManager.newArray(type, lengths[depth]);
		}
		ArrayValue array = memoryManager.newArray(type, lengths[depth]);
		if (depth == lengths.length - 1) {
			return array;
		}
		int length = lengths[depth];
		int next = depth + 1;
		while (length-- != 0) {
			array.setReference(length, newMultiArrayInner((SimpleArrayClass) newType, lengths, next));
		}
		return array;
	}
}
