package dev.xdark.ssvm.util;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.*;
import dev.xdark.ssvm.mirror.*;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.value.*;
import lombok.val;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
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
		val ctx = createContext(javaClass, method, locals);
		contextPrepare(ctx, stack, locals, 0);
		vm.execute(ctx, true);
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
		val mn = javaClass.getStaticMethodRecursively(name, desc);
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError, javaClass.getInternalName() + '.' + name + desc);
		}
		return invokeStatic(javaClass, mn, stack, locals);
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
		val instance = locals[0];
		checkNotNull(instance);
		InstanceJavaClass javaClass;
		if (instance instanceof ArrayValue) {
			javaClass = vm.getSymbols().java_lang_Object;
		} else {
			javaClass = ((InstanceValue) instance).getJavaClass();
		}
		JavaMethod m = javaClass.getVirtualMethodRecursively(name, desc);
		if (m == null) {
			// Perform invokeInterface call.
			return invokeInterface(javaClass, name, desc, stack, locals);
		}
		return invokeExact(javaClass, m, stack, locals);
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
		val instance = locals[0];
		checkNotNull(instance);
		val prioritized = ((InstanceValue) instance).getJavaClass();

		JavaMethod mn = prioritized.getInterfaceMethodRecursively(name, desc);
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError, javaClass.getInternalName() + '.' + name + desc);
		}
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
			throwException(vm.getSymbols().java_lang_NullPointerException);
		}
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalStateException("Method is static");
		}
		javaClass.initialize();
		val ctx = createContext(javaClass, method, locals);
		contextPrepare(ctx, stack, locals, 0);
		vm.execute(ctx, true);
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
		JavaMethod mn = javaClass.getVirtualMethodRecursively(name, desc);
		if (mn == null && javaClass.isInterface()) {
			mn = javaClass.getInterfaceMethodRecursively(name, desc);
		}
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError, javaClass.getInternalName() + '.' + name + desc);
		}
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
		val vm = this.vm;
		if (cst instanceof Long) return LongValue.of((Long) cst);
		if (cst instanceof Double) return new DoubleValue((Double) cst);
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte)
			return IntValue.of(((Number) cst).intValue());
		if (cst instanceof Character) return IntValue.of((Character) cst);
		if (cst instanceof Float) return new FloatValue((Float) cst);
		if (cst instanceof Boolean) return (Boolean) cst ? IntValue.ONE : IntValue.ZERO;
		if (cst instanceof String) return vm.getStringPool().intern((String) cst);
		if (cst instanceof Type) {
			val type = (Type) cst;
			val ctx = vm.currentThread().getBacktrace().last();
			Value loader = ctx == null ? NullValue.INSTANCE : ctx.getDeclaringClass().getClassLoader();
			val sort = type.getSort();
			switch (sort) {
				case Type.OBJECT:
					return vm.findClass(loader, type.getInternalName(), false).getOop();
				case Type.METHOD:
					return methodType(loader, type);
				default:
					return findClass(loader, type.getInternalName(), false).getOop();
			}
		}

		if (cst instanceof Handle) {
			val ctx = vm.currentThread().getBacktrace().last();
			return linkMethodHandleConstant(ctx.getDeclaringClass(), (Handle) cst);
		}

		throw new UnsupportedOperationException("TODO: " + cst);
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
		val result = new long[length];
		while (length-- != 0)
			result[length] = array.getLong(length);
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
		val result = new double[length];
		while (length-- != 0)
			result[length] = array.getDouble(length);
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
		val result = new int[length];
		while (length-- != 0)
			result[length] = array.getInt(length);
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
		val result = new float[length];
		while (length-- != 0)
			result[length] = array.getFloat(length);
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
		val result = new char[length];
		while (length-- != 0)
			result[length] = array.getChar(length);
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
		val result = new short[length];
		while (length-- != 0)
			result[length] = array.getShort(length);
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
		val result = new byte[length];
		while (length-- != 0)
			result[length] = array.getByte(length);
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
		val result = new boolean[length];
		while (length-- != 0)
			result[length] = array.getBoolean(length);
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
		val result = new Value[length];
		while (length-- != 0)
			result[length] = array.getValue(length);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().longPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().doublePrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().intPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().floatPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().charPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().shortPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().bytePrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getPrimitives().booleanPrimitive, newLength);
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
		val vm = this.vm;
		val wrapper = newArray(vm.getSymbols().java_lang_Object, newLength);
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
		val jc = (InstanceJavaClass) value.getJavaClass();
		val vm = jc.getVM();
		if (jc != vm.getSymbols().java_lang_String) {
			throw new IllegalStateException("Not a string: " + value + " (" + jc + ')');
		}
		val array = invokeExact(jc, "toCharArray", "()[C", new Value[0], new Value[]{value}).getResult();
		return new String(toJavaChars((ArrayValue) array));
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
		if (value.isNull()) return null;
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
		val vm = this.vm;
		val jc = vm.getSymbols().java_lang_String;
		jc.initialize();
		if (pool) {
			val pooled = vm.getStringPool().getIfPresent(str);
			if (pooled != null) return pooled;
		}
		val wrapper = vm.getMemoryManager().newInstance(jc);
		if (str.isEmpty()) {
			if (jc.hasVirtualField("value", "[C")) {
				// JDK 8
				wrapper.setValue("value", "[C", toVMChars(new char[0]));
			} else {
				wrapper.setValue("value", "[B", toVMBytes(new byte[0]));
			}
		} else {
			invokeExact(jc, "<init>", "([C)V", new Value[0], new Value[]{wrapper, toVMChars(str.toCharArray())});
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
	 * Initializes default static values of the class.
	 *
	 * @param javaClass
	 * 		Class to set fields for.
	 */
	public void initializeStaticFields(InstanceJavaClass javaClass) {
		val memoryManager = vm.getMemoryManager();
		val oop = javaClass.getOop();
		val baseOffset = memoryManager.getStaticOffset(javaClass);
		val fields = javaClass.getStaticFieldLayout().getFields();
		val asmFields = javaClass.getNode().fields;
		for (val entry : fields.entrySet()) {
			val key = entry.getKey();
			val name = key.getName();
			val desc = key.getDesc();
			val fn = asmFields.stream()
					.filter(x -> name.equals(x.name) && desc.equals(x.desc))
					.findFirst();
			if (!fn.isPresent()) {
				throw new PanicException("Static layout is broken");
			}
			Object cst = fn.get().value;
			if (cst == null) cst = AsmUtil.getDefaultValue(desc);
			val offset = entry.getValue().getOffset();
			val resultingOffset = baseOffset + offset;
			switch (desc) {
				case "J":
					memoryManager.writeLong(oop, resultingOffset, (Long) cst);
					break;
				case "D":
					memoryManager.writeDouble(oop, resultingOffset, (Double) cst);
					break;
				case "I":
					memoryManager.writeInt(oop, resultingOffset, (Integer) cst);
					break;
				case "F":
					memoryManager.writeFloat(oop, resultingOffset, (Float) cst);
					break;
				case "C":
					memoryManager.writeChar(oop, resultingOffset, (char) ((Integer) cst).intValue());
					break;
				case "S":
					memoryManager.writeShort(oop, resultingOffset, ((Integer) cst).shortValue());
					break;
				case "B":
				case "Z":
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
		val vm = this.vm;
		val memoryManager = vm.getMemoryManager();
		val baseOffset = memoryManager.valueBaseOffset(value);
		for (val entry : value.getJavaClass().getVirtualFieldLayout().getFields().values()) {
			val field = entry.getNode().desc;
			val offset = baseOffset + entry.getOffset();
			switch (field) {
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
	 * Initializes default values of the class.
	 *
	 * @param value
	 * 		Value to set fields for.
	 * @param javaClass
	 * 		Class to get fields from.
	 */
	public void initializeDefaultValues(InstanceValue value, InstanceJavaClass javaClass) {
		val vm = this.vm;
		val memoryManager = vm.getMemoryManager();
		val fields = value.getJavaClass().getVirtualFieldLayout()
				.getFields()
				.values()
				.stream()
				.filter(x -> javaClass == x.getOwner())
				.collect(Collectors.toList());
		val baseOffset = memoryManager.valueBaseOffset(value);
		for (val entry : fields) {
			val field = entry.getNode().desc;
			val offset = baseOffset + entry.getOffset();
			switch (field) {
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
		val javaThread = vmThread.getJavaThread();
		val oop = vmThread.getOop();
		// Copy thread name
		oop.setValue("name", "Ljava/lang/String;", newUtf8(javaThread.getName()));
		// Copy thread priority
		oop.setInt("priority", javaThread.getPriority());
		// Copy daemon status
		oop.setBoolean("daemon", javaThread.isDaemon());
		// Copy thread state (JVMTI_THREAD_STATE_RUNNABLE)
		oop.setInt("threadStatus", 0x0004);
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
		val vm = this.vm;
		javaClass.initialize();
		val instance = vm.getMemoryManager().newInstance(javaClass);
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
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException);
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
			throwException(vm.getSymbols().java_lang_NegativeArraySizeException);
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
			throwException(vm.getSymbols().java_lang_NullPointerException);
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
			throwException(vm.getSymbols().java_lang_NullPointerException);
		}
		if (!(value instanceof ArrayValue)) {
			throwException(vm.getSymbols().java_lang_InternalError, "not an array");
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
			throwException(vm.getSymbols().java_lang_IllegalArgumentException);
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
			throwException(vm.getSymbols().java_lang_IllegalStateException);
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
	 * Definec class.
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
		val vm = this.vm;
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException);
		}
		ClassLoaderData classLoaderData;
		if (classLoader.isNull()) {
			classLoaderData = vm.getBootClassLoaderData();
		} else {
			classLoaderData = ((JavaValue<ClassLoaderData>) ((InstanceValue) classLoader).getValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
		}
		val parsed = vm.getClassDefiner().parseClass(name, b, off, len, source);
		if (parsed == null) {
			throwException(vm.getSymbols().java_lang_NoClassDefFoundError, name);
		}
		val classReaderName = parsed.getClassReader().getClassName();
		if (name == null) {
			name = classReaderName;
		} else if (!classReaderName.equals(name.replace('.', '/'))) {
			throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Expected class name " + classReaderName.replace('/', '.') + " but received: " + name);
		}
		if (name.contains("[")) {
			throwException(vm.getSymbols().java_lang_NoClassDefFoundError, "Bad class name: " + classReaderName);
		}
		synchronized (classLoaderData) {
			if (classLoaderData.getClass(name) != null) {
				throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Duplicate class name: " + name);
			}
			// Create class
			val javaClass = newInstanceClass(classLoader, protectionDomain, parsed.getClassReader(), parsed.getNode());
			classLoaderData.linkClass(javaClass);
			return javaClass;
		}
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
		val vm = this.vm;
		val javaClass = new InstanceJavaClass(vm, loader, reader, node);
		val oop = vm.getMemoryManager().setOopForClass(javaClass);
		javaClass.setOop(oop);
		initializeDefaultValues(oop);
		setClassFields(oop, loader, protectionDomain);
		if (!loader.isNull()) {
			val classes = ((InstanceValue) loader).getValue("classes", "Ljava/util/Vector;");
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
		val oop = (InstanceValue) javaClass.getOop();
		if (oop.getJavaClass().hasVirtualField("componentType", "Ljava/lang/Class;"))
			oop.setValue("componentType", "Ljava/lang/Class;", componentType.getOop());
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
		val msg = readUtf8(oop.getValue("detailMessage", "Ljava/lang/String;"));
		val exception = new Exception(msg);
		val backtrace = oop.getValue("backtrace", "Ljava/lang/Object;");
		if (!backtrace.isNull()) {
			val unmarshalled = ((JavaValue<Backtrace>) backtrace).getValue();
			val stackTrace = StreamSupport.stream(unmarshalled.spliterator(), false)
					.map(frame -> {
						val methodName = frame.getMethodName();
						val owner = frame.getDeclaringClass();
						val className = owner.getName();
						val sourceFile = frame.getSourceFile();
						val lineNumber = frame.getLineNumber();
						return new StackTraceElement(className, methodName, sourceFile, lineNumber);
					})
					.toArray(StackTraceElement[]::new);
			Collections.reverse(Arrays.asList(stackTrace));
			exception.setStackTrace(stackTrace);
		}
		val cause = oop.getValue("cause", "Ljava/lang/Throwable;");
		if (!cause.isNull() && cause != oop) {
			exception.initCause(toJavaException((InstanceValue) cause));
		}
		val suppressedExceptions = oop.getValue("suppressedExceptions", "Ljava/util/List;");
		if (!suppressedExceptions.isNull()) {
			val list = (InstanceJavaClass) vm.findBootstrapClass("java/util/List");
			val size = invokeInterface(list, "size", "()I", new Value[0], new Value[]{suppressedExceptions}).getResult().asInt();
			for (int i = 0; i < size; i++) {
				val ex = invokeInterface(list, "get", "(I)Ljava/lang/Object;", new Value[]{IntValue.of(i)}, new Value[]{suppressedExceptions}).getResult();
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
		val methodName = frame.getMethodName();
		val owner = frame.getDeclaringClass();
		val className = owner.getName();
		val sourceFile = frame.getSourceFile();
		val lineNumber = frame.getLineNumber();
		val vm = this.vm;
		val jc = vm.getSymbols().java_lang_StackTraceElement;
		jc.initialize();
		val element = vm.getMemoryManager().newInstance(jc);
		invokeExact(jc, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", new Value[0], new Value[]{
				element,
				newUtf8(className),
				newUtf8(methodName),
				newUtf8(sourceFile),
				IntValue.of(lineNumber)
		});
		if (injectDeclaringClass && jc.hasVirtualField("declaringClassObject", "Ljava/lang/Class;")) {
			element.setValue("declaringClassObject", "Ljava/lang/Class;", owner.getOop());
		}
		return element;
	}

	public JavaClass findClass(Value loader, String name, boolean initialize) {
		int dimensions = 0;
		while (name.charAt(dimensions) == '[') dimensions++;
		if (dimensions != 0) name = name.substring(dimensions);
		JavaClass klass;
		if (name.length() == 1) {
			val primitives = vm.getPrimitives();
			switch (name.charAt(0)) {
				case 'J':
					klass = primitives.longPrimitive;
					break;
				case 'D':
					klass = primitives.doublePrimitive;
					break;
				case 'I':
					klass = primitives.intPrimitive;
					break;
				case 'F':
					klass = primitives.floatPrimitive;
					break;
				case 'C':
					klass = primitives.charPrimitive;
					break;
				case 'S':
					klass = primitives.shortPrimitive;
					break;
				case 'B':
					klass = primitives.bytePrimitive;
					break;
				case 'Z':
					klass = primitives.booleanPrimitive;
					break;
				case 'V':
					klass = primitives.voidPrimitive;
					break;
				default:
					throw new IllegalStateException(name);
			}
		} else {
			if (dimensions != 0) {
				name = name.substring(1, name.length() - 1);
			}
			klass = vm.findClass(loader, name, initialize);
		}
		while (dimensions-- != 0) klass = klass.newArrayClass();
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
		val primitive = vm.getPrimitives();
		if (jc == primitive.longPrimitive) return unboxLong(value);
		if (jc == primitive.doublePrimitive) return unboxDouble(value);
		if (jc == primitive.intPrimitive) return unboxInt(value);
		if (jc == primitive.floatPrimitive) return unboxFloat(value);
		if (jc == primitive.charPrimitive) return unboxChar(value);
		if (jc == primitive.shortPrimitive) return unboxShort(value);
		if (jc == primitive.bytePrimitive) return unboxByte(value);
		if (jc == primitive.booleanPrimitive) return unboxBoolean(value);
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Long, "valueOf", "(J)Ljava/lang/Long;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Double, "valueOf", "(D)Ljava/lang/Double;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Integer, "valueOf", "(I)Ljava/lang/Integer;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Float, "valueOf", "(F)Ljava/lang/Float;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Character, "valueOf", "(C)Ljava/lang/Character;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Short, "valueOf", "(S)Ljava/lang/Short;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Byte, "valueOf", "(B)Ljava/lang/Byte;", new Value[0], new Value[]{value}).getResult();
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
		return (ObjectValue) invokeStatic(vm.getSymbols().java_lang_Boolean, "valueOf", "(Z)Ljava/lang/Boolean;", new Value[0], new Value[]{value}).getResult();
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
		if (type == Type.LONG_TYPE) return boxLong(value);
		if (type == Type.DOUBLE_TYPE) return boxDouble(value);
		if (type == Type.INT_TYPE) return boxInt(value);
		if (type == Type.FLOAT_TYPE) return boxFloat(value);
		if (type == Type.CHAR_TYPE) return boxChar(value);
		if (type == Type.SHORT_TYPE) return boxShort(value);
		if (type == Type.BYTE_TYPE) return boxByte(value);
		if (type == Type.BOOLEAN_TYPE) return boxBoolean(value);
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
		val vm = this.vm;
		val array = newArray(vm.getSymbols().java_lang_Class, classes.length);
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
		val oops = new InstanceValue[classes.length];
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
	public JavaClass[] convertTypes(Value loader, Type[] types, boolean initialize) {
		val classes = new JavaClass[types.length];
		for (int i = 0; i < types.length; i++) {
			val name = types[i].getInternalName();
			val klass = findClass(loader, name, initialize);
			if (klass == null) {
				throwException(vm.getSymbols().java_lang_NoClassDefFoundError, name);
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
		val memoryManager = vm.getMemoryManager();
		return memoryManager.newArray(componentType.newArrayClass(), length, memoryManager.arrayIndexScale(componentType));
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
		if (!(jc instanceof InstanceJavaClass)) return false;
		val symbols = vm.getSymbols();
		return symbols.java_lang_Long == jc
				|| symbols.java_lang_Double == jc
				|| symbols.java_lang_Integer == jc
				|| symbols.java_lang_Float == jc
				|| symbols.java_lang_Character == jc
				|| symbols.java_lang_Short == jc
				|| symbols.java_lang_Byte == jc
				|| symbols.java_lang_Boolean == jc;
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
		val fd = invokeVirtual("getFD", "()Ljava/io/FileDescriptor;", new Value[0], new Value[]{fos}).getResult();
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
		return (InstanceValue) invokeStatic(vm.getSymbols().java_lang_invoke_MethodHandleNatives, "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;", new Value[0], new Value[]{
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
		val array = newArray(vm.getSymbols().java_lang_Class, parameters.length);
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
	public InstanceValue methodType(Value loader, Type methodType) {
		val rt = findClass(loader, methodType.getReturnType().getInternalName(), false);
		val args = convertTypes(loader, methodType.getArgumentTypes(), false);
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
		long offset = target.getFieldOffset(name, desc);
		if (offset == -1L) {
			do {
				offset = javaClass.getFieldOffset(name, desc);
			} while (offset == -1L && (javaClass = javaClass.getSuperClass()) != null);
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
		val instance = vm.getMemoryManager().newInstance(type);
		val args = new Value[params.length + 1];
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
		val node = method.getNode();
		node.access |= Modifier.ACC_HIDDEN_FRAME;
	}

	/**
	 * Makes necessary methods of a class hidden.
	 *
	 * @param jc
	 * 		Class to setup.
	 */
	public void setupHiddenFrames(InstanceJavaClass jc) {
		val throwable = vm.getSymbols().java_lang_Throwable;
		if (throwable.isAssignableFrom(jc)) {
			for (val jm : jc.getVirtualMethodLayout().getMethods().values()) {
				val name = jm.getName();
				if ("<init>".equals(name)) {
					makeHiddenMethod(jm);
					continue;
				}
				if ("fillInStackTrace".equals(name)) {
					val args = jm.getArgumentTypes();
					if (args.length == 0 || (args[0].equals(Type.INT_TYPE))) {
						makeHiddenMethod(jm);
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
		for (val m : jc.getVirtualMethodLayout().getMethods().values()) {
			if (slot == m.getSlot()) return m;
		}
		for (val m : jc.getStaticMethodLayout().getMethods().values()) {
			if (slot == m.getSlot()) return m;
		}
		return null;
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
		for (val f : jc.getVirtualFieldLayout().getFields().values()) {
			if (slot == f.getSlot()) return f;
		}
		for (val f : jc.getStaticFieldLayout().getFields().values()) {
			if (slot == f.getSlot()) return f;
		}
		return null;
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
		val args = new Value[]{
				caller.getOop(),
				IntValue.of(handle.getTag()),
				findClass(caller.getClassLoader(), handle.getOwner(), false).getOop(),
				newUtf8(handle.getName()),
				methodType(caller.getClassLoader(), Type.getMethodType(handle.getDesc()))
		};

		val natives = vm.getSymbols().java_lang_invoke_MethodHandleNatives;
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
		if (cst instanceof Long) return boxLong(LongValue.of((Long) cst));
		if (cst instanceof Double) return boxDouble(new DoubleValue((Double) cst));
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte)
			return boxInt(IntValue.of(((Number) cst).intValue()));
		if (cst instanceof Character)
			return boxInt(IntValue.of((Character) cst));
		if (cst instanceof Float) return boxFloat(new FloatValue((Float) cst));
		if (cst instanceof Boolean) return boxBoolean((Boolean) cst ? IntValue.ONE : IntValue.ZERO);
		return (ObjectValue) valueFromLdc(cst);
	}

	private ArrayValue newMultiArrayInner(ArrayJavaClass type, int[] lengths, int depth) {
		val newType = type.getComponentType();
		val memoryManager = vm.getMemoryManager();
		if (!newType.isArray()) {
			return memoryManager.newArray(type, lengths[depth], memoryManager.arrayIndexScale(newType));
		}
		val array = memoryManager.newArray(type, lengths[depth], memoryManager.arrayIndexScale(ArrayValue.class));
		if (depth == lengths.length - 1)
			return array;
		int length = lengths[depth];
		val next = depth + 1;
		while (length-- != 0) {
			array.setValue(length, newMultiArrayInner((ArrayJavaClass) newType, lengths, next));
		}
		return array;
	}

	private static void contextPrepare(ExecutionContext ctx, Value[] stack, Value[] locals, int localIndex) {
		val lvt = ctx.getLocals();
		for (val local : locals) {
			if (local == null) {
				localIndex++;
				continue;
			}
			lvt.set(localIndex++, local);
			if (local.isWide()) {
				localIndex++;
			}
		}
		val $stack = ctx.getStack();
		for (val value : stack) {
			$stack.pushGeneric(value);
		}
	}

	private static ExecutionContext createContext(InstanceJavaClass jc, JavaMethod jm, Value[] locals) {
		val mn = jm.getNode();
		return new ExecutionContext(
				jc.getVM(),
				jc,
				jm,
				new Stack(mn.maxStack),
				new Locals(getMaxLocals(jm, locals))
		);
	}

	private static int getMaxLocals(JavaMethod jm, Value[] locals) {
		int max = AsmUtil.getMaxLocals(jm);
		int x = 0;
		for (val local : locals) {
			if (local == null) x++;
			else
				x += (local.isWide() ? 2 : 1);
		}
		max = Math.max(max, x);
		return max;
	}
}
