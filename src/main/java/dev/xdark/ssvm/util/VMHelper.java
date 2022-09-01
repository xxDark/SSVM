package dev.xdark.ssvm.util;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.ExecutionOptions;
import dev.xdark.ssvm.execution.ExecutionRequest;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.mirror.MemberKey;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.ThreadState;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import dev.xdark.ssvm.value.sink.AbstractValueSink;
import dev.xdark.ssvm.value.sink.BlackholeValueSink;
import dev.xdark.ssvm.value.sink.DoubleValueSink;
import dev.xdark.ssvm.value.sink.FloatValueSink;
import dev.xdark.ssvm.value.sink.IntValueSink;
import dev.xdark.ssvm.value.sink.LongValueSink;
import dev.xdark.ssvm.value.sink.ReferenceValueSink;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.StreamSupport;

/**
 * Provides additional functionality for
 * the VM and simplifies some things.
 *
 * @author xDark
 */
public final class VMHelper {

	private static final int STRING_COPY_THRESHOLD = 256;
	private final VirtualMachine vm;
	private final Queue<SimpleExecutionRequest<?>> requests;
	private final Queue<InvocationSink> sinks;

	/**
	 * @param vm VM instance.
	 */
	public VMHelper(VirtualMachine vm) {
		this.vm = vm;
		requests = new BoundedQueue<>(new ConcurrentLinkedQueue<>(), 16);
		sinks = new BoundedQueue<>(new ConcurrentLinkedQueue<>(), 128);
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @param sink   Result sink.
	 * @return invocation result.
	 */
	public <R extends ValueSink> ExecutionContext<R> invoke(JavaMethod method, Locals locals, R sink) {
		ExecutionEngine engine = vm.getExecutionEngine();
		Queue<SimpleExecutionRequest<?>> requests = this.requests;
		SimpleExecutionRequest<R> request = (SimpleExecutionRequest<R>) requests.poll();
		if (request == null) {
			request = new SimpleExecutionRequest<>();
		}
		request.init(method, vm.getThreadStorage().newStack(method), locals, engine.defaultOptions(), sink);
		ExecutionContext<R> ctx = engine.createContext(request);
		requests.offer(request);
		engine.execute(ctx);
		return ctx;
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public ObjectValue invokeReference(JavaMethod method, Locals locals) {
		InvocationSink sink = makeSink();
		invoke(method, locals, sink);
		ObjectValue result = sink.r_value;
		sinks.offer(sink);
		return result;
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public long invokeLong(JavaMethod method, Locals locals) {
		InvocationSink sink = makeSink();
		invoke(method, locals, sink);
		long ref = sink.l_value;
		sinks.offer(sink);
		return ref;
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public double invokeDouble(JavaMethod method, Locals locals) {
		return Double.longBitsToDouble(invokeLong(method, locals));
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public int invokeInt(JavaMethod method, Locals locals) {
		InvocationSink sink = makeSink();
		invoke(method, locals, sink);
		int ref = sink.i_value;
		sinks.offer(sink);
		return ref;
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public float invokeFloat(JavaMethod method, Locals locals) {
		return Float.intBitsToFloat(invokeInt(method, locals));
	}

	/**
	 * Makes direct call.
	 *
	 * @param method Method to invoke.
	 * @param locals Local variable table.
	 * @return invocation result.
	 */
	public ExecutionContext<?> invoke(JavaMethod method, Locals locals) {
		return invoke(method, locals, BlackholeValueSink.INSTANCE);
	}

	/**
	 * Creates VM vales from constant reference.
	 *
	 * @return VM value.
	 * @throws IllegalStateException If constant value cannot be created.
	 */
	public Value referenceFromLdc(Object cst) {
		VirtualMachine vm = this.vm;
		if (cst instanceof String) {
			return vm.getStringPool().intern((String) cst);
		}
		if (cst instanceof Type) {
			Type type = (Type) cst;
			StackFrame ctx = vm.currentThread().getBacktrace().last();
			InstanceJavaClass caller = ctx == null ? null : ctx.getDeclaringClass();
			ObjectValue loader = caller == null ? vm.getMemoryManager().nullValue() : caller.getClassLoader();
			int sort = type.getSort();
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
			StackFrame ctx = vm.currentThread().getBacktrace().last();
			return linkMethodHandleConstant(ctx.getDeclaringClass(), (Handle) cst);
		}

		throw new PanicException("Unsupported constant " + cst + " " + cst.getClass());
	}

	/**
	 * Converts an array to {@code long[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public long[] toJavaLongs(ArrayValue array) {
		int length = array.getLength();
		long[] result = new long[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code double[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public double[] toJavaDoubles(ArrayValue array) {
		int length = array.getLength();
		double[] result = new double[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code int[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public int[] toJavaInts(ArrayValue array) {
		int length = array.getLength();
		int[] result = new int[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code float[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public float[] toJavaFloats(ArrayValue array) {
		int length = array.getLength();
		float[] result = new float[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code char[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public char[] toJavaChars(ArrayValue array) {
		int length = array.getLength();
		char[] result = new char[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code short[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public short[] toJavaShorts(ArrayValue array) {
		int length = array.getLength();
		short[] result = new short[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code byte[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public byte[] toJavaBytes(ArrayValue array) {
		int length = array.getLength();
		byte[] result = new byte[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code boolean[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public boolean[] toJavaBooleans(ArrayValue array) {
		int length = array.getLength();
		boolean[] result = new boolean[length];
		array.getMemory().getData().read(vm.getMemoryManager().arrayBaseOffset(array), result, 0, length);
		return result;
	}

	/**
	 * Converts an array to {@code Value[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	public ObjectValue[] toJavaValues(ArrayValue array) {
		int length = array.getLength();
		ObjectValue[] result = new ObjectValue[length];
		while (length-- != 0) {
			result[length] = array.getReference(length);
		}
		return result;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMLongs(long[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().longPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMLongs(long[] array) {
		return toVMLongs(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMDoubles(double[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().doublePrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMDoubles(double[] array) {
		return toVMDoubles(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMInts(int[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().intPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMInts(int[] array) {
		return toVMInts(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMFloats(float[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().floatPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMFloats(float[] array) {
		return toVMFloats(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMChars(char[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().charPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMChars(char[] array) {
		return toVMChars(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMShorts(short[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().shortPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMShorts(short[] array) {
		return toVMShorts(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMBytes(byte[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().bytePrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMBytes(byte[] array) {
		return toVMBytes(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMBooleans(boolean[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getPrimitives().booleanPrimitive(), newLength);
		wrapper.getMemory().getData().write(vm.getMemoryManager().arrayBaseOffset(wrapper), array, startIndex, newLength);
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMBooleans(boolean[] array) {
		return toVMBooleans(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	public ArrayValue toVMValues(ObjectValue[] array, int startIndex, int endIndex) {
		int newLength = endIndex - startIndex;
		VirtualMachine vm = this.vm;
		ArrayValue wrapper = newArray(vm.getSymbols().java_lang_Object(), newLength);
		for (int i = 0; startIndex < endIndex; startIndex++) {
			wrapper.setReference(i++, array[startIndex]);
		}
		return wrapper;
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMValues(ObjectValue[] array) {
		return toVMValues(array, 0, array.length);
	}

	/**
	 * Converts VM string to Java string.
	 *
	 * @param value VM string.
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
			array = (ArrayValue) vm.getMemoryManager().readReference(value, off);
		} else {
			JavaMethod toCharArray = vm.getPublicLinkResolver().resolveVirtualMethod(jc, jc, "toCharArray", "()[C");
			Locals locals = vm.getThreadStorage().newLocals(toCharArray);
			locals.setReference(0, value);
			array = (ArrayValue) invokeReference(toCharArray, locals);
		}
		return UnsafeUtil.newString(toJavaChars(array));
	}

	/**
	 * Converts VM string to Java string.
	 *
	 * @param value VM string.
	 * @return Java string.
	 */
	public String readUtf8(Value value) {
		if (((ObjectValue) value).isNull()) { // TODO fix me
			return null;
		}
		return readUtf8((InstanceValue) value);
	}

	/**
	 * Allocates VM string.
	 *
	 * @param str  Java string.
	 * @param pool True if string should be pooled.
	 * @return VM string.
	 */
	public ObjectValue newUtf8(String str, boolean pool) {
		VirtualMachine vm = this.vm;
		if (str == null) {
			return vm.getMemoryManager().nullValue();
		}
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
				vm.getPublicOperations().putReference(wrapper, "value", "[B", emptyArray(primitives.bytePrimitive()));
			}
			wrapper.initialize();
		} else {
			ArrayValue chars = toVMChars(str);
			if (jdk8) {
				memoryManager.writeValue(wrapper, off, chars);
				wrapper.initialize();
			} else {
				JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(jc, "<init>", "([C)V");
				Locals locals = vm.getThreadStorage().newLocals(init);
				locals.setReference(0, wrapper);
				locals.setReference(1, chars);
				invoke(init, locals);
			}
		}
		return wrapper;
	}

	/**
	 * Allocates VM string.
	 *
	 * @param str Java string.
	 * @return VM string.
	 */
	public ObjectValue newUtf8(String str) {
		return newUtf8(str, false);
	}

	/**
	 * Allocates VM string.
	 *
	 * @param chars String chars.
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
			JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(jc, "<init>", "([C)V");
			Locals locals = vm.getThreadStorage().newLocals(init);
			locals.setReference(0, wrapper);
			locals.setReference(1, chars);
			invoke(init, locals);
		}
		return wrapper;
	}

	/**
	 * Initializes default static values of the class.
	 *
	 * @param javaClass Class to set fields for.
	 */
	public void initializeStaticFields(InstanceJavaClass javaClass) {
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue oop = javaClass.getOop();
		MemoryData data = oop.getData();
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
			switch (desc.charAt(0)) {
				case 'J':
					data.writeLong(resultingOffset, (Long) cst);
					break;
				case 'D':
					data.writeLong(resultingOffset, Double.doubleToRawLongBits((Double) cst));
					break;
				case 'I':
					data.writeInt(resultingOffset, (Integer) cst);
					break;
				case 'F':
					data.writeInt(resultingOffset, Float.floatToRawIntBits((Float) cst));
					break;
				case 'C':
					data.writeChar(resultingOffset, (char) ((Integer) cst).intValue());
					break;
				case 'S':
					data.writeShort(resultingOffset, ((Integer) cst).shortValue());
					break;
				case 'B':
				case 'Z':
					data.writeByte(resultingOffset, ((Integer) cst).byteValue());
					break;
				default:
					memoryManager.writeValue(oop, resultingOffset, cst == null ? memoryManager.nullValue() : (ObjectValue) referenceFromLdc(cst));
			}
		}
	}

	/**
	 * Modifies VM oop according to native thread.
	 *
	 * @param vmThread Thread to modify.
	 */
	public void screenVmThread(VMThread vmThread) {
		Thread javaThread = vmThread.getJavaThread();
		InstanceValue oop = vmThread.getOop();
		VirtualMachine vm = this.vm;
		VMOperations ops = vm.getPublicOperations();
		InstanceJavaClass jc = vm.getSymbols().java_lang_Thread();
		// Copy thread name
		ops.putReference(oop, jc, "name", "Ljava/lang/String;", newUtf8(javaThread.getName()));
		// Copy thread priority
		ops.putInt(oop, jc, "priority", javaThread.getPriority());
		// Copy daemon status
		ops.putBoolean(oop, jc, "daemon", javaThread.isDaemon());
		// Copy thread state (JVMTI_THREAD_STATE_RUNNABLE | JVMTI_THREAD_STATE_ALIVE)
		ops.putInt(oop, jc, "threadStatus", ThreadState.JVMTI_THREAD_STATE_RUNNABLE | ThreadState.JVMTI_THREAD_STATE_ALIVE);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Exception message.
	 * @param cause     Exception cause.
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, String message, ObjectValue cause) {
		VirtualMachine vm = this.vm;
		javaClass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(javaClass);
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(javaClass, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.setReference(0, instance);
		invoke(init, locals);
		VMOperations ops = vm.getPublicOperations();
		if (message != null) {
			ops.putReference(instance, "detailMessage", "Ljava/lang/String;", newUtf8(message));
		}
		if (cause != null) {
			ops.putReference(instance, "cause", "Ljava/lang/Throwable;", cause);
		}
		return instance;
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Exception message.
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, String message) {
		return newException(javaClass, message, null);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param cause     Exception cause.
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass, ObjectValue cause) {
		return newException(javaClass, null, cause);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @return new exception instance.
	 */
	public InstanceValue newException(InstanceJavaClass javaClass) {
		return newException(javaClass, null, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Message.
	 * @param cause     Exception cause.
	 */
	public void throwException(InstanceJavaClass javaClass, String message, ObjectValue cause) {
		throw new VMException(newException(javaClass, message, cause));
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Exception Message.
	 */
	public void throwException(InstanceJavaClass javaClass, String message) {
		throwException(javaClass, message, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param cause     Exception cause.
	 */
	public void throwException(InstanceJavaClass javaClass, ObjectValue cause) {
		throwException(javaClass, null, cause);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 */
	public void throwException(InstanceJavaClass javaClass) {
		throwException(javaClass, null, null);
	}

	/**
	 * Performs array bounds check.
	 *
	 * @param array Array to check in.
	 * @param index Index to check.
	 */
	public void rangeCheck(ArrayValue array, int index) {
		if (index < 0 || index >= array.getLength()) {
			throwException(vm.getSymbols().java_lang_ArrayIndexOutOfBoundsException());
		}
	}

	/**
	 * Performs array length check.
	 *
	 * @param length Length to check.
	 */
	public void checkArrayLength(int length) {
		if (length < 0) {
			throwException(vm.getSymbols().java_lang_NegativeArraySizeException());
		}
	}

	/**
	 * Performs null check.
	 *
	 * @param value Value to check.
	 * @param <V>   New value type after null check.
	 * @return value.
	 */
	public <V extends ObjectValue> V checkNotNull(Value value) {
		if (((ObjectValue) value).isNull()) { // TODO fix me
			throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		return (V) value;
	}

	/**
	 * Performs null check on an array.
	 *
	 * @param value Value to check.
	 * @return array value.
	 */
	public <V extends ArrayValue> V checkNotNullArray(ObjectValue value) {
		if (value.isNull()) {
			throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		return (V) value;
	}

	/**
	 * Performs bounds check.
	 *
	 * @param value Value to check.
	 * @param from  Minimmm value.
	 * @param to    Maximum value.
	 */
	public void rangeCheck(int value, int from, int to) {
		if (value < from || value >= to) {
			throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
	}

	/**
	 * Performs equality check.
	 *
	 * @param a Left value.
	 * @param b Right value.
	 */
	public void checkEquals(int a, int b) {
		if (a != b) {
			throwException(vm.getSymbols().java_lang_IllegalStateException());
		}
	}

	/**
	 * Sets class fields, just like normal JVM.
	 *
	 * @param oop              Class to set fields for.
	 * @param classLoader      Class loader.
	 * @param protectionDomain Protection domain of the class.
	 */
	public void setClassFields(InstanceValue oop, ObjectValue classLoader, ObjectValue protectionDomain) {
		if (!classLoader.isNull()) {
			VMOperations ops = vm.getTrustedOperations();
			ops.putReference(oop, "classLoader", "Ljava/lang/ClassLoader;", classLoader);
			ops.putReference(oop, NativeJava.PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;", protectionDomain);
		}
	}

	/**
	 * Defines class.
	 *
	 * @param classLoader      Class loader to define class in.
	 * @param name             Class name.
	 * @param b                Class bytes.
	 * @param off              Class bytes offset.
	 * @param len              Class bytes length.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source, e.g. it's location
	 * @param linkToLoader     Whether the class should be linked.
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
		synchronized (classLoaderData) {
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
	 * @param classLoader      Class loader to define class in.
	 * @param name             Class name.
	 * @param b                Class bytes.
	 * @param off              Class bytes offset.
	 * @param len              Class bytes length.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source, e.g. it's location
	 * @return defined class.
	 */
	public InstanceJavaClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source) {
		return defineClass(classLoader, name, b, off, len, protectionDomain, source, true);
	}

	/**
	 * Creates new {@link InstanceJavaClass}.
	 *
	 * @param loader           Class loader.
	 * @param protectionDomain Protection domain.
	 * @param reader           Class source.
	 * @param node             Class node
	 */
	public InstanceJavaClass newInstanceClass(ObjectValue loader, ObjectValue protectionDomain, ClassReader reader, ClassNode node) {
		VirtualMachine vm = this.vm;
		ClassLoaders classLoaders = vm.getClassLoaders();
		InstanceJavaClass javaClass = vm.getMirrorFactory().newInstanceClass(loader, reader, node);
		classLoaders.setClassOop(javaClass);
		InstanceValue oop = javaClass.getOop();
		setClassFields(oop, loader, protectionDomain);
		if (!loader.isNull()) {
			ObjectValue classes = ((InstanceValue) loader).getValue("classes", "Ljava/util/Vector;");
			JavaMethod add = vm.getPublicLinkResolver().resolveVirtualMethod(classes, "add", "(Ljava/lang/Object;)Z");
			Locals locals = vm.getThreadStorage().newLocals(add);
			locals.setReference(0, classes);
			locals.setReference(1, javaClass.getOop());
			invoke(add, locals);
		}
		return javaClass;
	}

	/**
	 * Sets array class component type.
	 *
	 * @param javaClass     Class to set component for.
	 * @param componentType Type of the component.
	 */
	public void setComponentType(ArrayJavaClass javaClass, JavaClass componentType) {
		InstanceValue oop = javaClass.getOop();
		long offset = oop.getFieldOffset("componentType", "Ljava/lang/Class;");
		if (offset != -1L) {
			InstanceValue typeOop = componentType.getOop();
			vm.getMemoryManager().writeValue(oop, offset, typeOop);
		}
	}

	/**
	 * Converts VM exception to Java exception.
	 *
	 * @param oop VM exception to convert.
	 * @return Java exception.
	 */
	@Deprecated
	public Exception toJavaException(InstanceValue oop) {
		VMOperations ops = vm.getPublicOperations();
		String msg = readUtf8(ops.getReference(oop, "detailMessage", "Ljava/lang/String;"));
		Exception exception = new Exception(msg);
		ObjectValue backtrace = ops.getReference(oop, "backtrace", "Ljava/lang/Object;");
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
		ObjectValue cause = ops.getReference(oop, "cause", "Ljava/lang/Throwable;");
		if (!cause.isNull() && cause != oop) {
			exception.initCause(toJavaException((InstanceValue) cause));
		}
		ObjectValue suppressedExceptions = ops.getReference(oop, "suppressedExceptions", "Ljava/util/List;");
		if (!suppressedExceptions.isNull()) {
			InstanceJavaClass cl = (InstanceJavaClass) vm.findBootstrapClass("java/util/ArrayList");
			if (cl == suppressedExceptions.getJavaClass()) {
				InstanceValue value = (InstanceValue) suppressedExceptions;
				int size = ops.getInt(value, "size");
				ArrayValue array = (ArrayValue) ops.getReference(value, "elementData", "[Ljava/lang/Object;");
				for (int i = 0; i < size; i++) {
					InstanceValue ref = (InstanceValue) array.getReference(i);
					exception.addSuppressed(ref == oop ? exception : toJavaException(ref));
				}
			}
		}
		return exception;
	}

	/**
	 * Constructs new VM StackTraceElement from backtrace frame.
	 *
	 * @param frame                Java frame.
	 * @param injectDeclaringClass See {@link StackTraceElement#declaringClassObject} description.
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
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(jc, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.setReference(0, element);
		locals.setReference(1, newUtf8(className));
		locals.setReference(2, newUtf8(methodName));
		locals.setReference(3, newUtf8(sourceFile));
		locals.setInt(4, lineNumber);
		invoke(init, locals);
		long offset;
		if (injectDeclaringClass && (offset = element.getFieldOffset("declaringClassObject", "Ljava/lang/Class;")) != -1L) {
			InstanceValue oop = owner.getOop();
			memoryManager.writeValue(element, offset, oop);
		}
		return element;
	}

	public JavaClass findClass(ObjectValue loader, String name, boolean initialize) {
		int dimensions = 0;
		while (name.charAt(dimensions) == '[') {
			dimensions++;
		}
		String requested = name;
		if (dimensions != 0) {
			name = name.substring(dimensions);
		}
		JavaClass klass;
		if (name.charAt(0) != 'L' && dimensions != 0) {
			VMPrimitives primitives = vm.getPrimitives();
			switch (name.charAt(0)) {
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
					klass = null;
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
		if (klass == null) {
			throwException(vm.getSymbols().java_lang_ClassNotFoundException(), requested);
		}
		while (dimensions-- != 0) {
			klass = klass.newArrayClass();
		}
		return klass;
	}

	public JavaClass findClass(ObjectValue loader, Type type, boolean initialize) {
		JavaClass jc;
		VMPrimitives primitives = vm.getPrimitives();
		switch (type.getSort()) {
			case Type.LONG:
				jc = primitives.longPrimitive();
				break;
			case Type.DOUBLE:
				jc = primitives.doublePrimitive();
				break;
			case Type.INT:
				jc = primitives.intPrimitive();
				break;
			case Type.FLOAT:
				jc = primitives.floatPrimitive();
				break;
			case Type.CHAR:
				jc = primitives.charPrimitive();
				break;
			case Type.SHORT:
				jc = primitives.shortPrimitive();
				break;
			case Type.BYTE:
				jc = primitives.bytePrimitive();
				break;
			case Type.BOOLEAN:
				jc = primitives.booleanPrimitive();
				break;
			default:
				jc = findClass(loader, type.getInternalName(), initialize);
		}
		return jc;
	}

	/**
	 * Unwraps long value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public long unboxLong(ObjectValue value) {
		checkNotNull(value);
		return invokeUnbox(value, "longValue", "()J", new LongValueSink()).getValue();
	}

	/**
	 * Unwraps double value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public double unboxDouble(ObjectValue value) {
		checkNotNull(value);
		return invokeUnbox(value, "doubleValue", "()D", new DoubleValueSink()).getValue();
	}

	/**
	 * Unwraps int value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public int unboxInt(ObjectValue value) {
		checkNotNull(value);
		return invokeUnbox(value, "intValue", "()I", new IntValueSink()).getValue();
	}

	/**
	 * Unwraps float value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public float unboxFloat(ObjectValue value) {
		checkNotNull(value);
		return invokeUnbox(value, "floatValue", "()F", new FloatValueSink()).getValue();
	}

	/**
	 * Unwraps char value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public char unboxChar(ObjectValue value) {
		checkNotNull(value);
		return (char) invokeUnbox(value, "charValue", "()C", new IntValueSink()).getValue();
	}

	/**
	 * Unwraps short value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public short unboxShort(ObjectValue value) {
		checkNotNull(value);
		return (short) invokeUnbox(value, "shortValue", "()S", new IntValueSink()).getValue();
	}

	/**
	 * Unwraps byte value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public byte unboxByte(ObjectValue value) {
		checkNotNull(value);
		return (byte) invokeUnbox(value, "byteValue", "()B", new IntValueSink()).getValue();
	}

	/**
	 * Unwraps boolean value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	public boolean unboxBoolean(ObjectValue value) {
		checkNotNull(value);
		return invokeUnbox(value, "booleanValue", "()Z", new IntValueSink()).getValue() != 0;
	}

	/**
	 * Boxes long value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxLong(long value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Long(),
			"valueOf",
			"(J)Ljava/lang/Long;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setLong(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes double value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxDouble(double value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Double(),
			"valueOf",
			"(D)Ljava/lang/Double;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setDouble(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes int value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxInt(int value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Integer(),
			"valueOf",
			"(I)Ljava/lang/Integer;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes float value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxFloat(float value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Float(),
			"valueOf",
			"(F)Ljava/lang/Float;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setFloat(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes char value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxChar(char value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Character(),
			"valueOf",
			"(C)Ljava/lang/Character;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes short value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxShort(short value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Short(),
			"valueOf",
			"(S)Ljava/lang/Short;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes byte value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxByte(byte value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Byte(),
			"valueOf",
			"(B)Ljava/lang/Byte;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setInt(0, value);
		return invokeReference(method, locals);
	}

	/**
	 * Boxes boolean value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	public ObjectValue boxBoolean(boolean value) {
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(
			vm.getSymbols().java_lang_Boolean(),
			"valueOf",
			"(Z)Ljava/lang/Boolean;"
		);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setInt(0, value ? 1 : 0);
		return invokeReference(method, locals);
	}

	/**
	 * Converts array of classes to VM array.
	 *
	 * @param classes Array of classes to convert.
	 * @return VM array.
	 */
	public ArrayValue convertClasses(JavaClass[] classes) {
		VirtualMachine vm = this.vm;
		ArrayValue array = newArray(vm.getSymbols().java_lang_Class(), classes.length);
		for (int i = 0; i < classes.length; i++) {
			array.setReference(i, classes[i].getOop());
		}
		return array;
	}

	/**
	 * Converts array to VM classes to their oops.
	 *
	 * @param classes Array of classes to convert.
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
	 * @param loader     Class loader to use.
	 * @param types      ASM class types.
	 * @param initialize Should classes be initialized.
	 * @return Converted array.
	 */
	public JavaClass[] convertTypes(ObjectValue loader, Type[] types, boolean initialize) {
		JavaClass[] classes = new JavaClass[types.length];
		for (int i = 0; i < types.length; i++) {
			classes[i] = findClass(loader, types[i], initialize);
		}
		return classes;
	}

	/**
	 * Creates new VM array.
	 *
	 * @param componentType Component type of array.
	 * @param length        Array length.
	 * @return new array.
	 */
	public ArrayValue newArray(JavaClass componentType, int length) {
		MemoryManager memoryManager = vm.getMemoryManager();
		return memoryManager.newArray(componentType.newArrayClass(), length);
	}

	/**
	 * Returns empty VM array.
	 *
	 * @param componentType Component type of array.
	 * @return empty array.
	 */
	public ArrayValue emptyArray(JavaClass componentType) {
		return newArray(componentType, 0);
	}

	/**
	 * Returns file descriptor handle.
	 *
	 * @param fs File stream to get handle from.
	 * @return file descriptor handle.
	 */
	public long getFileStreamHandle(InstanceValue fs) {
		VirtualMachine vm = this.vm;
		JavaMethod getFD = vm.getTrustedLinkResolver().resolveVirtualMethod(fs, "getFD", "()Ljava/io/FileDescriptor;");
		Locals locals = vm.getThreadStorage().newLocals(getFD);
		locals.setReference(0, fs);
		ObjectValue fd = invokeReference(getFD, locals);
		return vm.getTrustedOperations().getLong(fd, vm.getSymbols().java_io_FileDescriptor(), "handle");
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param rt         Return type.
	 * @param parameters Parameter types.
	 * @return method type.
	 */
	public InstanceValue methodType(JavaClass rt, ArrayValue parameters) {
		VirtualMachine vm = this.vm;
		JavaMethod method = vm.getPublicLinkResolver().resolveStaticMethod(vm.getSymbols().java_lang_invoke_MethodHandleNatives(), "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;");
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setReference(0, rt.getOop());
		locals.setReference(1, parameters);
		return (InstanceValue) invokeReference(method, locals);
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param rt         Return type.
	 * @param parameters Parameter types.
	 * @return method type.
	 */
	public InstanceValue methodType(JavaClass rt, JavaClass[] parameters) {
		ArrayValue array = newArray(vm.getSymbols().java_lang_Class(), parameters.length);
		for (int i = 0; i < parameters.length; i++) {
			array.setReference(i, parameters[i].getOop());
		}
		return methodType(rt, array);
	}

	/**
	 * Invokes {@link java.lang.invoke.MethodType#methodType(Class, Class[])}
	 *
	 * @param loader     Class loader to pull classes from.
	 * @param methodType Method type.
	 * @return method type.
	 */
	public InstanceValue methodType(ObjectValue loader, Type methodType) {
		JavaClass rt = findClass(loader, methodType.getReturnType().getInternalName(), false);
		JavaClass[] args = convertTypes(loader, methodType.getArgumentTypes(), false);
		return methodType(rt, args);
	}

	/**
	 * Creates multi array.
	 *
	 * @param type    Array type.
	 * @param lengths Array containing length of each dimension.
	 * @return new array.
	 */
	public ArrayValue newMultiArray(ArrayJavaClass type, int[] lengths) {
		return newMultiArrayInner(type, lengths, 0);
	}

	/**
	 * Marks method as a hidden method.
	 *
	 * @param method Method to make hidden.
	 * @see Modifier#ACC_HIDDEN_FRAME
	 */
	public void makeHiddenMethod(JavaMethod method) {
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_HIDDEN_FRAME;
	}

	/**
	 * Makes necessary methods of a class hidden.
	 *
	 * @param jc Class to setup.
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
					JavaClass[] args = jm.getArgumentTypes();
					if (args.length == 0 || (args[0] == vm.getPrimitives().intPrimitive())) {
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
	 * @param jc   Method owner.
	 * @param slot Method slot.
	 * @return method by it's slot or {@code null},
	 * if not found.
	 */
	public JavaMethod getMethodBySlot(InstanceJavaClass jc, int slot) {
		return jc.getMethodBySlot(slot);
	}

	/**
	 * Returns field by it's slot.
	 *
	 * @param jc   Field owner.
	 * @param slot Method slot.
	 * @return field by it's slot or {@code null},
	 * if not found.
	 */
	public JavaField getFieldBySlot(InstanceJavaClass jc, int slot) {
		return jc.getFieldBySlot(slot);
	}

	/**
	 * Links method handle.
	 *
	 * @param handle Method handle.
	 * @return linked method handle.
	 */
	public InstanceValue linkMethodHandleConstant(InstanceJavaClass caller, Handle handle) {
		VirtualMachine vm = this.vm;
		InstanceJavaClass natives = vm.getSymbols().java_lang_invoke_MethodHandleNatives();
		JavaMethod link = vm.getPublicLinkResolver().resolveStaticMethod(natives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;");
		Locals locals = vm.getThreadStorage().newLocals(link);
		locals.setReference(0, caller.getOop());
		locals.setInt(1, handle.getTag());
		locals.setReference(2, findClass(caller.getClassLoader(), handle.getOwner(), false).getOop());
		locals.setReference(3, newUtf8(handle.getName()));
		locals.setReference(4, methodType(caller.getClassLoader(), Type.getMethodType(handle.getDesc())));
		return (InstanceValue) invokeReference(link, locals);
	}

	/**
	 * Converts Java string to VM array of chars.
	 *
	 * @param str String to convert.
	 * @return VM array.
	 */
	public ArrayValue toVMChars(String str) {
		VirtualMachine vm = this.vm;
		int length = str.length();
		ArrayValue wrapper = newArray(vm.getPrimitives().charPrimitive(), length);
		if (UnsafeUtil.stringValueFieldAccessible() || length <= STRING_COPY_THRESHOLD) {
			MemoryData memory = wrapper.getMemory().getData();
			char[] chars = UnsafeUtil.getChars(str);
			memory.write(vm.getMemoryManager().arrayBaseOffset(wrapper), chars, 0, chars.length);
		} else {
			while (length-- != 0) {
				wrapper.setChar(length, str.charAt(length));
			}
		}
		return wrapper;
	}

	/**
	 * Attempts to locate a class.
	 * Throws {@link NoClassDefFoundError} if class is not found.
	 *
	 * @param loader     Loader to search the class in.
	 * @param name       Class name.
	 * @param initialize Whether the class should be initialized.
	 * @return found class.
	 */
	public JavaClass tryFindClass(ObjectValue loader, String name, boolean initialize) {
		try {
			return findClass(loader, name, initialize);
		} catch (VMException ex) {
			InstanceValue oop = ex.getOop();
			VMSymbols symbols = vm.getSymbols();
			if (!symbols.java_lang_Error().isAssignableFrom(oop.getJavaClass())) {
				InstanceValue cnfe = newException(symbols.java_lang_NoClassDefFoundError(), name, oop);
				throw new VMException(cnfe);
			}
			throw ex;
		}
	}

	/**
	 * Returns the size of the descriptor.
	 *
	 * @param descriptor Descriptor to get size for.
	 * @return Descriptor size.
	 * @throws IllegalStateException If primitive descriptor is invalid.
	 */
	public int getDescriptorSize(String descriptor) {
		MemoryManager memoryManager = vm.getMemoryManager();
		if (descriptor.isEmpty()) {
			return memoryManager.objectSize();
		}
		char c;
		switch (c = descriptor.charAt(0)) {
			case 'J':
				return memoryManager.longSize();
			case 'D':
				return memoryManager.doubleSize();
			case 'I':
				return memoryManager.intSize();
			case 'F':
				return memoryManager.floatSize();
			case 'C':
				return memoryManager.charSize();
			case 'S':
				return memoryManager.shortSize();
			case 'B':
				return memoryManager.byteSize();
			case 'Z':
				return memoryManager.booleanSize();
			case 'L':
				return memoryManager.objectSize();
			default:
				if (c == '[') {
					return memoryManager.objectSize();
				}
				throw new IllegalArgumentException(descriptor);
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
		while (length-- != 0) {
			array.setReference(length, newMultiArrayInner((ArrayJavaClass) newType, lengths, next));
		}
		return array;
	}

	private <R extends ValueSink> R invokeUnbox(ObjectValue value, String name, String desc, R sink) {
		VirtualMachine vm = this.vm;
		JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(value, name, desc);
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setReference(0, value);
		invoke(method, locals, sink);
		return sink;
	}

	private InvocationSink makeSink() {
		Queue<InvocationSink> sinks = this.sinks;
		InvocationSink sink = sinks.poll();
		if (sink == null) {
			sink = new InvocationSink();
		} else {
			sink.reset();
		}
		return sink;
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

	private static final class SimpleExecutionRequest<R extends ValueSink> implements ExecutionRequest<R> {
		private JavaMethod method;
		private Stack stack;
		private Locals locals;
		private ExecutionOptions options;
		private R resultSink;

		SimpleExecutionRequest() {
		}

		private void init(JavaMethod method, Stack stack, Locals locals, ExecutionOptions options, R resultSink) {
			this.method = method;
			this.stack = stack;
			this.locals = locals;
			this.options = options;
			this.resultSink = resultSink;
		}

		@Override
		public JavaMethod getMethod() {
			return method;
		}

		@Override
		public Stack getStack() {
			return stack;
		}

		@Override
		public Locals getLocals() {
			return locals;
		}

		@Override
		public ExecutionOptions getOptions() {
			return options;
		}

		@Override
		public R getResultSink() {
			return resultSink;
		}
	}

	private static final class InvocationSink extends AbstractValueSink {
		long l_value;
		int i_value;
		ObjectValue r_value;

		@Override
		public void acceptReference(ObjectValue value) {
			check();
			r_value = value;
		}

		@Override
		public void acceptLong(long value) {
			check();
			l_value = value;
		}

		@Override
		public void acceptDouble(double value) {
			acceptLong(Double.doubleToRawLongBits(value));
		}

		@Override
		public void acceptInt(int value) {
			check();
			i_value = value;
		}

		@Override
		public void acceptFloat(float value) {
			acceptInt(Float.floatToRawIntBits(value));
		}
	}
}
