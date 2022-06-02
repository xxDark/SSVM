package dev.xdark.ssvm.util;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.mirror.MemberKey;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.ThreadState;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * Provides additional functionality for
 * the VM and simplifies some things.
 *
 * @author xDark
 */
public final class VMHelper {

	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMHelper(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Invokes static method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param method
	 * 		Method to invoke.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeStatic(InstanceJavaClass javaClass, JavaMethod method, Value[] stack, Value[] locals) {
		if ((method.getAccess() & Opcodes.ACC_STATIC) == 0) {
			throw new IllegalStateException("Method is not static");
		}
		javaClass.initialize();
		ExecutionContext ctx = createContext(javaClass, method, locals);
		contextPrepare(ctx, stack, locals);
		vm.execute(ctx);
		return ctx;
	}

	/**
	 * Invokes static method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeStatic(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		/*
		JavaMethod mn = javaClass.getStaticMethodRecursively(name, desc);
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError(), javaClass.getInternalName() + '.' + name + desc);

		}
		return invokeStatic(javaClass, mn, stack, locals);
		*/
		JavaMethod m = vm.getLinkResolver().resolveStaticMethod(javaClass, name, desc);
		return invokeStatic(javaClass, m, stack, locals);
	}

	/**
	 * Invokes virtual method.
	 *
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeVirtual(String name, String desc, Value[] stack, Value[] locals) {
		Value instance = locals[0];
		checkNotNull(instance);
		InstanceJavaClass javaClass;
		if (instance instanceof ArrayValue) {
			javaClass = vm.getSymbols().java_lang_Object();
		} else {
			javaClass = ((InstanceValue) instance).getJavaClass();
		}
		JavaMethod m = vm.getLinkResolver().resolveVirtualMethod(javaClass, javaClass, name, desc);
		return invokeExact(javaClass, m, stack, locals);
		/*
		JavaMethod m = javaClass.getVirtualMethodRecursively(name, desc);
		if (m == null) {
			// Perform invokeInterface call.
			return invokeInterface(javaClass, name, desc, stack, locals);
		}
		return invokeExact(javaClass, m, stack, locals);
		*/
	}

	/**
	 * Invokes interface method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeInterface(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		Value instance = locals[0];
		checkNotNull(instance);
		InstanceJavaClass prioritized = ((InstanceValue) instance).getJavaClass();
		JavaMethod mn = vm.getLinkResolver().resolveVirtualMethod(prioritized, javaClass, name, desc);
		/*
		JavaMethod mn = prioritized.getInterfaceMethodRecursively(name, desc);
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError(), javaClass.getInternalName() + '.' + name + desc);
		}
		*/
		return invokeExact(mn.getOwner(), mn, stack, locals);
	}

	/**
	 * Invokes exact method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param method
	 * 		Method to invoke.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeExact(InstanceJavaClass javaClass, JavaMethod method, Value[] stack, Value[] locals) {
		if (locals[0].isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalStateException("Method is static");
		}
		javaClass.initialize();
		ExecutionContext ctx = createContext(javaClass, method, locals);
		contextPrepare(ctx, stack, locals);
		vm.execute(ctx);
		return ctx;
	}

	/**
	 * Invokes exact method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeExact(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		/*
		JavaMethod mn = javaClass.getVirtualMethodRecursively(name, desc);
		if (mn == null) {
			mn = javaClass.getInterfaceMethodRecursively(name, desc);
			if (mn != null && (mn.getAccess() & Opcodes.ACC_ABSTRACT) != 0) {
				mn = null;
			}
		}
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError(), javaClass.getInternalName() + '.' + name + desc);
		}
		*/
		JavaMethod mn = vm.getLinkResolver().resolveSpecialMethod(javaClass, name, desc);
		return invokeExact(javaClass, mn, stack, locals);
	}

	/**
	 * Creates VM vales from constant.
	 *
	 * @return VM value.
	 *
	 * @throws IllegalStateException
	 * 		If constant value cannot be created.
	 */
	public Value valueFromLdc(Object cst) {
		VirtualMachine vm = this.vm;
		if (cst instanceof Long) {
			return LongValue.of((Long) cst);
		}
		if (cst instanceof Double) {
			return new DoubleValue((Double) cst);
		}
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte) {
			return IntValue.of(((Number) cst).intValue());
		}
		if (cst instanceof Character) {
			return IntValue.of((Character) cst);
		}
		if (cst instanceof Float) {
			return new FloatValue((Float) cst);
		}
		if (cst instanceof Boolean) {
			return (Boolean) cst ? IntValue.ONE : IntValue.ZERO;
		}
		if (cst instanceof String) {
			return vm.getStringPool().intern((String) cst);
		}
		if (cst instanceof Type) {
			Type type = (Type) cst;
			StackFrame ctx = vm.currentThread().getBacktrace().last();
			InstanceJavaClass caller = ctx == null ? null : ctx.getDeclaringClass();
			ObjectValue loader = caller == null ? NullValue.INSTANCE : caller.getClassLoader();
			int sort = type.getSort();
			switch(sort) {
				case Type.OBJECT:
					String internalName = type.getInternalName();
					if (caller != null && Modifier.isHiddenMember(caller.getModifiers()) && internalName.equals(caller.getInternalName())) {
						return caller.getOop();
					}
					return vm.findClass(loader, internalName, false).getOop();
				case Type.METHOD:
					return methodType(loader, type);
				default:
					return findClass(loader, type.getInternalName(), false).getOop();
			}
		}

		if (cst instanceof Handle) {
			StackFrame ctx = vm.currentThread().getBacktrace().last();
			return linkMethodHandleConstant(ctx.getDeclaringClass(), (Handle) cst);
		}

		throw new UnsupportedOperationException("TODO: " + cst + " (" + cst.getClass() + ')');
	}

	/**
	 * Converts an array to {@code long[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public long[] toJavaLongs(ArrayValue array) {
		int length = array.getLength();
		long[] result = new long[length];
		while(length-- != 0) {
			result[length] = array.getLong(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code double[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public double[] toJavaDoubles(ArrayValue array) {
		int length = array.getLength();
		double[] result = new double[length];
		while(length-- != 0) {
			result[length] = array.getDouble(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code int[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public int[] toJavaInts(ArrayValue array) {
		int length = array.getLength();
		int[] result = new int[length];
		while(length-- != 0) {
			result[length] = array.getInt(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code float[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public float[] toJavaFloats(ArrayValue array) {
		int length = array.getLength();
		float[] result = new float[length];
		while(length-- != 0) {
			result[length] = array.getFloat(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code char[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public char[] toJavaChars(ArrayValue array) {
		int length = array.getLength();
		char[] result = new char[length];
		while(length-- != 0) {
			result[length] = array.getChar(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code short[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public short[] toJavaShorts(ArrayValue array) {
		int length = array.getLength();
		short[] result = new short[length];
		while(length-- != 0) {
			result[length] = array.getShort(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code byte[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public byte[] toJavaBytes(ArrayValue array) {
		int length = array.getLength();
		byte[] result = new byte[length];
		while(length-- != 0) {
			result[length] = array.getByte(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code boolean[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public boolean[] toJavaBooleans(ArrayValue array) {
		int length = array.getLength();
		boolean[] result = new boolean[length];
		while(length-- != 0) {
			result[length] = array.getBoolean(length);
		}
		return result;
	}

	/**
	 * Converts an array to {@code Value[]} array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return native Java array.
	 */
	public Value[] toJavaValues(ArrayValue array) {
		int length = array.getLength();
		Value[] result = new Value[length];
		while(length-- != 0) {
			result[length] = array.getValue(length);
		}
		return result;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMLongs(long[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().longPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setLong(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMLongs(long[] array) {
		return toVMLongs(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMDoubles(double[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().doublePrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setDouble(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMDoubles(double[] array) {
		return toVMDoubles(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMInts(int[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().intPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setInt(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMInts(int[] array) {
		return toVMInts(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMFloats(float[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().floatPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setFloat(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMFloats(float[] array) {
		return toVMFloats(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMChars(char[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().charPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setChar(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMChars(char[] array) {
		return toVMChars(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMShorts(short[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().shortPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setShort(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMShorts(short[] array) {
		return toVMShorts(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMBytes(byte[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().bytePrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setByte(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMBytes(byte[] array) {
		return toVMBytes(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMBooleans(boolean[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().booleanPrimitive(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setBoolean(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMBooleans(boolean[] array) {
		return toVMBooleans(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 * @param startIndex
	 * 		The initial index of the range to be converted, inclusive.
	 * @param endIndex
	 * 		The final index of the range to be converted, exclusive.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMValues(ObjectValue[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getSymbols().java_lang_Object(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setValue(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array
	 * 		Array to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMValues(ObjectValue[] array) {
		return toVMValues(array, 0, array.length);
	}

	/**
	 * Converts VM string to Java string.
	 *
	 * @param value
	 * 		VM string.
	 *
	 * @return Java string.
	 */
	public String readUtf8(InstanceValue value) {
		InstanceJavaClass jc = value.getJavaClass();
		VirtualMachine vm = jc.getVM();
		if (jc != vm.getSymbols().java_lang_String()) {
			throw new IllegalStateException("Not a string: " + value + " (" + jc + ')');
		}
		long off = value.getFieldOffset("value", "[C");
		ArrayValue array;
		if (off != -1L) {
			array = (ArrayValue) vm.getMemoryManager().readValue(value, off);
		} else {
			array = (ArrayValue) invokeExact(jc, "toCharArray", "()[C", new Value[0], new Value[]{value}).getResult();
		}
		return new String(toJavaChars(array));
	}

	/**
	 * Converts VM string to Java string.
	 *
	 * @param value
	 * 		VM string.
	 *
	 * @return Java string.
	 */
	public String readUtf8(Value value) {
		if (value.isNull()) {
			return null;
		}
		return readUtf8((InstanceValue) value);
	}

	/**
	 * Allocates VM string.
	 *
	 * @param str
	 * 		Java string.
	 * @param pool
	 * 		True if string should be pooled.
	 *
	 * @return VM string.
	 */
	public ObjectValue newUtf8(String str, boolean pool) {
		if (str == null) {
			return NullValue.INSTANCE;
		}
		VirtualMachine vm = this.vm;
		InstanceJavaClass jc = vm.getSymbols().java_lang_String();
		jc.initialize();
		if (pool) {
			InstanceValue pooled = vm.getStringPool().getIfPresent(str);
			if (pooled != null) {
				return pooled;
			}
		}
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue wrapper = memoryManager.newInstance(jc);
		long off = wrapper.getFieldOffset("value", "[C");
		boolean jdk8 = off != -1L;
		if (str.isEmpty()) {
			VMPrimitives primitives = vm.getPrimitives();
			if (jdk8) {
				memoryManager.writeValue(wrapper, off, emptyArray(primitives.charPrimitive()));
			} else {
				wrapper.setValue("value", "[B", emptyArray(primitives.bytePrimitive()));
			}
			wrapper.initialize();
		} else {
			if (jdk8) {
				memoryManager.writeValue(wrapper, off, toVMChars(str));
				wrapper.initialize();
			} else {
				invokeExact(jc, "<init>", "([C)V", new Value[0], new Value[]{wrapper, toVMChars(str)});
			}
		}
		return wrapper;
	}

	/**
	 * Allocates VM string.
	 *
	 * @param str
	 * 		Java string.
	 *
	 * @return VM string.
	 */
	public ObjectValue newUtf8(String str) {
		return newUtf8(str, false);
	}

	/**
	 * Allocates VM string.
	 *
	 * @param chars
	 * 		String chars.
	 *
	 * @return VM string.
	 */
	public ObjectValue newUtf8(ArrayValue chars) {
		VirtualMachine vm = this.vm;
		InstanceJavaClass jc = vm.getSymbols().java_lang_String();
		jc.initialize();
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue wrapper = memoryManager.newInstance(jc);
		long off = wrapper.getFieldOffset("value", "[C");
		boolean jdk8 = off != -1L;
		if (jdk8) {
			memoryManager.writeValue(wrapper, off, chars);
			wrapper.initialize();
		} else {
			invokeExact(jc, "<init>", "([C)V", new Value[0], new Value[]{wrapper, chars});
		}
		return wrapper;
	}

	/**
	 * Initializes default static values of the class.
	 *
	 * @param javaClass
	 * 		Class to set fields for.
	 */
	public void initializeStaticFields(InstanceJavaClass javaClass) {
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue oop = javaClass.getOop();
		long baseOffset = memoryManager.getStaticOffset(javaClass);
		Map<MemberKey, JavaField> fields = javaClass.getStaticFieldLayout().getFields();
		for (Map.Entry<MemberKey, JavaField> entry : fields.entrySet()) {
			MemberKey key = entry.getKey();
			String desc = key.getDesc();
			JavaField jf = entry.getValue();
			FieldNode fn = jf.getNode();
			Object cst = fn.value;
			if (cst == null) {
				cst = AsmUtil.getDefaultValue(desc);
			}
			long offset = jf.getOffset();
			long resultingOffset = baseOffset + offset;
			switch(desc.charAt(0)) {
				case 'J':
					memoryManager.writeLong(oop, resultingOffset, (Long) cst);
					break;
				case 'D':
					memoryManager.writeDouble(oop, resultingOffset, (Double) cst);
					break;
				case 'I':
					memoryManager.writeInt(oop, resultingOffset, (Integer) cst);
					break;
				case 'F':
					memoryManager.writeFloat(oop, resultingOffset, (Float) cst);
					break;
				case 'C':
					memoryManager.writeChar(oop, resultingOffset, (char) ((Integer) cst).intValue());
					break;
				case 'S':
					memoryManager.writeShort(oop, resultingOffset, ((Integer) cst).shortValue());
					break;
				case 'B':
				case 'Z':
					memoryManager.writeByte(oop, resultingOffset, ((Integer) cst).byteValue());
					break;
				default:
					memoryManager.writeValue(oop, resultingOffset, cst == null ? NullValue.INSTANCE : (ObjectValue) valueFromLdc(cst));
			}
		}
	}

	/**
	 * Initializes default values of the class.
	 *
	 * @param value
	 * 		Value to set fields for.
	 */
	public void initializeDefaultValues(InstanceValue value) {
		VirtualMachine vm = this.vm;
		MemoryManager memoryManager = vm.getMemoryManager();
		long baseOffset = memoryManager.valueBaseOffset(value);
		for (JavaField entry : value.getJavaClass().getVirtualFieldLayout().getAll()) {
			String field = entry.getNode().desc;
			long offset = baseOffset + entry.getOffset();
			switch(field) {
				case "J":
					memoryManager.writeLong(value, offset, 0L);
					break;
				case "D":
					memoryManager.writeDouble(value, offset, 0.0D);
					break;
				case "I":
					memoryManager.writeInt(value, offset, 0);
					break;
				case "F":
					memoryManager.writeFloat(value, offset, 0.0F);
					break;
				case "C":
					memoryManager.writeChar(value, offset, '\0');
					break;
				case "S":
					memoryManager.writeShort(value, offset, (short) 0);
					break;
				case "B":
					memoryManager.writeByte(value, offset, (byte) 0);
					break;
				case "Z":
					memoryManager.writeBoolean(value, offset, false);
					break;
				default:
					memoryManager.writeValue(value, offset, NullValue.INSTANCE);
			}
		}
	}

	/**
	 * Modifies VM oop according to native thread.
	 *
	 * @param vmThread
	 * 		Thread to modify.
	 */
	public void screenVmThread(VMThread vmThread) {
		Thread javaThread = vmThread.getJavaThread();
		InstanceValue oop = vmThread.getOop();
		// Copy thread name
		oop.setValue("name", "Ljava/lang/String;", newUtf8(javaThread.getName()));
		// Copy thread priority
		oop.setInt("priority", javaThread.getPriority());
		// Copy daemon status
		oop.setBoolean("daemon", javaThread.isDaemon());
		// Copy thread state (JVMTI_THREAD_STATE_RUNNABLE | JVMTI_THREAD_STATE_ALIVE)
		oop.setInt("threadStatus", ThreadState.JVMTI_THREAD_STATE_RUNNABLE | ThreadState.JVMTI_THREAD_STATE_ALIVE);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param message
	 * 		Exception message.
	 * @param cause
	 * 		Exception cause.
	 *
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, String message, ObjectValue cause) {
		VirtualMachine vm = this.vm;
		javaClass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(javaClass);
		invokeExact(javaClass, "<init>", "()V", new Value[0], new Value[]{instance});
		if (message != null) {
			instance.setValue("detailMessage", "Ljava/lang/String;", newUtf8(message));
		}
		if (cause != null) {
			instance.setValue("cause", "Ljava/lang/Throwable;", cause);
		}
		return instance;
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param message
	 * 		Exception message.
	 *
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, String message) {
		return newException(javaClass, message, null);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param cause
	 * 		Exception cause.
	 *
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, ObjectValue cause) {
		return newException(javaClass, null, cause);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 *
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass) {
		return newException(javaClass, null, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param message
	 * 		Message.
	 * @param cause
	 * 		Exception cause.
	 */
	public void throwException(InstanceJavaClass javaClass, String message, ObjectValue cause) {
		throw new VMException(newException(javaClass, message, cause));
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param message
	 * 		Exception Message.
	 */
	public void throwException(InstanceJavaClass javaClass, String message) {
		throwException(javaClass, message, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 * @param cause
	 * 		Exception cause.
	 */
	public void throwException(InstanceJavaClass javaClass, ObjectValue cause) {
		throwException(javaClass, null, cause);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass
	 * 		Exception class.
	 */
	public void throwException(InstanceJavaClass javaClass) {
		throwException(javaClass, null, null);
	}

	/**
	 * Performs array bounds check.
	 *
	 * @param array
	 * 		Array to check in.
	 * @param index
	 * 		Index to check.
	 */
	public void rangeCheck(ArrayValue array, int index) {
		if (index < 0 || index >= array.getLength()) {
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException());
		}
	}

	/**
	 * Performs array length check.
	 *
	 * @param length
	 * 		Length to check.
	 */
	public void checkArrayLength(int length) {
		if (length < 0) {
			throwException(vm.getSymbols().java_lang_NegativeArraySizeException());
		}
	}

	/**
	 * Performs null check.
	 *
	 * @param value
	 * 		Value to check.
	 * @param <V>
	 * 		New value type after null check.
	 *
	 * @return value.
	 */
	public <V extends ObjectValue> V checkNotNull(Value value) {
		if (value.isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		return (V) value;
	}

	/**
	 * Checks whether array is nonnull.
	 *
	 * @param value
	 * 		Array to check.
	 */
	public ArrayValue checkNotNullArray(ObjectValue value) {
		checkNotNull(value);
		return checkArray(value);
	}

	/**
	 * Performs array check.
	 *
	 * @param value
	 * 		Value to check.
	 *
	 * @return array value if cast succeeds.
	 */
	public ArrayValue checkArray(Value value) {
		if (value.isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		if (!(value instanceof ArrayValue)) {
			throwException(vm.getSymbols().java_lang_InternalError(), "not an array");
		}
		return (ArrayValue) value;
	}

	/**
	 * Performs bounds check.
	 *
	 * @param value
	 * 		Value to check.
	 * @param from
	 * 		Minimmm value.
	 * @param to
	 * 		Maximum value.
	 */
	public void rangeCheck(int value, int from, int to) {
		if (value < from || value >= to) {
			throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
	}

	/**
	 * Performs equality check.
	 *
	 * @param a
	 * 		Left value.
	 * @param b
	 * 		Right value.
	 */
	public void checkEquals(int a, int b) {
		if (a != b) {
			throwException(vm.getSymbols().java_lang_IllegalStateException());
		}
	}

	/**
	 * Sets class fields, just like normal JVM.
	 *
	 * @param oop
	 * 		Class to set fields for.
	 * @param classLoader
	 * 		Class loader.
	 * @param protectionDomain
	 * 		Protection domain of the class.
	 */
	public void setClassFields(InstanceValue oop, ObjectValue classLoader, ObjectValue protectionDomain) {
		if (!classLoader.isNull()) {
			oop.setValue("classLoader", "Ljava/lang/ClassLoader;", classLoader);
			oop.setValue(NativeJava.PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;", protectionDomain);
		}
	}

	/**
	 * Defines class.
	 *
	 * @param classLoader
	 * 		Class loader to define class in.
	 * @param name
	 * 		Class name.
	 * @param b
	 * 		Class bytes.
	 * @param off
	 * 		Class bytes offset.
	 * @param len
	 * 		Class bytes length.
	 * @param protectionDomain
	 * 		Protection domain.
	 * @param source
	 * 		Class source, e.g. it's location
	 * @param linkToLoader
	 * 		Whether the class should be linked.
	 *
	 * @return defined class.
	 */
	public InstanceJavaClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source, boolean linkToLoader) {
		VirtualMachine vm = this.vm;
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException());
		}
		ClassLoaderData classLoaderData = vm.getClassLoaders().getClassLoaderData(classLoader);
		ClassParseResult parsed = vm.getClassDefiner().parseClass(name, b, off, len, source);
		if (parsed == null) {
			throwException(vm.getSymbols().java_lang_NoClassDefFoundError(), name);
		}
		String classReaderName = parsed.getClassReader().getClassName();
		if (name == null) {
			name = classReaderName;
		} else if (!classReaderName.equals(name.replace('.', '/'))) {
			throwException(vm.getSymbols().java_lang_ClassNotFoundException(), "Expected class name " + classReaderName.replace('/', '.') + " but received: " + name);
		}
		if (name.contains("[")) {
			throwException(vm.getSymbols().java_lang_NoClassDefFoundError(), "Bad class name: " + classReaderName);
		}
		if (!linkToLoader) {
			InstanceJavaClass javaClass = newInstanceClass(classLoader, protectionDomain, parsed.getClassReader(), parsed.getNode());
			javaClass.link();
			return javaClass;
		}
		synchronized(classLoaderData) {
			if (classLoaderData.getClass(name) != null) {
				throwException(vm.getSymbols().java_lang_ClassNotFoundException(), "Duplicate class name: " + name);
			}
			InstanceJavaClass javaClass = newInstanceClass(classLoader, protectionDomain, parsed.getClassReader(), parsed.getNode());
			classLoaderData.linkClass(javaClass);
			return javaClass;
		}
	}

	/**
	 * Defines class.
	 *
	 * @param classLoader
	 * 		Class loader to define class in.
	 * @param name
	 * 		Class name.
	 * @param b
	 * 		Class bytes.
	 * @param off
	 * 		Class bytes offset.
	 * @param len
	 * 		Class bytes length.
	 * @param protectionDomain
	 * 		Protection domain.
	 * @param source
	 * 		Class source, e.g. it's location
	 *
	 * @return defined class.
	 */
	public InstanceJavaClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source) {
		return defineClass(classLoader, name, b, off, len, protectionDomain, source, true);
	}

	/**
	 * Creates new {@link InstanceJavaClass}.
	 *
	 * @param loader
	 * 		Class loader.
	 * @param protectionDomain
	 * 		Protection domain.
	 * @param reader
	 * 		Class source.
	 * @param node
	 * 		Class node
	 */
	public InstanceJavaClass newInstanceClass(ObjectValue loader, ObjectValue protectionDomain, ClassReader reader, ClassNode node) {
		VirtualMachine vm = this.vm;
		ClassLoaders classLoaders = vm.getClassLoaders();
		InstanceJavaClass javaClass = classLoaders.constructClass(loader, reader, node);
		classLoaders.setClassOop(javaClass);
		InstanceValue oop = javaClass.getOop();
		initializeDefaultValues(oop);
		setClassFields(oop, loader, protectionDomain);
		if (!loader.isNull()) {
			ObjectValue classes = ((InstanceValue) loader).getValue("classes", "Ljava/util/Vector;");
			invokeVirtual("add", "(Ljava/lang/Object;)Z", new Value[0], new Value[]{classes, javaClass.getOop()});
		}
		return javaClass;
	}

	/**
	 * Sets array class component type.
	 *
	 * @param javaClass
	 * 		Class to set component for.
	 * @param componentType
	 * 		Type of the component.
	 */
	public void setComponentType(ArrayJavaClass javaClass, JavaClass componentType) {
		InstanceValue oop = javaClass.getOop();
		long offset = oop.getFieldOffset("componentType", "Ljava/lang/Class;");
		if (offset != -1L) {
			vm.getMemoryManager().writeValue(oop, offset, componentType.getOop());
		}
	}

	/**
	 * Converts VM exception to Java exception.
	 *
	 * @param oop
	 * 		VM exception to convert.
	 *
	 * @return Java exception.
	 */
	public Exception toJavaException(InstanceValue oop) {
		String msg = readUtf8(oop.getValue("detailMessage", "Ljava/lang/String;"));
		Exception exception = new Exception(msg);
		ObjectValue backtrace = oop.getValue("backtrace", "Ljava/lang/Object;");
		if (!backtrace.isNull()) {
			Backtrace unmarshalled = ((JavaValue<Backtrace>) backtrace).getValue();
			StackTraceElement[] stackTrace = StreamSupport.stream(unmarshalled.spliterator(), false)
					.map(frame -> {
						String methodName = frame.getMethodName();
						InstanceJavaClass owner = frame.getDeclaringClass();
						String className = owner.getName();
						String sourceFile = frame.getSourceFile();
						int lineNumber = frame.getLineNumber();
						return new StackTraceElement(className, methodName, sourceFile, lineNumber);
					})
					.toArray(StackTraceElement[]::new);
			Collections.reverse(Arrays.asList(stackTrace));
			exception.setStackTrace(stackTrace);
		}
		ObjectValue cause = oop.getValue("cause", "Ljava/lang/Throwable;");
		if (!cause.isNull() && cause != oop) {
			exception.initCause(toJavaException((InstanceValue) cause));
		}
		ObjectValue suppressedExceptions = oop.getValue("suppressedExceptions", "Ljava/util/List;");
		if (!suppressedExceptions.isNull()) {
			InstanceJavaClass list = (InstanceJavaClass) vm.findBootstrapClass("java/util/List");
			int size = invokeInterface(list, "size", "()I", new Value[0], new Value[]{suppressedExceptions}).getResult().asInt();
			for (int i = 0; i < size; i++) {
				Value ex = invokeInterface(list, "get", "(I)Ljava/lang/Object;", new Value[0], new Value[]{suppressedExceptions, IntValue.of(i)}).getResult();
				exception.addSuppressed(toJavaException((InstanceValue) ex));
			}
		}
		return exception;
	}

	/**
	 * Constructs new VM StackTraceElement from backtrace frame.
	 *
	 * @param frame
	 * 		Java frame.
	 * @param injectDeclaringClass
	 * 		See {@link StackTraceElement#declaringClassObject} description.
	 *
	 * @return VM StackTraceElement.
	 */
	public InstanceValue newStackTraceElement(StackFrame frame, boolean injectDeclaringClass) {
		String methodName = frame.getMethodName();
		InstanceJavaClass owner = frame.getDeclaringClass();
		String className = owner.getName();
		String sourceFile = frame.getSourceFile();
		int lineNumber = frame.getLineNumber();
		VirtualMachine vm = this.vm;
		InstanceJavaClass jc = vm.getSymbols().java_lang_StackTraceElement();
		jc.initialize();
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue element = memoryManager.newInstance(jc);
		invokeExact(jc, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", new Value[0], new Value[]{
				element,
				newUtf8(className),
				newUtf8(methodName),
				newUtf8(sourceFile),
				IntValue.of(lineNumber)
		});
		long offset;
		if (injectDeclaringClass && (offset = element.getFieldOffset("declaringClassObject", "Ljava/lang/Class;")) != -1L) {
			memoryManager.writeValue(element, offset, owner.getOop());
		}
		return element;
	}

	public JavaClass findClass(ObjectValue loader, String name, boolean initialize) {
		int dimensions = 0;
		while(name.charAt(dimensions) == '[') {
			dimensions++;
		}
		if (dimensions != 0) {
			name = name.substring(dimensions);
		}
		JavaClass klass;
		if (name.length() == 1) {
			VMPrimitives primitives = vm.getPrimitives();
			switch(name.charAt(0)) {
				case 'J':
					klass = primitives.longPrimitive();
					break;
				case 'D':
					klass = primitives.doublePrimitive();
					break;
				case 'I':
					klass = primitives.intPrimitive();
					break;
				case 'F':
					klass = primitives.floatPrimitive();
					break;
				case 'C':
					klass = primitives.charPrimitive();
					break;
				case 'S':
					klass = primitives.shortPrimitive();
					break;
				case 'B':
					klass = primitives.bytePrimitive();
					break;
				case 'Z':
					klass = primitives.booleanPrimitive();
					break;
				case 'V':
					klass = primitives.voidPrimitive();
					break;
				default:
					klass = vm.findClass(loader, name, initialize);
			}
		} else {
			if (dimensions != 0) {
				name = name.substring(1, name.length() - 1);
			}
			find:
			{
				StackFrame ctx = vm.currentThread().getBacktrace().last();
				if (ctx != null) {
					InstanceJavaClass caller = ctx.getDeclaringClass();
					if (caller.getClassLoader() == loader && name.equals(caller.getInternalName())) {
						klass = caller;
						break find;
					}
				}
				klass = vm.findClass(loader, name, initialize);
			}
		}
		while(dimensions-- != 0) {
			klass = klass.newArrayClass();
		}
		return klass;
	}

	/**
	 * Unwraps long value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public LongValue unboxLong(ObjectValue value) {
		checkNotNull(value);
		return (LongValue) invokeVirtual("longValue", "()J", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps double value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public DoubleValue unboxDouble(ObjectValue value) {
		checkNotNull(value);
		return (DoubleValue) invokeVirtual("doubleValue", "()D", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps int value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public IntValue unboxInt(ObjectValue value) {
		checkNotNull(value);
		return (IntValue) invokeVirtual("intValue", "()I", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps float value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public FloatValue unboxFloat(ObjectValue value) {
		checkNotNull(value);
		return (FloatValue) invokeVirtual("floatValue", "()F", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps char value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public IntValue unboxChar(ObjectValue value) {
		checkNotNull(value);
		return (IntValue) invokeVirtual("charValue", "()C", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps short value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public IntValue unboxShort(ObjectValue value) {
		checkNotNull(value);
		return (IntValue) invokeVirtual("shortValue", "()S", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps byte value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public IntValue unboxByte(ObjectValue value) {
		checkNotNull(value);
		return (IntValue) invokeVirtual("byteValue", "()B", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Unwraps boolean value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 *
	 * @return primitive value.
	 */
	public IntValue unboxBoolean(ObjectValue value) {
		checkNotNull(value);
		return (IntValue) invokeVirtual("booleanValue", "()Z", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Attempts to unbox generic object value.
	 *
	 * @param value
	 * 		Wrapper to unwrap.
	 * @param jc
	 * 		Primitive class.
	 *
	 * @return unwrapped value or itself.
	 */
	public Value unboxGeneric(ObjectValue value, JavaClass jc) {
		VMPrimitives primitive = vm.getPrimitives();
		if (jc == primitive.longPrimitive()) {
			return unboxLong(value);
		}
		if (jc == primitive.doublePrimitive()) {
			return unboxDouble(value);
		}
		if (jc == primitive.intPrimitive()) {
			return unboxInt(value);
		}
		if (jc == primitive.floatPrimitive()) {
			return unboxFloat(value);
		}
		if (jc == primitive.charPrimitive()) {
			return unboxChar(value);
		}
		if (jc == primitive.shortPrimitive()) {
			return unboxShort(value);
		}
		if (jc == primitive.bytePrimitive()) {
			return unboxByte(value);
		}
		if (jc == primitive.booleanPrimitive()) {
			return unboxBoolean(value);
		}
		return value;
	}

	/**
	 * Boxes long value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxLong(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Long(), "valueOf", "(J)Ljava/lang/Long;", new Value[0], new Value[]{value, TopValue.INSTANCE}).getResult();
	}

	/**
	 * Boxes double value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxDouble(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Double(), "valueOf", "(D)Ljava/lang/Double;", new Value[0], new Value[]{value, TopValue.INSTANCE}).getResult();
	}

	/**
	 * Boxes int value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxInt(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Integer(), "valueOf", "(I)Ljava/lang/Integer;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes float value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxFloat(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Float(), "valueOf", "(F)Ljava/lang/Float;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes char value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxChar(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Character(), "valueOf", "(C)Ljava/lang/Character;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes short value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxShort(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Short(), "valueOf", "(S)Ljava/lang/Short;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes byte value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxByte(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Byte(), "valueOf", "(B)Ljava/lang/Byte;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes boolean value.
	 *
	 * @param value
	 * 		Value to box.
	 *
	 * @return boxed value.
	 */
	public ObjectValue boxBoolean(Value value) {
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Boolean(), "valueOf", "(Z)Ljava/lang/Boolean;", new Value[0], new Value[]{value}).getResult();
	}

	/**
	 * Boxes primitive type if needed.
	 *
	 * @param value
	 * 		Value to box.
	 * @param type
	 * 		Value type.
	 *
	 * @return boxed value or original,
	 * if boxing is not needed.
	 */
	public Value boxGeneric(Value value, Type type) {
		if (type == Type.LONG_TYPE) {
			return boxLong(value);
		}
		if (type == Type.DOUBLE_TYPE) {
			return boxDouble(value);
		}
		if (type == Type.INT_TYPE) {
			return boxInt(value);
		}
		if (type == Type.FLOAT_TYPE) {
			return boxFloat(value);
		}
		if (type == Type.CHAR_TYPE) {
			return boxChar(value);
		}
		if (type == Type.SHORT_TYPE) {
			return boxShort(value);
		}
		if (type == Type.BYTE_TYPE) {
			return boxByte(value);
		}
		if (type == Type.BOOLEAN_TYPE) {
			return boxBoolean(value);
		}
		return value;
	}

	/**
	 * Converts array of classes to VM array.
	 *
	 * @param classes
	 * 		Array of classes to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue convertClasses(JavaClass[] classes) {
		VirtualMachine vm = this.vm;
		ArrayValue array = newArray(vm.getSymbols().java_lang_Class(), classes.length);
		for (int i = 0; i < classes.length; i++) {
			array.setValue(i, classes[i].getOop());
		}
		return array;
	}

	/**
	 * Converts array to VM classes to their oops.
	 *
	 * @param classes
	 * 		Array of classes to convert.
	 *
	 * @return array of oops.
	 */
	public InstanceValue[] getClassOops(JavaClass[] classes) {
		InstanceValue[] oops = new InstanceValue[classes.length];
		for (int i = 0; i < classes.length; i++) {
			oops[i] = classes[i].getOop();
		}
		return oops;
	}

	/**
	 * Converts array of ASM types to VM classes.
	 *
	 * @param loader
	 * 		Class loader to use.
	 * @param types
	 * 		ASM class types.
	 * @param initialize
	 * 		Should classes be initialized.
	 *
	 * @return Converted array.
	 */
	public JavaClass[] convertTypes(ObjectValue loader, Type[] types, boolean initialize) {
		JavaClass[] classes = new JavaClass[types.length];
		for (int i = 0; i < types.length; i++) {
			String name = types[i].getInternalName();
			JavaClass klass = findClass(loader, name, initialize);
			if (klass == null) {
				throwException(vm.getSymbols().java_lang_NoClassDefFoundError(), name);
			}
			classes[i] = klass;
		}
		return classes;
	}

	/**
	 * Creates new VM array.
	 *
	 * @param componentType
	 * 		Component type of array.
	 * @param length
	 * 		Array length.
	 *
	 * @return new array.
	 */
	public ArrayValue newArray(JavaClass componentType, int length) {
		MemoryManager memoryManager = vm.getMemoryManager();
		return memoryManager.newArray(componentType.newArrayClass(), length);
	}

	/**
	 * Returns empty VM array.
	 *
	 * @param componentType
	 * 		Component type of array.
	 *
	 * @return empty array.
	 */
	public ArrayValue emptyArray(JavaClass componentType) {
		return newArray(componentType, 0);
	}

	/**
	 * Checks whether a class is a primitive wrapper.
	 *
	 * @param jc
	 * 		Class to check.
	 *
	 * @return {@code true} if class is a primitive wrapper,
	 * {@code false} otherwise.
	 */
	public boolean isPrimitiveWrapper(JavaClass jc) {
		if (!(jc instanceof InstanceJavaClass)) {
			return false;
		}
		VMSymbols symbols = vm.getSymbols();
		return symbols.java_lang_Long() == jc
				|| symbols.java_lang_Double() == jc
				|| symbols.java_lang_Integer() == jc
				|| symbols.java_lang_Float() == jc
				|| symbols.java_lang_Character() == jc
				|| symbols.java_lang_Short() == jc
				|| symbols.java_lang_Byte() == jc
				|| symbols.java_lang_Boolean() == jc;
	}

	/**
	 * Returns file descriptor handle.
	 *
	 * @param fos
	 * 		File output stream to get handle from.
	 *
	 * @return file descriptor handle.
	 */
	public long getFileStreamHandle(InstanceValue fos) {
		Value fd = invokeVirtual("getFD", "()Ljava/io/FileDescriptor;", new Value[0], new Value[]{fos}).getResult();
		return this.<InstanceValue>checkNotNull(fd).getLong("handle");
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param rt
	 * 		Return type.
	 * @param parameters
	 * 		Parameter types.
	 *
	 * @return method type.
	 */
	public InstanceValue methodType(JavaClass rt, ArrayValue parameters) {
		return (InstanceValue) invokeStatic(vm.getSymbols().java_lang_invoke_MethodHandleNatives(), "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;", new Value[0], new Value[]{
				rt.getOop(), parameters
		}).getResult();
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param rt
	 * 		Return type.
	 * @param parameters
	 * 		Parameter types.
	 *
	 * @return method type.
	 */
	public InstanceValue methodType(JavaClass rt, JavaClass[] parameters) {
		ArrayValue array = newArray(vm.getSymbols().java_lang_Class(), parameters.length);
		for (int i = 0; i < parameters.length; i++) {
			array.setValue(i, parameters[i].getOop());
		}
		return methodType(rt, array);
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param loader
	 * 		Class loader to pull classes from.
	 * @param methodType
	 * 		Method type.
	 *
	 * @return method type.
	 */
	public InstanceValue methodType(ObjectValue loader, Type methodType) {
		JavaClass rt = findClass(loader, methodType.getReturnType().getInternalName(), false);
		JavaClass[] args = convertTypes(loader, methodType.getArgumentTypes(), false);
		return methodType(rt, args);
	}

	/**
	 * Searches for field offset.
	 *
	 * @param target
	 * 		Class to search in first.
	 * @param javaClass
	 * 		Class to search in recursively.
	 * @param name
	 * 		Feild name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffset(InstanceJavaClass target, InstanceJavaClass javaClass, String name, String desc) {
		long offset = target.getVirtualFieldOffset(name, desc);
		if (offset == -1L) {
			do {
				offset = javaClass.getVirtualFieldOffset(name, desc);
			} while(offset == -1L && (javaClass = javaClass.getSuperClass()) != null);
		}
		return offset;
	}

	/**
	 * Creates multi array.
	 *
	 * @param type
	 * 		Array type.
	 * @param lengths
	 * 		Array containing length of each dimension.
	 *
	 * @return new array.
	 */
	public ArrayValue newMultiArray(ArrayJavaClass type, int[] lengths) {
		return newMultiArrayInner(type, lengths, 0);
	}

	/**
	 * Creates new object instance.
	 *
	 * @param type
	 * 		Type of object.
	 * @param desc
	 * 		Init method descriptor.
	 * @param params
	 * 		Init method arguments.
	 *
	 * @return new allocated object.
	 */
	public InstanceValue newInstance(InstanceJavaClass type, String desc, Value... params) {
		type.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(type);
		Value[] args = new Value[params.length + 1];
		args[0] = instance;
		System.arraycopy(params, 0, args, 1, params.length);
		invokeExact(type, "<init>", desc, new Value[0], args);
		return instance;
	}

	/**
	 * Marks method as a hidden method.
	 *
	 * @param method
	 * 		Method to make hidden.
	 *
	 * @see Modifier#ACC_HIDDEN_FRAME
	 */
	public void makeHiddenMethod(JavaMethod method) {
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_HIDDEN_FRAME;
	}

	/**
	 * Makes necessary methods of a class hidden.
	 *
	 * @param jc
	 * 		Class to setup.
	 */
	public void setupHiddenFrames(InstanceJavaClass jc) {
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass throwable = symbols.java_lang_Throwable();
		if (throwable.isAssignableFrom(jc)) {
			for (JavaMethod jm : jc.getVirtualMethodLayout().getAll()) {
				String name = jm.getName();
				if ("<init>".equals(name)) {
					makeHiddenMethod(jm);
					continue;
				}
				if ("fillInStackTrace".equals(name)) {
					Type[] args = jm.getArgumentTypes();
					if (args.length == 0 || (args[0].equals(Type.INT_TYPE))) {
						makeHiddenMethod(jm);
					}
				}
			}
		} else {
			ObjectValue loader = jc.getClassLoader();
			if (loader.isNull()) {
				if (jc.getInternalName().startsWith("java/lang/invoke/")) {
					for (JavaMethod jm : jc.getVirtualMethodLayout().getAll()) {
						hideLambdaForm(jm);
					}
					for (JavaMethod jm : jc.getStaticMethodLayout().getAll()) {
						hideLambdaForm(jm);
					}
				}
				if (jc == symbols.java_lang_invoke_MethodHandle()) {
					makeHiddenMethod(jc, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "invokeBasic", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "linkToStatic", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "linkToVirtual", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "linkToInterface", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeHiddenMethod(jc, "linkToSpecial", "([Ljava/lang/Object;)Ljava/lang/Object;");
					makeCallerSensitive(jc, "invokeWithArguments", "(Ljava/util/List;)Ljava/lang/Object;");
				} else if (jc == symbols.java_lang_reflect_Method()) {
					makeCallerSensitive(jc, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
				} else if (symbols.reflect_MethodAccessorImpl().isAssignableFrom(jc)) {
					for (JavaMethod jm : jc.getVirtualMethodLayout().getAll()) {
						MethodNode node = jm.getNode();
						node.access |= Modifier.ACC_CALLER_SENSITIVE;
					}
					for (JavaMethod jm : jc.getStaticMethodLayout().getAll()) {
						MethodNode node = jm.getNode();
						node.access |= Modifier.ACC_CALLER_SENSITIVE;
					}
				}
			}
		}
	}

	/**
	 * Returns method by it's slot.
	 *
	 * @param jc
	 * 		Method owner.
	 * @param slot
	 * 		Method slot.
	 *
	 * @return method by it's slot or {@code null},
	 * if not found.
	 */
	public JavaMethod getMethodBySlot(InstanceJavaClass jc, int slot) {
		return jc.getMethodBySlot(slot);
	}

	/**
	 * Returns field by it's slot.
	 *
	 * @param jc
	 * 		Field owner.
	 * @param slot
	 * 		Method slot.
	 *
	 * @return field by it's slot or {@code null},
	 * if not found.
	 */
	public JavaField getFieldBySlot(InstanceJavaClass jc, int slot) {
		return jc.getFieldBySlot(slot);
	}

	/**
	 * Links method handle.
	 *
	 * @param handle
	 * 		Method handle.
	 *
	 * @return linked method handle.
	 */
	public InstanceValue linkMethodHandleConstant(InstanceJavaClass caller, Handle handle) {
		Value[] args = new Value[]{
				caller.getOop(),
				IntValue.of(handle.getTag()),
				findClass(caller.getClassLoader(), handle.getOwner(), false).getOop(),
				newUtf8(handle.getName()),
				methodType(caller.getClassLoader(), Type.getMethodType(handle.getDesc()))
		};

		InstanceJavaClass natives = vm.getSymbols().java_lang_invoke_MethodHandleNatives();
		return (InstanceValue) invokeStatic(natives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", new Value[0], args).getResult();
	}

	/**
	 * Creates VM boxed vales from constant
	 * for an invokedynamic call.
	 *
	 * @return VM boxed value.
	 *
	 * @throws IllegalStateException
	 * 		If constant value cannot be created.
	 */
	public ObjectValue forInvokeDynamicCall(Object cst) {
		if (cst instanceof Long) {
			return boxLong(LongValue.of((Long) cst));
		}
		if (cst instanceof Double) {
			return boxDouble(new DoubleValue((Double) cst));
		}
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte) {
			return boxInt(IntValue.of(((Number) cst).intValue()));
		}
		if (cst instanceof Character) {
			return boxInt(IntValue.of((Character) cst));
		}
		if (cst instanceof Float) {
			return boxFloat(new FloatValue((Float) cst));
		}
		if (cst instanceof Boolean) {
			return boxBoolean((Boolean) cst ? IntValue.ONE : IntValue.ZERO);
		}
		return (ObjectValue) valueFromLdc(cst);
	}

	/**
	 * Converts Java string to VM array of chars.
	 *
	 * @param str
	 * 		String to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue toVMChars(String str) {
		int length = str.length();
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().charPrimitive(), length);
		while(length-- != 0) {
			wrapper.setChar(length, str.charAt(length));
		}
		return wrapper;
	}

	/**
	 * Attempts to locate a class.
	 * Throws {@link NoClassDefFoundError} if class is not found.
	 *
	 * @param loader
	 * 		Loader to search the class in.
	 * @param name
	 * 		Class name.
	 * @param initialize
	 * 		Whether the class should be initialized.
	 *
	 * @return found class.
	 */
	public JavaClass tryFindClass(ObjectValue loader, String name, boolean initialize) {
		try {
			return findClass(loader, name, initialize);
		} catch(VMException ex) {
			InstanceValue oop = ex.getOop();
			if (oop.isNull() || !vm.getSymbols().java_lang_Error().isAssignableFrom(oop.getJavaClass())) {
				InstanceValue cnfe = newException(vm.getSymbols().java_lang_NoClassDefFoundError(), name, oop);
				throw new VMException(cnfe);
			}
			throw ex;
		}
	}

	/**
	 * @return VM instance.
	 */
	public VirtualMachine getVM() {
		return vm;
	}

	private ArrayValue newMultiArrayInner(ArrayJavaClass type, int[] lengths, int depth) {
		JavaClass newType = type.getComponentType();
		MemoryManager memoryManager = vm.getMemoryManager();
		if (!newType.isArray()) {
			return memoryManager.newArray(type, lengths[depth]);
		}
		ArrayValue array = memoryManager.newArray(type, lengths[depth]);
		if (depth == lengths.length - 1) {
			return array;
		}
		int length = lengths[depth];
		int next = depth + 1;
		while(length-- != 0) {
			array.setValue(length, newMultiArrayInner((ArrayJavaClass) newType, lengths, next));
		}
		return array;
	}

	private static void contextPrepare(ExecutionContext ctx, Value[] stack, Value[] locals) {
		Locals lvt = ctx.getLocals();
		for (int i = 0, j = locals.length; i < j; i++) {
			Value arg = locals[i];
			Objects.requireNonNull(arg, "Null argument");
			lvt.set(i, arg);
			if (arg != TopValue.INSTANCE && arg.isWide()) {
				if (i + 1 == locals.length || locals[i + 1] != TopValue.INSTANCE) {
					throw new IllegalStateException("Locals table is broken");
				}
			}
		}
		Stack $stack = ctx.getStack();
		for (Value value : stack) {
			$stack.pushGeneric(value);
		}
	}

	private static ExecutionContext createContext(InstanceJavaClass jc, JavaMethod jm, Value[] locals) {
		MethodNode mn = jm.getNode();
		VirtualMachine vm = jc.getVM();
		ThreadStorage storage = vm.getThreadStorage();
		int maxStack = mn.maxStack;
		int maxLocals = getMaxLocals(jm, locals);
		return vm.getExecutionEngine().createContext(
				jm,
				storage.newStack(maxStack),
				storage.newLocals(maxLocals)
		);
	}

	private static int getMaxLocals(JavaMethod jm, Value[] locals) {
		return Math.max(AsmUtil.getMaxLocals(jm), locals.length);
	}

	private static void makeHiddenMethod(InstanceJavaClass jc, String name, String desc) {
		JavaMethod mn = jc.getVirtualMethod(name, desc);
		if (mn == null) {
			mn = jc.getStaticMethod(name, desc);
		}
		if (mn != null) {
			MethodNode node = mn.getNode();
			node.access |= Modifier.ACC_HIDDEN_FRAME | Modifier.ACC_CALLER_SENSITIVE;
		}
	}

	private static void hideLambdaForm(JavaMethod jm) {
		MethodNode node = jm.getNode();
		List<AnnotationNode> annotations = node.visibleAnnotations;
		if (annotations != null) {
			for (int i = 0; i < annotations.size(); i++) {
				if ("Ljava/lang/invoke/LambdaForm$Hidden;".equals(annotations.get(i).desc)) {
					node.access |= Modifier.ACC_HIDDEN_FRAME | Modifier.ACC_CALLER_SENSITIVE;
					break;
				}
			}
		}
	}

	private static void makeCallerSensitive(InstanceJavaClass jc, String name, String desc) {
		JavaMethod jm = jc.getVirtualMethod(name, desc);
		if (jm == null) {
			jm = jc.getStaticMethod(name, desc);
		}
		MethodNode node = jm.getNode();
		node.access |= Modifier.ACC_CALLER_SENSITIVE;
	}
}
