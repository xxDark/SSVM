package dev.xdark.ssvm.util;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.*;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

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
	public ExecutionContext invokeStatic(InstanceJavaClass javaClass, MethodNode method, Value[] stack, Value[] locals) {
		javaClass.initialize();
		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			throw new IllegalStateException("Method is not static");
		}
		var ctx = createContext(javaClass, method);
		contextPrepare(ctx, stack, locals, 0);
		javaClass.getVM().execute(ctx, true);
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
		var mn = javaClass.getMethod(name, desc);
		if (mn == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError, javaClass.getName() + '.' + name + desc);
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
		InstanceJavaClass javaClass;
		var instance = locals[0];
		if (instance.isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException);
		}
		if (instance instanceof ArrayValue) {
			javaClass = vm.getSymbols().java_lang_Object;
		} else {
			javaClass = ((InstanceValue) instance).getJavaClass();
		}
		javaClass.initialize();
		var keep = javaClass;
		MethodNode method;
		do {
			method = javaClass.getMethod(name, desc);
		} while (method == null && (javaClass = javaClass.getSuperClass()) != null);
		if (method == null) {
			throwException(vm.getSymbols().java_lang_NoSuchMethodError, keep.getName() + '.' + name + desc);
		}
		if ((method.access & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalStateException("Method is static");
		}
		var ctx = createContext(javaClass, method);
		contextPrepare(ctx, stack, locals, 0);
		javaClass.getVM().execute(ctx, true);
		return ctx;
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
		// TODO actually implement this properly
		return invokeVirtual(name, desc, stack, locals);
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
	public ExecutionContext invokeExact(InstanceJavaClass javaClass, MethodNode method, Value[] stack, Value[] locals) {
		if (locals[0].isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException);
		}
		if ((method.access & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalStateException("Method is static");
		}
		var ctx = createContext(javaClass, method);
		contextPrepare(ctx, stack, locals, 0);
		javaClass.getVM().execute(ctx, true);
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
		return invokeExact(javaClass, javaClass.getMethod(name, desc), stack, locals);
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
		var vm = this.vm;
		if (cst instanceof Long) return new LongValue((Long) cst);
		if (cst instanceof Double) return new DoubleValue((Double) cst);
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte)
			return new IntValue(((Number) cst).intValue());
		if (cst instanceof Character) return new IntValue((Character) cst);
		if (cst instanceof Float) return new FloatValue((Float) cst);
		if (cst instanceof Boolean) return new IntValue((Boolean) cst ? 1 : 0);
		if (cst instanceof String) return newUtf8((String) cst);
		if (cst instanceof Type) {
			var type = (Type) cst;
			var ctx = vm.currentThread().getBacktrace().last();
			var loader = ctx == null ? NullValue.INSTANCE : ctx.getOwner().getClassLoader();
			var sort = type.getSort();
			switch (sort) {
				case Type.OBJECT: {
					var name = type.getInternalName();
					var klass = vm.findClass(loader, name, false); // fast path
					if (klass == null) {
						throwException(vm.getSymbols().java_lang_ClassNotFoundException, name);
						return null;
					}
					return klass.getOop();
				}
				case Type.ARRAY:
					var name = type.getInternalName();
					var klass = findClass(loader, type.getInternalName(), false);
					if (klass == null) {
						throwException(vm.getSymbols().java_lang_ClassNotFoundException, name);
						return null;
					}
					return klass.getOop();
				default:
					throw new IllegalStateException("Not implemented yet: " + sort);
			}
		}
		throw new UnsupportedOperationException("TODO: " + cst);
	}

	private JavaClass findType(Value loader, String name) {
		var vm = this.vm;
		switch (name) {
			case "J":
				return vm.getPrimitives().longPrimitive;
			case "D":
				return vm.getPrimitives().doublePrimitive;
			case "I":
				return vm.getPrimitives().intPrimitive;
			case "F":
				return vm.getPrimitives().floatPrimitive;
			case "C":
				return vm.getPrimitives().charPrimitive;
			case "S":
				return vm.getPrimitives().shortPrimitive;
			case "B":
				return vm.getPrimitives().bytePrimitive;
			case "Z":
				return vm.getPrimitives().booleanPrimitive;
			default:
				return vm.findClass(loader, name, false);
		}
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
		var length = array.getLength();
		var result = new long[length];
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
		var length = array.getLength();
		var result = new double[length];
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
		var length = array.getLength();
		var result = new int[length];
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
		var length = array.getLength();
		var result = new float[length];
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
		var length = array.getLength();
		var result = new char[length];
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
		var length = array.getLength();
		var result = new short[length];
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
		var length = array.getLength();
		var result = new byte[length];
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
		var length = array.getLength();
		var result = new boolean[length];
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
		var length = array.getLength();
		var result = new Value[length];
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().longPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(long.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().doublePrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(double.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().intPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(int.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().floatPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(float.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().charPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(char.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().shortPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(short.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().bytePrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(byte.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getPrimitives().booleanPrimitive.newArrayClass(), newLength, memoryManager.arrayIndexScale(boolean.class));
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var wrapper = memoryManager.newArray(vm.getSymbols().java_lang_Object.newArrayClass(), newLength, memoryManager.arrayIndexScale(Value.class));
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
		var jc = (InstanceJavaClass) value.getJavaClass();
		var vm = jc.getVM();
		if (jc != vm.getSymbols().java_lang_String) {
			throw new IllegalStateException("Not a string: " + value);
		}
		var array = invokeExact(jc, "toCharArray", "()[C", new Value[0], new Value[]{value}).getResult();
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
	 *
	 * @return VM string.
	 */
	public ObjectValue newUtf8(String str) {
		if (str == null) {
			return NullValue.INSTANCE;
		}
		var vm = this.vm;
		var jc = vm.getSymbols().java_lang_String;
		jc.initialize();
		var wrapper = vm.getMemoryManager().newInstance(jc);
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
	 * Returns default descriptor value.
	 *
	 * @param desc
	 * 		Type descriptor.
	 */
	public Value getDefaultValue(String desc) {
		switch (desc) {
			case "J":
				return new LongValue(0L);
			case "D":
				return new DoubleValue(0.0D);
			case "I":
			case "S":
			case "B":
			case "Z":
				return new IntValue(0);
			case "F":
				return new FloatValue(0.0F);
			case "C":
				return new IntValue('\0');
			default:
				return NullValue.INSTANCE;
		}
	}

	/**
	 * Initializes default static values of the class.
	 *
	 * @param javaClass
	 * 		Class to set fields for.
	 */
	public void initializeStaticFields(InstanceJavaClass javaClass) {
		var memoryManager = vm.getMemoryManager();
		var oop = javaClass.getOop();
		var baseOffset = memoryManager.getStaticOffset(javaClass);
		var fields = javaClass.getStaticLayout().getOffsetMap();
		var asmFields = javaClass.getNode().fields;
		for (var entry : fields.entrySet()) {
			var key = entry.getKey();
			var name = key.getName();
			var desc = key.getDesc();
			var fn = asmFields.stream()
					.filter(x -> name.equals(x.name) && desc.equals(x.desc))
					.findFirst();
			if (fn.isEmpty()) {
				throw new PanicException("Static layout is broken");
			}
			var cst = fn.get().value;
			if (cst == null) cst = AsmUtil.getDefaultValue(desc);
			var offset = entry.getValue().intValue();
			var resultingOffset = baseOffset + offset;
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var baseOffset = memoryManager.valueBaseOffset(value);
		for (var entry : value.getJavaClass().getVirtualLayout().getOffsetMap().entrySet()) {
			var field = entry.getKey().getDesc();
			var offset = baseOffset + entry.getValue();
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
		var vm = this.vm;
		var memoryManager = vm.getMemoryManager();
		var fields = value.getJavaClass().getVirtualLayout()
				.getOffsetMap()
				.entrySet()
				.stream()
				.filter(x -> javaClass == x.getKey().getOwner())
				.collect(Collectors.toList());
		var baseOffset = memoryManager.valueBaseOffset(value);
		for (var entry : fields) {
			var field = entry.getKey().getDesc();
			var offset = baseOffset + entry.getValue();
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
		var javaThread = vmThread.getJavaThread();
		var oop = vmThread.getOop();
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
		var vm = this.vm;
		javaClass.initialize();
		var instance = vm.getMemoryManager().newInstance(javaClass);
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
	 */
	public void checkNotNull(Value value) {
		if (value.isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException);
		}
	}

	/**
	 * Performs array check.
	 *
	 * @param value
	 * 		Value to check.
	 */
	public void checkArray(Value value) {
		if (!(value instanceof ArrayValue)) {
			throwException(vm.getSymbols().java_lang_IllegalArgumentException);
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
		oop.setValue("classLoader", "Ljava/lang/ClassLoader;", classLoader);
		oop.setValue("protectionDomain", "Ljava/security/ProtectionDomain;", protectionDomain);
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
	public JavaClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source) {
		var vm = this.vm;
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException);
			return null;
		}
		ClassLoaderData classLoaderData;
		if (classLoader.isNull()) {
			classLoaderData = vm.getBootClassLoaderData();
		} else {
			classLoaderData = ((JavaValue<ClassLoaderData>) ((InstanceValue) classLoader).getValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
		}
		var parsed = vm.getClassDefiner().parseClass(name, b, off, len, source);
		if (parsed == null) {
			throwException(vm.getSymbols().java_lang_NoClassDefFoundError);
			return null;
		}
		var actualName = parsed.getClassReader().getClassName();
		if (name == null) {
			name = actualName;
		} else if (!actualName.equals(name.replace('.', '/'))) {
			throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Expected class name: " + actualName.replace('/', '.') + " but received: " + name);
			return null;
		}
		synchronized (classLoaderData) {
			if (classLoaderData.getClass(name) != null) {
				throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Class already exists: " + name);
				return null;
			}
			// Create class
			var javaClass = new InstanceJavaClass(vm, classLoader, parsed.getClassReader(), parsed.getNode());
			classLoaderData.linkClass(javaClass);
			var oop = (InstanceValue) vm.getMemoryManager().setOopForClass(javaClass);
			javaClass.setOop(oop);
			vm.getHelper().initializeDefaultValues(oop);
			setClassFields(oop, classLoader, protectionDomain);
			if (!classLoader.isNull()) {
				var classes = ((InstanceValue) classLoader).getValue("classes", "Ljava/util/Vector;");
				invokeVirtual("add", "(Ljava/lang/Object;)Z", new Value[0], new Value[]{classes, javaClass.getOop()});
			}
			return javaClass;
		}
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
		var oop = (InstanceValue) javaClass.getOop();
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
		var msg = readUtf8(oop.getValue("detailMessage", "Ljava/lang/String;"));
		var exception = new Exception(msg);
		var backtrace = oop.getValue("backtrace", "Ljava/lang/Object;");
		if (!backtrace.isNull()) {
			var unmarshalled = ((JavaValue<Backtrace>) backtrace).getValue();
			var stackTrace = StreamSupport.stream(unmarshalled.spliterator(), false)
					.map(ctx -> {
						var methodName = ctx.getMethod().name;
						var owner = ctx.getOwner();
						var className = owner.getName();
						var sourceFile = owner.getNode().sourceFile;
						var lineNumber = ctx.getLineNumber();
						return new StackTraceElement(className, methodName, sourceFile, lineNumber);
					})
					.toArray(StackTraceElement[]::new);
			var len = stackTrace.length;
			for (int i = 0, j = len / 2; i < j; i++) {
				var temp = stackTrace[i];
				var idx = len - i - 1;
				stackTrace[i] = stackTrace[idx];
				stackTrace[idx] = temp;
			}
			exception.setStackTrace(stackTrace);
		}
		var cause = oop.getValue("cause", "Ljava/lang/Throwable;");
		if (!cause.isNull() && cause != oop) {
			exception.initCause(toJavaException((InstanceValue) cause));
		}
		var suppressedExceptions = oop.getValue("suppressedExceptions", "Ljava/util/List;");
		if (!suppressedExceptions.isNull()) {
			var list = (InstanceJavaClass) vm.findBootstrapClass("java/util/List");
			var size = invokeInterface(list, "size", "()I", new Value[0], new Value[]{suppressedExceptions}).getResult().asInt();
			for (int i = 0; i < size; i++) {
				var ex = invokeInterface(list, "get", "(I)Ljava/lang/Object;", new Value[]{new IntValue(i)}, new Value[]{suppressedExceptions}).getResult();
				exception.addSuppressed(toJavaException((InstanceValue) ex));
			}
		}
		return exception;
	}

	/**
	 * Constructs new VM StackTraceElement from backtrace frame.
	 *
	 * @param ctx
	 * 		Java frame.
	 * @param injectDeclaringClass
	 * 		See {@link StackTraceElement#declaringClassObject} description.
	 *
	 * @return VM StackTraceElement.
	 */
	public InstanceValue newStackTraceElement(ExecutionContext ctx, boolean injectDeclaringClass) {
		var methodName = ctx.getMethod().name;
		var owner = ctx.getOwner();
		var className = owner.getName();
		var sourceFile = owner.getNode().sourceFile;
		var lineNumber = ctx.getLineNumber();
		var vm = this.vm;
		var jc = vm.getSymbols().java_lang_StackTraceElement;
		var element = vm.getMemoryManager().newInstance(jc);
		invokeExact(jc, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", new Value[0], new Value[]{
				element,
				newUtf8(className),
				newUtf8(methodName),
				newUtf8(sourceFile),
				new IntValue(lineNumber)
		});
		if (injectDeclaringClass && jc.hasVirtualField("declaringClassObject", "Ljava/lang/Class;")) {
			element.setValue("declaringClassObject", "Ljava/lang/Class;", owner.getOop());
		}
		return element;
	}

	public JavaClass findClass(Value loader, String name, boolean initialize) {
		var dimensions = 0;
		while (name.charAt(dimensions) == '[') dimensions++;
		if (dimensions != 0) name = name.substring(dimensions);
		JavaClass klass = null;
		if (name.length() == 1) {
			var primitives = vm.getPrimitives();
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
			}
		} else {
			if (dimensions != 0) {
				name = name.substring(1, name.length() - 1);
			}
			klass = vm.findClass(loader, name, initialize);
		}
		if (klass != null) {
			while (dimensions-- != 0) klass = klass.newArrayClass();
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
	 * Converts array of classes to VM array.
	 *
	 * @param classes
	 * 		Array of classes to convert.
	 *
	 * @return VM array.
	 */
	public ArrayValue convertClasses(JavaClass[] classes) {
		var vm = this.vm;
		var classArray = vm.getSymbols().java_lang_Class.newArrayClass();
		var memoryManager = vm.getMemoryManager();
		var array = memoryManager.newArray(classArray, classes.length, memoryManager.arrayIndexScale(Value.class));
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
		var oops = new InstanceValue[classes.length];
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
		var classes = new JavaClass[types.length];
		for (int i = 0; i < types.length; i++) {
			var name = types[i].getInternalName();
			var klass = findClass(loader, name, initialize);
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
		var memoryManager = vm.getMemoryManager();
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
		var symbols = vm.getSymbols();
		return symbols.java_lang_Long == jc
				|| symbols.java_lang_Double == jc
				|| symbols.java_lang_Integer == jc
				|| symbols.java_lang_Float == jc
				|| symbols.java_lang_Character == jc
				|| symbols.java_lang_Short == jc
				|| symbols.java_lang_Byte == jc
				|| symbols.java_lang_Boolean == jc;
	}

	private static void contextPrepare(ExecutionContext ctx, Value[] stack, Value[] locals, int localIndex) {
		var lvt = ctx.getLocals();
		for (var local : locals) {
			lvt.set(localIndex++, local);
			if (local.isWide()) {
				localIndex++;
			}
		}
		var $stack = ctx.getStack();
		for (var value : stack) {
			$stack.pushGeneric(value);
		}
	}

	private static ExecutionContext createContext(InstanceJavaClass jc, MethodNode mn) {
		return new ExecutionContext(
				jc.getVM(),
				jc,
				mn,
				new Stack(mn.maxStack),
				new Locals(AsmUtil.getMaxLocals(mn))
		);
	}
}
