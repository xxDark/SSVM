package dev.xdark.ssvm;

import dev.xdark.ssvm.util.VMHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class VirtualMachineArrayHelperTest {

	private static VMHelper helper;

	@BeforeAll
	private static void setUp() {
		helper = new VirtualMachine().getHelper();
	}

	@Test
	public void testLongArray() {
		var array = new long[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMLongs(array);
		assertArrayEquals(array, helper.toJavaLongs(wrapper));
	}

	@Test
	public void testDoubleArray() {
		var array = new double[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMDoubles(array);
		assertArrayEquals(array, helper.toJavaDoubles(wrapper));
	}

	@Test
	public void testIntArray() {
		var array = new int[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMInts(array);
		assertArrayEquals(array, helper.toJavaInts(wrapper));
	}

	@Test
	public void testFloatArray() {
		var array = new float[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMFloats(array);
		assertArrayEquals(array, helper.toJavaFloats(wrapper));
	}

	@Test
	public void testCharArray() {
		var array = new char[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMChars(array);
		assertArrayEquals(array, helper.toJavaChars(wrapper));
	}

	@Test
	public void testShortArray() {
		var array = new short[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMShorts(array);
		assertArrayEquals(array, helper.toJavaShorts(wrapper));
	}

	@Test
	public void testByteArray() {
		var array = new byte[]{1, 2, 3, 4, 5};
		var wrapper = helper.toVMBytes(array);
		assertArrayEquals(array, helper.toJavaBytes(wrapper));
	}

	@Test
	public void testBooleanArray() {
		var array = new boolean[]{true, false, true, true, false, true, false};
		var wrapper = helper.toVMBooleans(array);
		assertArrayEquals(array, helper.toJavaBooleans(wrapper));
	}
}
