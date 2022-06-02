package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * JIT helper.
 *
 * @author xDark
 */
@SuppressWarnings("unused")
@UtilityClass
public class JitHelper {
	
	private static final Value[] NO_VALUES = {};

	public int arrayLoadInt(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getInt(index);
	}

	public Value arrayLoadInt(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadInt(array, index.asInt(), ctx));
	}

	public long arrayLoadLong(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getLong(index);
	}

	public Value arrayLoadLong(Value array, Value index, ExecutionContext ctx) {
		return LongValue.of(arrayLoadLong(array, index.asInt(), ctx));
	}

	public float arrayLoadFloat(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getFloat(index);
	}

	public Value arrayLoadFloat(Value array, Value index, ExecutionContext ctx) {
		return new FloatValue(arrayLoadFloat(array, index.asInt(), ctx));
	}

	public double arrayLoadDouble(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getDouble(index);
	}

	public Value arrayLoadDouble(Value array, Value index, ExecutionContext ctx) {
		return new DoubleValue(arrayLoadDouble(array, index.asInt(), ctx));
	}

	public char arrayLoadChar(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getChar(index);
	}

	public Value arrayLoadChar(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadChar(array, index.asInt(), ctx));
	}

	public short arrayLoadShort(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getShort(index);
	}

	public Value arrayLoadShort(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadShort(array, index.asInt(), ctx));
	}

	public byte arrayLoadByte(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getByte(index);
	}

	public Value arrayLoadByte(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadByte(array, index.asInt(), ctx));
	}

	public Value arrayLoadValue(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getValue(index);
	}

	public Value arrayLoadValue(Value array, Value index, ExecutionContext ctx) {
		return arrayLoadValue(array, index.asInt(), ctx);
	}

	public void arrayStoreLong(Value array, int index, long value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setLong(index, value);
	}

	public void arrayStoreLong(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreLong(array, index.asInt(), value.asLong(), ctx);
	}

	public void arrayStoreDouble(Value array, int index, double value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setDouble(index, value);
	}

	public void arrayStoreDouble(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreDouble(array, index.asInt(), value.asDouble(), ctx);
	}

	public void arrayStoreFloat(Value array, int index, float value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setFloat(index, value);
	}

	public void arrayStoreFloat(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreFloat(array, index.asInt(), value.asFloat(), ctx);
	}

	public void arrayStoreInt(Value array, int index, int value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setInt(index, value);
	}

	public void arrayStoreInt(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreInt(array, index.asInt(), value.asInt(), ctx);
	}

	public void arrayStoreChar(Value array, int index, char value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setChar(index, value);
	}

	public void arrayStoreChar(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreChar(array, index.asInt(), value.asChar(), ctx);
	}

	public void arrayStoreShort(Value array, int index, short value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setShort(index, value);
	}

	public void arrayStoreShort(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreShort(array, index.asInt(), value.asShort(), ctx);
	}

	public void arrayStoreByte(Value array, int index, byte value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setByte(index, value);
	}

	public void arrayStoreByte(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreByte(array, index.asInt(), value.asByte(), ctx);
	}

	public void arrayStoreValue(Value array, int index, Value value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);

		ObjectValue o = (ObjectValue) value;
		if (!value.isNull()) {
			JavaClass type = arr.getJavaClass().getComponentType();
			JavaClass valueType = o.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				VMSymbols symbols = ctx.getVM().getSymbols();
				helper.throwException(symbols.java_lang_ArrayStoreException(), valueType.getName());
			}
		}

		arr.setValue(index, (ObjectValue) value);
	}

	public void arrayStoreValue(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreValue(array, index.asInt(), value, ctx);
	}

	public Value compareFloat(Value a, Value b, int nan) {
		float v1 = a.asFloat();
		float v2 = b.asFloat();
		if (Float.isNaN(v1) || Float.isNaN(v2)) {
			return IntValue.of(nan);
		} else {
			return IntValue.of(Float.compare(v1, v2));
		}
	}

	public Value compareDouble(Value a, Value b, int nan) {
		double v1 = a.asDouble();
		double v2 = b.asDouble();
		if (Double.isNaN(v1) || Double.isNaN(v2)) {
			return IntValue.of(nan);
		} else {
			return IntValue.of(Double.compare(v1, v2));
		}
	}

	public Value getStaticA(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		return vm.getOperations().getStaticField(klass, name, desc);
	}

	public void getStatic(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		ctx.getStack().pushGeneric(vm.getOperations().getGenericStaticField(klass, name, desc));
	}

	// special intrinsic versions.
	public void getStaticFail(Object owner, Object field, long offset, ExecutionContext ctx) {
		if (owner instanceof String) {
			// Class was not found
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoClassDefFoundError(), (String) owner);
		}
		if (offset == -1L) {
			// Field was not found.
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoSuchFieldError(), (String) field);
		}
	}

	public long getStaticJ(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readLong(((InstanceJavaClass) owner).getOop(), offset);
	}

	public double getStaticD(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readDouble(((InstanceJavaClass) owner).getOop(), offset);
	}

	public int getStaticI(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readInt(((InstanceJavaClass) owner).getOop(), offset);
	}

	public float getStaticF(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readFloat(((InstanceJavaClass) owner).getOop(), offset);
	}

	public char getStaticC(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readChar(((InstanceJavaClass) owner).getOop(), offset);
	}

	public short getStaticS(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readShort(((InstanceJavaClass) owner).getOop(), offset);
	}

	public byte getStaticB(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readByte(((InstanceJavaClass) owner).getOop(), offset);
	}

	public Value getStaticA(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readValue(((InstanceJavaClass) owner).getOop(), offset);
	}

	public void putStaticA(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticField(klass, name, desc, (ObjectValue) value);
	}

	public void putStaticJ(long value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticLongField(klass, name, desc, value);
	}

	public void putStaticD(double value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticDoubleField(klass, name, desc, value);
	}

	public void putStaticI(int value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticIntField(klass, name, desc, value);
	}

	public void putStaticF(float value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticFloatField(klass, name, desc, value);
	}

	public void putStaticS(short value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticShortField(klass, name, desc, value);
	}

	public void putStaticC(char value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticCharField(klass, name, desc, value);
	}

	public void putStaticB(byte value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticByteField(klass, name, desc, value);
	}

	public void putStatic(String owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putStaticGenericField(klass, name, desc, ctx.getStack().popGeneric());
	}

	public Value getFieldA(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getField((ObjectValue) value, klass, name, desc);
	}

	public long getFieldJ(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getLongField((ObjectValue) value, klass, name, desc);
	}

	public double getFieldD(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getDoubleField((ObjectValue) value, klass, name, desc);
	}

	public int getFieldI(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getIntField((ObjectValue) value, klass, name, desc);
	}

	public float getFieldF(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getFloatField((ObjectValue) value, klass, name, desc);
	}

	public char getFieldC(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getCharField((ObjectValue) value, klass, name, desc);
	}

	public short getFieldS(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getShortField((ObjectValue) value, klass, name, desc);
	}

	public byte getFieldB(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getByteField((ObjectValue) value, klass, name, desc);
	}

	public Value getFieldGeneric(String owner, String name, String desc, ExecutionContext ctx) {
		ObjectValue value = ctx.getStack().pop();
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		return ctx.getOperations().getGenericField(value, klass, name, desc);
	}

	public void putFieldA(Value instance, Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putField((ObjectValue) instance, klass, name, desc, (ObjectValue) value);
	}

	public void putFieldJ(Value instance, long value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putLongField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldD(Value instance, double value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putDoubleField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldI(Value instance, int value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putIntField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldF(Value instance, float value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putFloatField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldC(Value instance, char value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putCharField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldS(Value instance, short value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putShortField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldB(Value instance, byte value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putByteField((ObjectValue) instance, klass, name, desc, value);
	}

	public void putFieldGeneric(String owner, String name, String desc, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value value = stack.popGeneric();
		Value instance = stack.pop();
		InstanceJavaClass klass = getOrFindClass(owner, ctx);
		ctx.getOperations().putGenericField((ObjectValue) instance, klass, name, desc, value);
	}

	public void invokeVirtual(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		Stack stack = ctx.getStack();
		Type[] args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (Type arg : args) {
			localsLength += arg.getSize();
		}
		Value[] locals = new Value[localsLength];
		while(localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		ExecutionContext result = vm.getHelper().invokeVirtual(name, desc, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public Value invokeSpecial(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		ExecutionContext result = helper.invokeExact(klass, name, desc, NO_VALUES, locals);
		return result.getResult();
	}

	public void invokeSpecial(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		Stack stack = ctx.getStack();
		Type[] args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (Type arg : args) {
			localsLength += arg.getSize();
		}
		Value[] locals = new Value[localsLength];
		while(localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		ExecutionContext result = helper.invokeExact(klass, name, desc, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeStatic(String owner, String name, String desc, ExecutionContext ctx) {
		JavaMethod mn = resolveStaticMethod(owner, name, desc, ctx);
		Stack stack = ctx.getStack();
		int localsLength = mn.getMaxArgs();
		Value[] locals = new Value[localsLength];
		while(localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		ExecutionContext result = helper.invokeStatic(mn.getOwner(), mn, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	// special intrinsic version.
	public Value invokeFail(Object owner, Object method, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		VMSymbols symbols = ctx.getSymbols();
		if (owner instanceof String) {
			// Class was not found
			helper.throwException(symbols.java_lang_NoClassDefFoundError(), (String) owner);
		}
		ctx.getHelper().throwException(symbols.java_lang_NoSuchMethodError(), (String) method);
		return null;
	}

	public Value invokeStatic(Value[] locals, Object owner, Object method, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		JavaMethod mn = (JavaMethod) method;
		Stack stack = ctx.getStack();
		ExecutionContext result = helper.invokeStatic((InstanceJavaClass) owner, mn, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeStatic(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		JavaMethod mn = resolveStaticMethod(owner, name, desc, ctx);
		Stack stack = ctx.getStack();
		ExecutionContext result = helper.invokeStatic(mn.getOwner(), mn, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeSpecial(Value[] locals, Object owner, Object method, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		Stack stack = ctx.getStack();
		JavaMethod mn = (JavaMethod) method;
		ExecutionContext result = helper.invokeExact((InstanceJavaClass) owner, mn, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeVirtual(Value[] locals, Object name, Object desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		Stack stack = ctx.getStack();
		ExecutionContext result = vm.getHelper().invokeVirtual((String) name, (String) desc, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeInterface(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		ExecutionContext result = helper.invokeInterface(klass, name, desc, NO_VALUES, locals);
		return result.getResult();
	}

	public void invokeInterface(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		Stack stack = ctx.getStack();
		Type[] args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (Type arg : args) {
			localsLength += arg.getSize();
		}
		Value[] locals = new Value[localsLength];
		while(localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		ExecutionContext result = helper.invokeInterface(klass, name, desc, NO_VALUES, locals);
		Value v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public Value allocateInstance(Object type, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		// TODO checks like in UnsafeNatives
		InstanceJavaClass klass = (InstanceJavaClass) type;
		klass.initialize();
		InstanceValue instance = vm.getMemoryManager().newInstance(klass);
		vm.getHelper().initializeDefaultValues(instance);
		return instance;
	}

	public Value allocateInstance(String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		JavaClass type = helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		// TODO checks like in UnsafeNatives
		InstanceValue instance = vm.getMemoryManager().newInstance((InstanceJavaClass) type);
		helper.initializeDefaultValues(instance);
		return instance;
	}

	public Value allocatePrimitiveArray(int length, int operand, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		VirtualMachine vm = ctx.getVM();
		vm.getHelper().checkArrayLength(length);
		VMPrimitives primitives = vm.getPrimitives();
		VMHelper helper = vm.getHelper();
		ArrayValue array;
		switch(operand) {
			case T_LONG:
				array = helper.newArray(primitives.longPrimitive(), length);
				break;
			case T_DOUBLE:
				array = helper.newArray(primitives.doublePrimitive(), length);
				break;
			case T_INT:
				array = helper.newArray(primitives.intPrimitive(), length);
				break;
			case T_FLOAT:
				array = helper.newArray(primitives.floatPrimitive(), length);
				break;
			case T_CHAR:
				array = helper.newArray(primitives.charPrimitive(), length);
				break;
			case T_SHORT:
				array = helper.newArray(primitives.shortPrimitive(), length);
				break;
			case T_BYTE:
				array = helper.newArray(primitives.bytePrimitive(), length);
				break;
			case T_BOOLEAN:
				array = helper.newArray(primitives.booleanPrimitive(), length);
				break;
			default:
				throw new IllegalStateException("Illegal array type: " + operand);
		}
		return array;
	}

	public Value allocatePrimitiveArray(int operand, ExecutionContext ctx) {
		return allocatePrimitiveArray(ctx.getStack().pop().asInt(), operand, ctx);
	}

	public Value allocateValueArray(int length, Object klass, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray((JavaClass) klass, length);
	}

	public Value allocateValueArray(int length, String desc, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		return allocateValueArray(length, helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, false), ctx);
	}

	public Value allocateValueArray(String desc, ExecutionContext ctx) {
		return allocateValueArray(ctx.getStack().pop().asInt(), desc, ctx);
	}

	public int getArrayLength(Value value, ExecutionContext ctx) {
		ArrayValue array = ctx.getHelper().checkNotNullArray((ObjectValue) value);
		return array.getLength();
	}

	public int getArrayLength(ExecutionContext ctx) {
		return getArrayLength(ctx.getStack().pop(), ctx);
	}

	public void throwException(Value exception, ExecutionContext ctx) {
		if (exception.isNull()) {
			// NPE it is then.
			VirtualMachine vm = ctx.getVM();
			InstanceJavaClass exceptionClass = vm.getSymbols().java_lang_NullPointerException();
			exceptionClass.initialize();
			exception = vm.getMemoryManager().newInstance(exceptionClass);
			vm.getHelper().invokeExact(exceptionClass, "<init>", "()V", NO_VALUES, new Value[]{exception});
		}
		throw new VMException((InstanceValue) exception);
	}

	public void throwException(ExecutionContext ctx) {
		throwException(ctx.getStack().pop(), ctx);
	}

	public Value checkCast(Value value, Object type, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		if (!value.isNull()) {
			JavaClass against = ((ObjectValue) value).getJavaClass();
			JavaClass jc = (JavaClass) type;
			if (!jc.isAssignableFrom(against)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_ClassCastException(), against.getName() + " cannot be cast to " + jc.getName());
			}
		}
		return value;
	}

	public Value checkCast(Value value, String desc, ExecutionContext ctx) {
		// It seems like JVM can pass descriptors instead of internal names?
		if (!desc.isEmpty() && desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';') {
			desc = desc.substring(1, desc.length() - 1);
		}
		JavaClass type = ctx.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		return checkCast(value, type, ctx);
	}

	public void checkCast(String desc, ExecutionContext ctx) {
		checkCast(ctx.getStack().peek(), desc, ctx);
	}

	public boolean instanceofResult(Value value, Object javaClass, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		if (javaClass instanceof InstanceJavaClass) {
			((InstanceJavaClass) javaClass).loadNoResolve();
		}
		if (value.isNull()) {
			return false;
		} else {
			return ((JavaClass) javaClass).isAssignableFrom(((ObjectValue) value).getJavaClass());
		}
	}

	public boolean instanceofResult(Value value, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		JavaClass javaClass = vm.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), desc, false);
		return instanceofResult(value, javaClass, ctx);
	}

	public void instanceofResult(String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		Stack stack = ctx.getStack();
		ObjectValue value = stack.<ObjectValue>pop();
		stack.push(instanceofResult(value, desc, ctx) ? IntValue.ONE : IntValue.ZERO);
	}

	public void monitorEnter(Value value, ExecutionContext ctx) {
		ctx.monitorEnter((ObjectValue) value);
	}

	public void monitorEnter(ExecutionContext ctx) {
		monitorEnter(ctx.getStack().pop(), ctx);
	}

	public void monitorExit(Value value, ExecutionContext ctx) {
		ctx.monitorExit((ObjectValue) value);
	}

	public void monitorExit(ExecutionContext ctx) {
		monitorExit(ctx.getStack().pop(), ctx);
	}

	public Value multiNewArray(String desc, int dimensions, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		JavaClass type = helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		Stack stack = ctx.getStack();
		int[] lengths = new int[dimensions];
		while(dimensions-- != 0) {
			lengths[dimensions] = stack.pop().asInt();
		}
		return helper.newMultiArray((ArrayJavaClass) type, lengths);
	}

	public Value classLdc(String desc, ExecutionContext ctx) {
		return ctx.getHelper().valueFromLdc(Type.getObjectType(desc));
	}

	public Value methodLdc(String desc, ExecutionContext ctx) {
		return ctx.getHelper().valueFromLdc(Type.getMethodType(desc));
	}

	public void intToByte(ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v = stack.peek();
		byte b = v.asByte();
		if (v.asInt() != b) {
			stack.pop();
			stack.push(IntValue.of(b));
		}
	}

	public void intToChar(ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v = stack.peek();
		char c = v.asChar();
		if (v.asInt() != c) {
			stack.pop();
			stack.push(IntValue.of(c));
		}
	}

	public void intToShort(ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v = stack.peek();
		short s = v.asShort();
		if (v.asInt() != s) {
			stack.pop();
			stack.push(IntValue.of(s));
		}
	}

	public VMException exceptionCaught(VMException ex, Object $classes, ExecutionContext ctx) {
		InstanceJavaClass exceptionType = ex.getOop().getJavaClass();
		ObjectValue loader = ctx.getOwner().getClassLoader();
		VMHelper helper = ctx.getHelper();
		Object[] classes = (Object[]) $classes;
		for (int i = 0, j = classes.length; i < j; i++) {
			Object $type = classes[0];
			InstanceJavaClass type;
			if ($type instanceof InstanceJavaClass) {
				type = (InstanceJavaClass) $type;
			} else {
				type = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), (String) $type, true);
				classes[i] = type;
			}
			if (type.isAssignableFrom(exceptionType)) {
				return ex;
			}
		}
		throw ex;
	}

	public Value invokeDynamic(Value[] args, Object constants, int index, ExecutionContext ctx) {
		Object[] arr = (Object[]) constants;
		Object operand = arr[index];
		DynamicLinkResult result;
		if (operand instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode insn = (InvokeDynamicInsnNode) operand;
			InstanceValue linked = InvokeDynamicLinker.linkCall(insn, ctx);
			result = new DynamicLinkResult(linked, insn.desc);
			arr[index] = result;
		} else {
			result = (DynamicLinkResult) operand;
		}
		return InvokeDynamicLinker.dynamicCall(args, result.desc, result.handle, ctx);
	}

	private static JavaMethod resolveStaticMethod(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		/*
		JavaMethod mn = klass.getStaticMethodRecursively(name, desc);
		if (mn == null) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchMethodError(), owner + '.' + name + desc);
		}
		return mn;
		*/
		return vm.getLinkResolver().resolveStaticMethod(klass, name, desc);
	}

	public void tryMonitorExit(ObjectValue value, ExecutionContext ctx) {
		try {
			value.monitorExit();
		} catch (IllegalMonitorStateException ignored) {
			VirtualMachine vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException());
		}
	}

	private static InstanceJavaClass getOrFindClass(Object owner, ExecutionContext ctx) {
		InstanceJavaClass klass;
		if (owner instanceof InstanceJavaClass) {
			klass = (InstanceJavaClass) owner;
		} else {
			klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		return klass;
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class DynamicLinkResult {
		final InstanceValue handle;
		final String desc;
	}
}
