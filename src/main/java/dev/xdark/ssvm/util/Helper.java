package dev.xdark.ssvm.util;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;
import dev.xdark.ssvm.mirror.type.SimpleArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import dev.xdark.ssvm.value.sink.AbstractValueSink;
import dev.xdark.ssvm.value.sink.BlackholeValueSink;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.StreamSupport;

/**
 * Provides additional functionality for
 * the VM and simplifies some things.
 *
 * @author xDark
 */
public final class Helper {

	private static final int STRING_COPY_THRESHOLD = 256;
	private final ExecutionEngine executionEngine;
	private final ThreadManager threadManager;
	private final Queue<SimpleExecutionRequest<?>> requests;
	private final Queue<InvocationSink> sinks;

	/**
	 */
	public Helper() {
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
	public <R extends ValueSink> R invoke(JavaMethod method, Locals locals, R sink) {
		ExecutionEngine engine = executionEngine;
		Queue<SimpleExecutionRequest<?>> requests = this.requests;
		SimpleExecutionRequest<R> request = (SimpleExecutionRequest<R>) requests.poll();
		if (request == null) {
			request = new SimpleExecutionRequest<>();
		}
		request.init(method, vm.getThreadStorage().newStack(method), locals, engine.defaultOptions(), sink);
		ExecutionContext<R> ctx = engine.createContext(request);
		requests.offer(request);
		engine.execute(ctx);
		return sink;
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
			StackFrame ctx = vm.currentOSThread().getBacktrace().last();
			InstanceClass caller = ctx == null ? null : ctx.getDeclaringClass();
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
			StackFrame ctx = vm.currentOSThread().getBacktrace().last();
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
	 * Sets class fields, just like normal JVM.
	 *
	 * @param oop              Class to set fields for.
	 * @param classLoader      Class loader.
	 * @param protectionDomain Protection domain of the class.
	 */
	public void setClassFields(InstanceValue oop, ObjectValue classLoader, ObjectValue protectionDomain) {
		if (!classLoader.isNull()) {
			Operations ops = vm.getOperations();
			ops.putReference(oop, "classLoader", "Ljava/lang/ClassLoader;", classLoader);
			ops.putReference(oop, NativeJava.PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;", protectionDomain);
		}
	}

	/**
	 * Constructs new VM StackTraceElement from backtrace frame.
	 *
	 * @param frame                Java frame.
	 * @param injectDeclaringClass See {@link StackTraceElement#declaringClassObject} description.
	 * @return VM StackTraceElement.
	 */
	public InstanceValue newStackTraceElement(StackFrame<?> frame, boolean injectDeclaringClass) {
		JavaMethod method = frame.getMethod();
		String methodName = method.getName();
		InstanceClass owner = method.getOwner();
		String className = owner.getName();
		String sourceFile = owner.getNode().sourceFile;
		int lineNumber = frame.getLineNumber();
		VirtualMachine vm = this.vm;
		InstanceClass jc = vm.getSymbols().java_lang_StackTraceElement();
		jc.initialize();
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceValue element = memoryManager.newInstance(jc);
		JavaMethod init = vm.getLinkResolver().resolveSpecialMethod(jc, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.setReference(0, element);
		locals.setReference(1, newUtf8(className));
		locals.setReference(2, newUtf8(methodName));
		locals.setReference(3, newUtf8(sourceFile));
		locals.setInt(4, lineNumber);
		invoke(init, locals);
		JavaField field;
		if (injectDeclaringClass && (field = jc.getField("declaringClassObject", "Ljava/lang/Class;")) != null) {
			InstanceValue oop = owner.getOop();
			memoryManager.writeValue(element, field.getOffset(), oop);
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
			Primitives primitives = vm.getPrimitives();
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
				StackFrame<?> frame = vm.currentOSThread().getBacktrace().peek();
				if (frame != null) {
					InstanceClass caller = frame.getMethod().getOwner();
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
		Primitives primitives = vm.getPrimitives();
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
		return vm.getOperations().getLong(fd, vm.getSymbols().java_io_FileDescriptor(), "handle");
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
		JavaMethod method = vm.getLinkResolver().resolveStaticMethod(vm.getSymbols().java_lang_invoke_MethodHandleNatives(), "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;");
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
	public ArrayValue newMultiArray(SimpleArrayClass type, int[] lengths) {
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
	@Deprecated
	public void setupHiddenFrames(InstanceClass jc) {
		Symbols symbols = vm.getSymbols();
		InstanceClass throwable = symbols.java_lang_Throwable();
		if (throwable.isAssignableFrom(jc)) {
			for (JavaMethod jm : jc.methodArea().list()) {
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
					for (JavaMethod jm : jc.methodArea().list()) {
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
					for (JavaMethod jm : jc.methodArea().list()) {
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
	public JavaMethod getMethodBySlot(InstanceClass jc, int slot) {
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
	public JavaField getFieldBySlot(InstanceClass jc, int slot) {
		return jc.getFieldBySlot(slot);
	}

	/**
	 * Links method handle.
	 *
	 * @param handle Method handle.
	 * @return linked method handle.
	 */
	public InstanceValue linkMethodHandleConstant(InstanceClass caller, Handle handle) {
		VirtualMachine vm = this.vm;
		InstanceClass natives = vm.getSymbols().java_lang_invoke_MethodHandleNatives();
		JavaMethod link = vm.getLinkResolver().resolveStaticMethod(natives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;");
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
			Symbols symbols = vm.getSymbols();
			if (!symbols.java_lang_Error().isAssignableFrom(oop.getJavaClass())) {
				InstanceValue cnfe = newException(symbols.java_lang_NoClassDefFoundError(), name, oop);
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

	private ArrayValue newMultiArrayInner(SimpleArrayClass type, int[] lengths, int depth) {
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
			array.setReference(length, newMultiArrayInner((SimpleArrayClass) newType, lengths, next));
		}
		return array;
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

	private static void makeHiddenMethod(InstanceClass jc, String name, String desc) {
		JavaMethod mn = jc.getMethod(name, desc);
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

	private static void makeCallerSensitive(InstanceClass jc, String name, String desc) {
		JavaMethod jm = jc.getMethod(name, desc);
		MethodNode node = jm.getNode();
		node.access |= Modifier.ACC_CALLER_SENSITIVE;
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
