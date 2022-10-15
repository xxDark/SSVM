package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.memory.management.MemoryManager;
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
public final class DefaultConversionOperations implements ConversionOperations {

	private final Symbols symbols;
	private final MemoryManager memoryManager;
	private final AllocationOperations allocationOperations;

	@Override
	public long[] toJavaLongs(ArrayValue array) {
		int length = array.getLength();
		long[] result = new long[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public double[] toJavaDoubles(ArrayValue array) {
		int length = array.getLength();
		double[] result = new double[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public int[] toJavaInts(ArrayValue array) {
		int length = array.getLength();
		int[] result = new int[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public float[] toJavaFloats(ArrayValue array) {
		int length = array.getLength();
		float[] result = new float[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public char[] toJavaChars(ArrayValue array) {
		int length = array.getLength();
		char[] result = new char[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public short[] toJavaShorts(ArrayValue array) {
		int length = array.getLength();
		short[] result = new short[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public byte[] toJavaBytes(ArrayValue array) {
		int length = array.getLength();
		byte[] result = new byte[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public boolean[] toJavaBooleans(ArrayValue array) {
		int length = array.getLength();
		boolean[] result = new boolean[length];
		array.getMemory().getData().read(memoryManager.arrayBaseOffset(array), result, 0, length);
		return result;
	}

	@Override
	public ObjectValue[] toJavaValues(ArrayValue array) {
		int length = array.getLength();
		ObjectValue[] result = new ObjectValue[length];
		while (length-- != 0) {
			result[length] = array.getReference(length);
		}
		return result;
	}

	@Override
	public ArrayValue toVMLongs(long[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateLongArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMLongs(long[] array) {
		return toVMLongs(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMDoubles(double[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateDoubleArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMDoubles(double[] array) {
		return toVMDoubles(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMInts(int[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateIntArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMFloats(float[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateFloatArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMFloats(float[] array) {
		return toVMFloats(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMChars(char[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateCharArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMChars(char[] array) {
		return toVMChars(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMShorts(short[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateShortArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMShorts(short[] array) {
		return toVMShorts(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMBytes(byte[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateByteArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMBytes(byte[] array) {
		return toVMBytes(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMBooleans(boolean[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateBooleanArray(newLength);
		wrapper.getMemory().getData().write(memoryManager.arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	@Override
	public ArrayValue toVMBooleans(boolean[] array) {
		return toVMBooleans(array, 0, array.length);
	}

	@Override
	public ArrayValue toVMReferences(ObjectValue[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		ArrayValue wrapper = allocationOperations.allocateArray(symbols.java_lang_Object(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setReference(i++, array[startIndex]);
		}
		return wrapper;
	}

	@Override
	public ArrayValue toVMReferences(ObjectValue[] array) {
		return toVMReferences(array, 0, array.length);
	}
}
