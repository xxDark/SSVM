package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

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
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
		}
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
		return invokeStatic(javaClass, javaClass.getMethod(name, desc), stack, locals);
	}

	/**
	 * Invokes virtual method.
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
	public ExecutionContext invokeVirtual(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
		}
		javaClass.initialize();
		MethodNode method;
		do {
			method = javaClass.getMethod(name, desc);
		} while (method == null && (javaClass = javaClass.getSuperClass()) != null);
		if (method == null) {
			throw new IllegalStateException("No such method: " + name + desc);
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
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
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
		if (cst instanceof Float) return new DoubleValue((Float) cst);
		if (cst instanceof Boolean) return new IntValue((Boolean) cst ? 1 : 0);
		if (cst instanceof String) return newUtf8((String) cst);
		if (cst instanceof Type) {
			var type = (Type) cst;
			var ctx = vm.currentThread().getBacktrace().last();
			var loader = ctx == null ? NullValue.INSTANCE : ctx.getOwner().getClassLoader();
			var sort = type.getSort();
			switch (sort) {
				case Type.OBJECT:
					return vm.findClass(loader, type.getInternalName(), false).getOop();
				case Type.ARRAY:
					var dimensions = 0;
					var name = type.getInternalName();
					while (name.charAt(dimensions) == '[') dimensions++;
					var base = vm.findClass(loader, name.substring(dimensions), false);
					while (dimensions-- != 0) {
						base = base.newArrayClass();
					}
					return base.getOop();
				default:
					throw new IllegalStateException("Not implemented yet: " + sort);
			}
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().longPrimitive.newArrayClass(), newLength, 8L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().doublePrimitive.newArrayClass(), newLength, 8L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().intPrimitive.newArrayClass(), newLength, 4L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().floatPrimitive.newArrayClass(), newLength, 4L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().charPrimitive.newArrayClass(), newLength, 2L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().shortPrimitive.newArrayClass(), newLength, 2L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().bytePrimitive.newArrayClass(), newLength, 1L);
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
		var wrapper = vm.getMemoryManager().newArray(vm.getPrimitives().booleanPrimitive.newArrayClass(), newLength, 1L);
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
	public ArrayValue tOVMValues(Value[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		var vm = this.vm;
		var wrapper = vm.getMemoryManager().newArray(vm.getSymbols().java_lang_Object.newArrayClass(), newLength, 8L);
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
	public ArrayValue tOVMValues(Value[] array) {
		return tOVMValues(array, 0, array.length);
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
		if (this.vm != vm) {
			throw new IllegalStateException("Wrong helper!");
		}
		if (jc != vm.getSymbols().java_lang_String) {
			throw new IllegalStateException("Not a string: " + value);
		}
		var array = invokeExact(jc, "toCharArray", "()[C", new Value[0], new Value[]{value}).getResult();
		return new String(toJavaChars((ArrayValue) array));
	}

	/**
	 * Allocates VM string.
	 *
	 * @param str
	 * 		Java string.
	 *
	 * @return VM string.
	 */
	public InstanceValue newUtf8(String str) {
		var vm = this.vm;
		var jc = vm.getSymbols().java_lang_String;
		jc.initialize();
		var wrapper = vm.getMemoryManager().newInstance(jc);
		invokeExact(jc, "<init>", "([C)V", new Value[0], new Value[]{wrapper, toVMChars(str.toCharArray())});
		return wrapper;
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
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
		}
		var memoryManager = vm.getMemoryManager();
		var fields = javaClass.getVirtualFields();
		for (var entry : fields.entrySet()) {
			var field = entry.getKey().getDesc();
			var offset = entry.getValue();
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
