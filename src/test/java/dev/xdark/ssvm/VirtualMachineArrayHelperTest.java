package dev.xdark.ssvm;

import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class VirtualMachineArrayHelperTest {

	private static VMHelper helper;

	@BeforeAll
	private static void setup() {
		helper = new VirtualMachine().getHelper();
	}

	@Test
	public void testLongArray() {
		long[] array = new long[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMLongs(array);
		assertArrayEquals(array, helper.toJavaLongs(wrapper));
	}

	@Test
	public void testDoubleArray() {
		double[] array = new double[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMDoubles(array);
		assertArrayEquals(array, helper.toJavaDoubles(wrapper));
	}

	@Test
	public void testIntArray() {
		int[] array = new int[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMInts(array);
		assertArrayEquals(array, helper.toJavaInts(wrapper));
	}

	@Test
	public void testFloatArray() {
		float[] array = new float[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMFloats(array);
		assertArrayEquals(array, helper.toJavaFloats(wrapper));
	}

	@Test
	public void testCharArray() {
		char[] array = new char[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMChars(array);
		assertArrayEquals(array, helper.toJavaChars(wrapper));
	}

	@Test
	public void testShortArray() {
		short[] array = new short[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMShorts(array);
		assertArrayEquals(array, helper.toJavaShorts(wrapper));
	}

	@Test
	public void testByteArray() {
		byte[] array = new byte[]{1, 2, 3, 4, 5};
		ArrayValue wrapper = helper.toVMBytes(array);
		assertArrayEquals(array, helper.toJavaBytes(wrapper));
	}

	@Test
	public void testBooleanArray() {
		boolean[] array = new boolean[]{true, false, true, true, false, true, false};
		ArrayValue wrapper = helper.toVMBooleans(array);
		assertArrayEquals(array, helper.toJavaBooleans(wrapper));
	}

	@Test
	public void testValueArray() {
		ObjectValue[] array = new ObjectValue[] {helper.newUtf8("Hello"), helper.newUtf8("World"), helper.newUtf8("!")};
		ArrayValue wrapper = helper.toVMValues(array);
		assertArrayEquals(array, helper.toJavaValues(wrapper));
	}
}
