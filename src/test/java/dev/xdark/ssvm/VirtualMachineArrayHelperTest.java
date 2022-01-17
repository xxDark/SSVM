package dev.xdark.ssvm;

import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
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
		val array = new long[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMLongs(array);
		assertArrayEquals(array, helper.toJavaLongs(wrapper));
	}

	@Test
	public void testDoubleArray() {
		val array = new double[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMDoubles(array);
		assertArrayEquals(array, helper.toJavaDoubles(wrapper));
	}

	@Test
	public void testIntArray() {
		val array = new int[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMInts(array);
		assertArrayEquals(array, helper.toJavaInts(wrapper));
	}

	@Test
	public void testFloatArray() {
		val array = new float[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMFloats(array);
		assertArrayEquals(array, helper.toJavaFloats(wrapper));
	}

	@Test
	public void testCharArray() {
		val array = new char[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMChars(array);
		assertArrayEquals(array, helper.toJavaChars(wrapper));
	}

	@Test
	public void testShortArray() {
		val array = new short[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMShorts(array);
		assertArrayEquals(array, helper.toJavaShorts(wrapper));
	}

	@Test
	public void testByteArray() {
		val array = new byte[]{1, 2, 3, 4, 5};
		val wrapper = helper.toVMBytes(array);
		assertArrayEquals(array, helper.toJavaBytes(wrapper));
	}

	@Test
	public void testBooleanArray() {
		val array = new boolean[]{true, false, true, true, false, true, false};
		val wrapper = helper.toVMBooleans(array);
		assertArrayEquals(array, helper.toJavaBooleans(wrapper));
	}

	@Test
	public void testValueArray() {
		val array = new ObjectValue[] {helper.newUtf8("Hello"), helper.newUtf8("World"), helper.newUtf8("!")};
		val wrapper = helper.toVMValues(array);
		assertArrayEquals(array, helper.toJavaValues(wrapper));
	}
}
