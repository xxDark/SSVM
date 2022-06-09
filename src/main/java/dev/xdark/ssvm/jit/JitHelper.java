package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.AsmUtil;
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
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getInt(index);
	}

	public Value arrayLoadInt(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadInt(array, index.asInt(), ctx));
	}

	public long arrayLoadLong(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getLong(index);
	}

	public Value arrayLoadLong(Value array, Value index, ExecutionContext ctx) {
		return LongValue.of(arrayLoadLong(array, index.asInt(), ctx));
	}

	public float arrayLoadFloat(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getFloat(index);
	}

	public Value arrayLoadFloat(Value array, Value index, ExecutionContext ctx) {
		return new FloatValue(arrayLoadFloat(array, index.asInt(), ctx));
	}

	public double arrayLoadDouble(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getDouble(index);
	}

	public Value arrayLoadDouble(Value array, Value index, ExecutionContext ctx) {
		return new DoubleValue(arrayLoadDouble(array, index.asInt(), ctx));
	}

	public char arrayLoadChar(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getChar(index);
	}

	public Value arrayLoadChar(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadChar(array, index.asInt(), ctx));
	}

	public short arrayLoadShort(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getShort(index);
	}

	public Value arrayLoadShort(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadShort(array, index.asInt(), ctx));
	}

	public byte arrayLoadByte(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getByte(index);
	}

	public Value arrayLoadByte(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadByte(array, index.asInt(), ctx));
	}

	public Value arrayLoadValue(Value array, int index, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		return arr.getValue(index);
	}

	public Value arrayLoadValue(Value array, Value index, ExecutionContext ctx) {
		return arrayLoadValue(array, index.asInt(), ctx);
	}

	public void arrayStoreLong(Value array, int index, long value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setLong(index, value);
	}

	public void arrayStoreLong(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreLong(array, index.asInt(), value.asLong(), ctx);
	}

	public void arrayStoreDouble(Value array, int index, double value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setDouble(index, value);
	}

	public void arrayStoreDouble(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreDouble(array, index.asInt(), value.asDouble(), ctx);
	}

	public void arrayStoreFloat(Value array, int index, float value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setFloat(index, value);
	}

	public void arrayStoreFloat(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreFloat(array, index.asInt(), value.asFloat(), ctx);
	}

	public void arrayStoreInt(Value array, int index, int value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setInt(index, value);
	}

	public void arrayStoreInt(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreInt(array, index.asInt(), value.asInt(), ctx);
	}

	public void arrayStoreChar(Value array, int index, char value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setChar(index, value);
	}

	public void arrayStoreChar(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreChar(array, index.asInt(), value.asChar(), ctx);
	}

	public void arrayStoreShort(Value array, int index, short value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setShort(index, value);
	}

	public void arrayStoreShort(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreShort(array, index.asInt(), value.asShort(), ctx);
	}

	public void arrayStoreByte(Value array, int index, byte value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
		helper.rangeCheck(arr, index);
		arr.setByte(index, value);
	}

	public void arrayStoreByte(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreByte(array, index.asInt(), value.asByte(), ctx);
	}

	public void arrayStoreValue(Value array, int index, Value value, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		ArrayValue arr = helper.checkNotNull(array);
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
		return memoryManager.readLong(getClassOop(owner), offset);
	}

	public double getStaticD(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readDouble(getClassOop(owner), offset);
	}

	public int getStaticI(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readInt(getClassOop(owner), offset);
	}

	public float getStaticF(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readFloat(getClassOop(owner), offset);
	}

	public char getStaticC(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readChar(getClassOop(owner), offset);
	}

	public short getStaticS(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readShort(getClassOop(owner), offset);
	}

	public byte getStaticB(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readByte(getClassOop(owner), offset);
	}

	public Value getStaticA(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readValue(getClassOop(owner), offset);
	}

	public void getStaticA(Value value, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		vm.getOperations().putStaticGenericField(klass, name, desc, value);
	}

	public void putStaticA(Value value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeValue(getClassOop(owner), offset, (ObjectValue) value);
	}

	public void putStaticJ(long value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeLong(getClassOop(owner), offset, value);
	}

	public void putStaticD(double value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeDouble(getClassOop(owner), offset, value);
	}

	public void putStaticI(int value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeInt(getClassOop(owner), offset, value);
	}

	public void putStaticF(float value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeFloat(getClassOop(owner), offset, value);
	}

	public void putStaticS(short value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeShort(getClassOop(owner), offset, value);
	}

	public void putStaticC(char value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeChar(getClassOop(owner), offset, value);
	}

	public void putStaticB(byte value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeByte(getClassOop(owner), offset, value);
	}

	public void putStaticZ(boolean value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeBoolean(getClassOop(owner), offset, value);
	}

	public Value getFieldA(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getField((ObjectValue) value, klass, name, desc);
	}

	public long getFieldJ(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getLongField((ObjectValue) value, klass, name);
	}

	public double getFieldD(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getDoubleField((ObjectValue) value, klass, name);
	}

	public int getFieldI(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getIntField((ObjectValue) value, klass, name);
	}

	public float getFieldF(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getFloatField((ObjectValue) value, klass, name);
	}

	public char getFieldC(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getCharField((ObjectValue) value, klass, name);
	}

	public short getFieldS(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getShortField((ObjectValue) value, klass, name);
	}

	public byte getFieldB(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getByteField((ObjectValue) value, klass, name);
	}

	public boolean getFieldZ(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getBooleanField((ObjectValue) value, klass, name);
	}

	public Value getFieldA(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readValue(o, offset);
	}

	public long getFieldJ(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readLong(o, offset);
	}

	public double getFieldD(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readDouble(o, offset);
	}

	public int getFieldI(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readInt(o, offset);
	}

	public float getFieldF(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readFloat(o, offset);
	}

	public char getFieldC(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readChar(o, offset);
	}

	public short getFieldS(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readShort(o, offset);
	}

	public byte getFieldB(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readByte(o, offset);
	}

	public boolean getFieldZ(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readBoolean(o, offset);
	}

	public Value getFieldGeneric(String owner, String name, String desc, ExecutionContext ctx) {
		ObjectValue value = ctx.getStack().pop();
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getGenericField(value, klass, name, desc);
	}

	public void putFieldA(Value instance, Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putField((ObjectValue) instance, klass, name, desc, (ObjectValue) value);
	}

	public void putFieldJ(Value instance, long value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putLongField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldD(Value instance, double value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putDoubleField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldI(Value instance, int value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putIntField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldF(Value instance, float value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putFloatField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldC(Value instance, char value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putCharField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldS(Value instance, short value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putShortField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldB(Value instance, byte value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putByteField((ObjectValue) instance, klass, name, value);
	}

	public void putFieldZ(Value instance, boolean value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putBooleanField((ObjectValue) instance, klass, name, value);
	}

	// TODO GC
	public void putFieldA(Value instance, Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeValue(o, offset, (ObjectValue) value);
	}

	public void putFieldJ(Value instance, long value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeLong(o, offset, value);
	}

	public void putFieldD(Value instance, double value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeDouble(o, offset, value);
	}

	public void putFieldI(Value instance, int value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeInt(o, offset, value);
	}

	public void putFieldF(Value instance, float value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeFloat(o, offset, value);
	}

	public void putFieldC(Value instance, char value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeChar(o, offset, value);
	}

	public void putFieldS(Value instance, short value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeShort(o, offset, value);
	}

	public void putFieldB(Value instance, byte value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeByte(o, offset, value);
	}

	public void putFieldZ(Value instance, boolean value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeBoolean(o, offset, value);
	}

	public Value invokeSpecial(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		ExecutionContext result = helper.invokeExact(klass, name, desc, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeStatic(Value[] locals, Object method, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		JavaMethod mn = (JavaMethod) method;
		Stack stack = ctx.getStack();
		ExecutionContext result = helper.invokeStatic(mn, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeStatic(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		JavaMethod mn = resolveStaticMethod(owner, name, desc, ctx);
		Stack stack = ctx.getStack();
		ExecutionContext result = helper.invokeStatic(mn, NO_VALUES, locals);
		return result.getResult();
	}

	public Value invokeSpecial(Value[] locals, Object method, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		Stack stack = ctx.getStack();
		JavaMethod mn = (JavaMethod) method;
		ExecutionContext result = helper.invokeExact(mn, NO_VALUES, locals);
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

	public ObjectValue allocateInstance(InstanceJavaClass klass, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		// TODO checks like in UnsafeNatives
		MemoryManager memoryManager = vm.getMemoryManager();
		klass.initialize();
		return memoryManager.newInstance(klass);
	}

	// For JIT to avoid checkcasts in generated code, do not use
	public Value allocateInstance(Object type, ExecutionContext ctx) {
		return allocateInstance((InstanceJavaClass) type, ctx);
	}

	public Value allocateInstance(String desc, ExecutionContext ctx) {
		return allocateInstance(getOrFindInstanceClass(desc, ctx), ctx);
	}

	public ArrayValue allocateLongArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().longPrimitive(), length);
	}

	public ArrayValue allocateDoubleArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().doublePrimitive(), length);
	}

	public Value allocateIntArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().intPrimitive(), length);
	}

	public Value allocateFloatArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().floatPrimitive(), length);
	}

	public Value allocateCharArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().charPrimitive(), length);
	}

	public Value allocateShortArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().shortPrimitive(), length);
	}

	public Value allocateByteArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().bytePrimitive(), length);
	}

	public Value allocateBooleanArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().booleanPrimitive(), length);
	}

	public Value allocatePrimitiveArray(int length, int operand, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		VMPrimitives primitives = vm.getPrimitives();
		ArrayValue array;
		switch (operand) {
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
		return ctx.getOperations().allocateArray((JavaClass) klass, length);
	}

	public Value allocateValueArray(int length, String desc, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		return allocateValueArray(length, helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, false), ctx);
	}

	public int getArrayLength(ObjectValue value, ExecutionContext ctx) {
		ArrayValue array = ctx.getHelper().checkNotNull(value);
		return array.getLength();
	}

	public void throwException(Value exception, ExecutionContext ctx) {
		ctx.getOperations().throwException((ObjectValue) exception);
	}

	public ObjectValue checkCast(Value value, JavaClass jc, ExecutionContext ctx) {
		return ctx.getOperations().checkCast((ObjectValue) value, jc);
	}

	public Value checkCast(Value value, Object rawDesc, ExecutionContext ctx) {
		String desc = AsmUtil.normalizeDescriptor((String) rawDesc);
		JavaClass type = ctx.getHelper().tryFindClass(ctx.getClassLoader(), desc, true);
		return ctx.getOperations().checkCast((ObjectValue) value, type);
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
		ObjectValue value = stack.pop();
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
		while (dimensions-- != 0) {
			lengths[dimensions] = stack.pop().asInt();
		}
		return helper.newMultiArray((ArrayJavaClass) type, lengths);
	}

	public Value multiNewArray(Object klass, int[] dimensions, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		Stack stack = ctx.getStack();
		return helper.newMultiArray((ArrayJavaClass) klass, dimensions);
	}

	public Value multiNewArray(String klass, int[] dimensions, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		Stack stack = ctx.getStack();
		return helper.newMultiArray((ArrayJavaClass) getOrFindClass(klass, ctx), dimensions);
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
			Object $type = classes[i];
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
		InvokeDynamicLinker invokeDynamicLinker = ctx.getInvokeDynamicLinker();
		DynamicLinkResult result;
		if (operand instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode insn = (InvokeDynamicInsnNode) operand;
			InstanceValue linked = invokeDynamicLinker.linkCall(insn, ctx.getOwner());
			result = new DynamicLinkResult(linked, insn.desc);
			arr[index] = result;
		} else {
			result = (DynamicLinkResult) operand;
		}
		return invokeDynamicLinker.dynamicCall(args, result.desc, result.handle);
	}

	private static JavaMethod resolveStaticMethod(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
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

	private static InstanceJavaClass getOrFindInstanceClass(Object owner, ExecutionContext ctx) {
		InstanceJavaClass klass;
		if (owner instanceof InstanceJavaClass) {
			klass = (InstanceJavaClass) owner;
			klass.initialize();
		} else {
			klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		return klass;
	}

	private static JavaClass getOrFindClass(Object owner, ExecutionContext ctx) {
		JavaClass klass;
		if (owner instanceof JavaClass) {
			klass = (JavaClass) owner;
			klass.initialize();
		} else {
			klass = ctx.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		return klass;
	}

	private static InstanceValue getClassOop(Object klass) {
		InstanceJavaClass jc = (InstanceJavaClass) klass;
		jc.initialize();
		return jc.getOop();
	}

	private static ObjectValue nonNull(Value value, ExecutionContext ctx) {
		return ctx.getHelper().checkNotNull(value);
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class DynamicLinkResult {
		final InstanceValue handle;
		final String desc;
	}
}
