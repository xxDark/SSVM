package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.value.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
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

	public int arrayLoadInt(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getInt(index);
	}

	public Value arrayLoadInt(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadInt(array, index.asInt(), ctx));
	}

	public long arrayLoadLong(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getLong(index);
	}

	public Value arrayLoadLong(Value array, Value index, ExecutionContext ctx) {
		return LongValue.of(arrayLoadLong(array, index.asInt(), ctx));
	}

	public float arrayLoadFloat(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getFloat(index);
	}

	public Value arrayLoadFloat(Value array, Value index, ExecutionContext ctx) {
		return new FloatValue(arrayLoadFloat(array, index.asInt(), ctx));
	}

	public double arrayLoadDouble(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getDouble(index);
	}

	public Value arrayLoadDouble(Value array, Value index, ExecutionContext ctx) {
		return new DoubleValue(arrayLoadDouble(array, index.asInt(), ctx));
	}

	public char arrayLoadChar(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getChar(index);
	}

	public Value arrayLoadChar(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadChar(array, index.asInt(), ctx));
	}

	public short arrayLoadShort(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getShort(index);
	}

	public Value arrayLoadShort(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadShort(array, index.asInt(), ctx));
	}

	public byte arrayLoadByte(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getByte(index);
	}

	public Value arrayLoadByte(Value array, Value index, ExecutionContext ctx) {
		return IntValue.of(arrayLoadByte(array, index.asInt(), ctx));
	}

	public Value arrayLoadValue(Value array, int index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		return arr.getValue(index);
	}

	public Value arrayLoadValue(Value array, Value index, ExecutionContext ctx) {
		return arrayLoadValue(array, index.asInt(), ctx);
	}

	public void arrayStoreLong(Value array, int index, long value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setLong(index, value);
	}

	public void arrayStoreLong(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreLong(array, index.asInt(), value.asLong(), ctx);
	}

	public void arrayStoreDouble(Value array, int index, double value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setDouble(index, value);
	}

	public void arrayStoreDouble(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreDouble(array, index.asInt(), value.asDouble(), ctx);
	}

	public void arrayStoreFloat(Value array, int index, float value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setFloat(index, value);
	}

	public void arrayStoreFloat(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreFloat(array, index.asInt(), value.asFloat(), ctx);
	}

	public void arrayStoreInt(Value array, int index, int value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setInt(index, value);
	}

	public void arrayStoreInt(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreInt(array, index.asInt(), value.asInt(), ctx);
	}

	public void arrayStoreChar(Value array, int index, char value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setChar(index, value);
	}

	public void arrayStoreChar(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreChar(array, index.asInt(), value.asChar(), ctx);
	}

	public void arrayStoreShort(Value array, int index, short value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setShort(index, value);
	}

	public void arrayStoreShort(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreShort(array, index.asInt(), value.asShort(), ctx);
	}

	public void arrayStoreByte(Value array, int index, byte value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);
		arr.setByte(index, value);
	}

	public void arrayStoreByte(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreByte(array, index.asInt(), value.asByte(), ctx);
	}

	public void arrayStoreValue(Value array, int index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		helper.rangeCheck(arr, index);

		val o = (ObjectValue) value;
		if (!value.isNull()) {
			val type = arr.getJavaClass().getComponentType();
			val valueType = o.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				val symbols = ctx.getVM().getSymbols();
				helper.throwException(symbols.java_lang_ArrayStoreException, valueType.getName());
			}
		}

		arr.setValue(index, (ObjectValue) value);
	}

	public void arrayStoreValue(Value array, Value index, Value value, ExecutionContext ctx) {
		arrayStoreValue(array, index.asInt(), value, ctx);
	}

	public Value compareFloat(Value a, Value b, int nan) {
		val v1 = a.asFloat();
		val v2 = b.asFloat();
		if (Float.isNaN(v1) || Float.isNaN(v2)) {
			return IntValue.of(nan);
		} else {
			return IntValue.of(Float.compare(v1, v2));
		}
	}

	public Value compareDouble(Value a, Value b, int nan) {
		val v1 = a.asDouble();
		val v2 = b.asDouble();
		if (Double.isNaN(v1) || Double.isNaN(v2)) {
			return IntValue.of(nan);
		} else {
			return IntValue.of(Double.compare(v1, v2));
		}
	}

	public Value getStaticA(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		while (klass != null) {
			val value = klass.getStaticValue(name, desc);
			if (value != null) {
				return value;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		return null;
	}

	public void getStatic(String owner, String name, String desc, ExecutionContext ctx) {
		ctx.getStack().pushGeneric(getStaticA(owner, name, desc, ctx));
	}

	// special intrinsic versions.
	public void getStaticFail(Object owner, Object field, long offset, ExecutionContext ctx) {
		if (owner instanceof String) {
			// Class was not found
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoClassDefFoundError, (String) owner);
		}
		if (offset == -1L) {
			// Field was not found.
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoSuchFieldError, (String) field);
		}
	}

	public long getStaticJ(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readLong(((InstanceJavaClass) owner).getOop(), offset);
	}

	public double getStaticD(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readDouble(((InstanceJavaClass) owner).getOop(), offset);
	}

	public int getStaticI(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readInt(((InstanceJavaClass) owner).getOop(), offset);
	}

	public float getStaticF(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readFloat(((InstanceJavaClass) owner).getOop(), offset);
	}

	public char getStaticC(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readChar(((InstanceJavaClass) owner).getOop(), offset);
	}

	public short getStaticS(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readShort(((InstanceJavaClass) owner).getOop(), offset);
	}

	public byte getStaticB(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readByte(((InstanceJavaClass) owner).getOop(), offset);
	}

	public Value getStaticA(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readValue(((InstanceJavaClass) owner).getOop(), offset);
	}

	public void putStaticA(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass klass;
		if (owner instanceof InstanceJavaClass) {
			klass = (InstanceJavaClass) owner;
		} else {
			klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		if (!klass.setFieldValue(name, desc, value)) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		}
	}

	public void putStaticJ(long value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(LongValue.of(value), owner, name, desc, ctx);
	}

	public void putStaticD(double value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(new DoubleValue(value), owner, name, desc, ctx);
	}

	public void putStaticI(int value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(IntValue.of(value), owner, name, desc, ctx);
	}

	public void putStaticF(float value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(new FloatValue(value), owner, name, desc, ctx);
	}

	public void putStaticF(char value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(IntValue.of(value), owner, name, desc, ctx);
	}

	public void putStaticS(short value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(IntValue.of(value), owner, name, desc, ctx);
	}

	public void putStaticC(char value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(IntValue.of(value), owner, name, desc, ctx);
	}

	public void putStaticB(byte value, Object owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(IntValue.of(value), owner, name, desc, ctx);
	}

	public void putStatic(String owner, String name, String desc, ExecutionContext ctx) {
		putStaticA(ctx.getStack().popGeneric(), owner, name, desc, ctx);
	}

	public Value getFieldA(Value $instance, Object owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass klass;
		if (owner instanceof InstanceJavaClass) {
			klass = (InstanceJavaClass) owner;
		} else {
			klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		val instance = helper.<InstanceValue>checkNotNull($instance);
		long offset = helper.getFieldOffset(klass, instance.getJavaClass(), name, desc);
		if (offset == -1L) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		}
		Value value;
		val manager = vm.getMemoryManager();
		offset += manager.valueBaseOffset(instance);
		switch (desc.charAt(0)) {
			case 'J':
				value = LongValue.of(manager.readLong(instance, offset));
				break;
			case 'D':
				value = new DoubleValue(manager.readDouble(instance, offset));
				break;
			case 'I':
				value = IntValue.of(manager.readInt(instance, offset));
				break;
			case 'F':
				value = new FloatValue(manager.readFloat(instance, offset));
				break;
			case 'C':
				value = IntValue.of(manager.readChar(instance, offset));
				break;
			case 'S':
				value = IntValue.of(manager.readShort(instance, offset));
				break;
			case 'B':
				value = IntValue.of(manager.readByte(instance, offset));
				break;
			case 'Z':
				value = manager.readBoolean(instance, offset) ? IntValue.ONE : IntValue.ZERO;
				break;
			default:
				value = manager.readValue(instance, offset);
		}
		return value;
	}

	public long getFieldJ(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asLong();
	}

	public double getFieldD(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asDouble();
	}

	public int getFieldI(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asInt();
	}

	public float getFieldF(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asFloat();
	}

	public char getFieldC(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asChar();
	}

	public short getFieldS(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asShort();
	}

	public byte getFieldB(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(value, owner, name, desc, ctx).asByte();
	}

	public Value getField(String owner, String name, String desc, ExecutionContext ctx) {
		return getFieldA(ctx.getStack().pop(), owner, name, desc, ctx);
	}

	public void putFieldA(Value $instance, Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass klass;
		if (owner instanceof InstanceJavaClass) {
			klass = (InstanceJavaClass) owner;
		} else {
			klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), (String) owner, true);
		}
		val instance = helper.<InstanceValue>checkNotNull($instance);
		long offset = helper.getFieldOffset(klass, instance.getJavaClass(), name, desc);
		if (offset == -1L) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		}
		val manager = vm.getMemoryManager();
		offset += manager.valueBaseOffset(instance);
		switch (desc.charAt(0)) {
			case 'J':
				manager.writeLong(instance, offset, value.asLong());
				break;
			case 'D':
				manager.writeDouble(instance, offset, value.asDouble());
				break;
			case 'I':
				manager.writeInt(instance, offset, value.asInt());
				break;
			case 'F':
				manager.writeFloat(instance, offset, value.asFloat());
				break;
			case 'C':
				manager.writeChar(instance, offset, value.asChar());
				break;
			case 'S':
				manager.writeShort(instance, offset, value.asShort());
				break;
			case 'B':
				manager.writeByte(instance, offset, value.asByte());
				break;
			case 'Z':
				manager.writeBoolean(instance, offset, value.asBoolean());
				break;
			default:
				manager.writeValue(instance, offset, (ObjectValue) value);
		}
	}

	public void putFieldJ(Value instance, long value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, LongValue.of(value), owner, name, desc, ctx);
	}

	public void putFieldD(Value instance, double value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, new DoubleValue(value), owner, name, desc, ctx);
	}

	public void putFieldI(Value instance, int value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, IntValue.of(value), owner, name, desc, ctx);
	}

	public void putFieldF(Value instance, float value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, new FloatValue(value), owner, name, desc, ctx);
	}

	public void putFieldC(Value instance, char value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, IntValue.of(value), owner, name, desc, ctx);
	}

	public void putFieldS(Value instance, short value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, IntValue.of(value), owner, name, desc, ctx);
	}

	public void putFieldB(Value instance, byte value, Object owner, String name, String desc, ExecutionContext ctx) {
		putFieldA(instance, IntValue.of(value), owner, name, desc, ctx);
	}

	public void putField(String owner, String name, String desc, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val value = stack.popGeneric();
		val instance = stack.pop();
		putFieldA(instance, value, owner, name, desc, ctx);
	}

	public void invokeVirtual(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (val arg : args) {
			localsLength += arg.getSize();
		}
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		val result = vm.getHelper().invokeVirtual(name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public Value invokeSpecial(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		val result = helper.invokeExact(klass, name, desc, new Value[0], locals);
		return result.getResult();
	}

	public void invokeSpecial(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (val arg : args) {
			localsLength += arg.getSize();
		}
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		val result = helper.invokeExact(klass, name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeStatic(String owner, String name, String desc, ExecutionContext ctx) {
		val mn = resolveStaticMethod(owner, name, desc, ctx);
		val stack = ctx.getStack();
		int localsLength = mn.getMaxArgs();
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val result = helper.invokeStatic(mn.getOwner(), mn, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	// special intrinsic version.
	public Value invokeFail(Object owner, Object method, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val symbols = ctx.getSymbols();
		if (owner instanceof String) {
			// Class was not found
			helper.throwException(symbols.java_lang_NoClassDefFoundError, (String) owner);
		}
		ctx.getHelper().throwException(symbols.java_lang_NoSuchMethodError, (String) method);
		return null;
	}

	public Value invokeStatic(Value[] locals, Object owner, Object method, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val mn = (JavaMethod) method;
		val stack = ctx.getStack();
		val result = helper.invokeStatic((InstanceJavaClass) owner, mn, new Value[0], locals);
		return result.getResult();
	}

	public Value invokeStatic(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val mn = resolveStaticMethod(owner, name, desc, ctx);
		val stack = ctx.getStack();
		val result = helper.invokeStatic(mn.getOwner(), mn, new Value[0], locals);
		return result.getResult();
	}

	public Value invokeSpecial(Value[] locals, Object owner, Object method, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val stack = ctx.getStack();
		val mn = (JavaMethod) method;
		val result = helper.invokeExact((InstanceJavaClass) owner, mn, new Value[0], locals);
		return result.getResult();
	}

	public Value invokeVirtual(Value[] locals, Object name, Object desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val result = vm.getHelper().invokeVirtual((String) name, (String) desc, new Value[0], locals);
		return result.getResult();
	}

	public Value invokeInterface(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		val result = helper.invokeInterface(klass, name, desc, new Value[0], locals);
		return result.getResult();
	}

	public void invokeInterface(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = 1;
		for (val arg : args) {
			localsLength += arg.getSize();
		}
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.pop();
		}
		val result = helper.invokeInterface(klass, name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public Value allocateInstance(Object type, ExecutionContext ctx) {
		val vm = ctx.getVM();
		// TODO checks like in UnsafeNatives
		val klass = (InstanceJavaClass) type;
		klass.initialize();
		val instance = vm.getMemoryManager().newInstance(klass);
		vm.getHelper().initializeDefaultValues(instance);
		return instance;
	}

	public Value allocateInstance(String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val type = helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		// TODO checks like in UnsafeNatives
		val instance = vm.getMemoryManager().newInstance((InstanceJavaClass) type);
		helper.initializeDefaultValues(instance);
		return instance;
	}

	public Value allocatePrimitiveArray(int length, int operand, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val vm = ctx.getVM();
		vm.getHelper().checkArrayLength(length);
		val primitives = vm.getPrimitives();
		val helper = vm.getHelper();
		ArrayValue array;
		switch (operand) {
			case T_LONG:
				array = helper.newArray(primitives.longPrimitive, length);
				break;
			case T_DOUBLE:
				array = helper.newArray(primitives.doublePrimitive, length);
				break;
			case T_INT:
				array = helper.newArray(primitives.intPrimitive, length);
				break;
			case T_FLOAT:
				array = helper.newArray(primitives.floatPrimitive, length);
				break;
			case T_CHAR:
				array = helper.newArray(primitives.charPrimitive, length);
				break;
			case T_SHORT:
				array = helper.newArray(primitives.shortPrimitive, length);
				break;
			case T_BYTE:
				array = helper.newArray(primitives.bytePrimitive, length);
				break;
			case T_BOOLEAN:
				array = helper.newArray(primitives.booleanPrimitive, length);
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
		val stack = ctx.getStack();
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray((JavaClass) klass, length);
	}

	public Value allocateValueArray(int length, String desc, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		return allocateValueArray(length, helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, false), ctx);
	}

	public Value allocateValueArray(String desc, ExecutionContext ctx) {
		return allocateValueArray(ctx.getStack().pop().asInt(), desc, ctx);
	}

	public int getArrayLength(Value value, ExecutionContext ctx) {
		val array = ctx.getHelper().checkNotNullArray((ObjectValue) value);
		return array.getLength();
	}

	public int getArrayLength(ExecutionContext ctx) {
		return getArrayLength(ctx.getStack().pop(), ctx);
	}

	public void throwException(Value exception, ExecutionContext ctx) {
		if (exception.isNull()) {
			// NPE it is then.
			val vm = ctx.getVM();
			val exceptionClass = vm.getSymbols().java_lang_NullPointerException;
			exceptionClass.initialize();
			exception = vm.getMemoryManager().newInstance(exceptionClass);
			vm.getHelper().invokeExact(exceptionClass, "<init>", "()V", new Value[0], new Value[]{exception});
		}
		throw new VMException((InstanceValue) exception);
	}

	public void throwException(ExecutionContext ctx) {
		throwException(ctx.getStack().pop(), ctx);
	}

	public Value checkCast(Value value, Object type, ExecutionContext ctx) {
		val vm = ctx.getVM();
		if (!value.isNull()) {
			val against = ((ObjectValue) value).getJavaClass();
			val jc = (JavaClass) type;
			if (!jc.isAssignableFrom(against)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_ClassCastException, against.getName() + " cannot be cast to " + jc.getName());
			}
		}
		return value;
	}

	public Value checkCast(Value value, String desc, ExecutionContext ctx) {
		val type = ctx.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		return checkCast(value, type, ctx);
	}

	public void checkCast(String desc, ExecutionContext ctx) {
		checkCast(ctx.getStack().peek(), desc, ctx);
	}

	public boolean instanceofResult(Value value, Object javaClass, ExecutionContext ctx) {
		val vm = ctx.getVM();
		if (javaClass instanceof InstanceJavaClass) ((InstanceJavaClass) javaClass).loadNoResolve();
		if (value.isNull()) {
			return false;
		} else {
			return ((JavaClass) javaClass).isAssignableFrom(((ObjectValue) value).getJavaClass());
		}
	}

	public boolean instanceofResult(Value value, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val javaClass = vm.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), desc, false);
		return instanceofResult(value, javaClass, ctx);
	}

	public void instanceofResult(String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val value = stack.<ObjectValue>pop();
		stack.push(instanceofResult(value, desc, ctx) ? IntValue.ONE : IntValue.ZERO);
	}

	public void monitorEnter(Value value, ExecutionContext ctx) {
		if (value.isNull()) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NullPointerException);
		} else {
			((ObjectValue) value).monitorEnter();
		}
	}

	public void monitorEnter(ExecutionContext ctx) {
		monitorEnter(ctx.getStack().pop(), ctx);
	}

	public void monitorExit(Value value, ExecutionContext ctx) {
		if (value.isNull()) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NullPointerException);
		}
		try {
			((ObjectValue) value).monitorExit();
		} catch (IllegalMonitorStateException ex) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException);
		}
	}

	public void monitorExit(ExecutionContext ctx) {
		monitorExit(ctx.getStack().pop(), ctx);
	}

	public Value multiNewArray(String desc, int dimensions, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val type = helper.tryFindClass(ctx.getOwner().getClassLoader(), desc, true);
		val stack = ctx.getStack();
		val lengths = new int[dimensions];
		while (dimensions-- != 0) lengths[dimensions] = stack.pop().asInt();
		return helper.newMultiArray((ArrayJavaClass) type, lengths);
	}

	public Value classLdc(String desc, ExecutionContext ctx) {
		return ctx.getHelper().valueFromLdc(Type.getObjectType(desc));
	}

	public Value methodLdc(String desc, ExecutionContext ctx) {
		return ctx.getHelper().valueFromLdc(Type.getMethodType(desc));
	}

	public void intToByte(ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v = stack.peek();
		val b = v.asByte();
		if (v.asInt() != b) {
			stack.pop();
			stack.push(IntValue.of(b));
		}
	}

	public void intToChar(ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v = stack.peek();
		val c = v.asChar();
		if (v.asInt() != c) {
			stack.pop();
			stack.push(IntValue.of(c));
		}
	}

	public void intToShort(ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v = stack.peek();
		val s = v.asShort();
		if (v.asInt() != s) {
			stack.pop();
			stack.push(IntValue.of(s));
		}
	}

	public VMException exceptionCaught(VMException ex, Object $classes, ExecutionContext ctx) {
		val exceptionType = ex.getOop().getJavaClass();
		val loader = ctx.getOwner().getClassLoader();
		val helper = ctx.getHelper();
		val classes = (Object[]) $classes;
		for (int i = 0, j = classes.length; i < j; i++) {
			val $type = classes[0];
			InstanceJavaClass type;
			if ($type instanceof InstanceJavaClass) {
				type = (InstanceJavaClass) $type;
			} else {
				type = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), (String) $type, true);
				classes[i] = type;
			}
			if (type.isAssignableFrom(exceptionType))
				return ex;
		}
		throw ex;
	}

	public Value invokeDynamic(Value[] args, Object constants, int index, ExecutionContext ctx) {
		val arr = (Object[]) constants;
		val operand = arr[index];
		DynamicLinkResult result;
		if (operand instanceof InvokeDynamicInsnNode) {
			val insn = (InvokeDynamicInsnNode) operand;
			val linked= InvokeDynamicLinker.linkCall(insn, ctx);
			result = new DynamicLinkResult(linked, insn.desc);
			arr[index] = result;
		} else {
			result = (DynamicLinkResult) operand;
		}
		return InvokeDynamicLinker.dynamicCall(args, result.desc, result.handle, ctx);
	}
	
	private static JavaMethod resolveStaticMethod(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		val mn = klass.getStaticMethodRecursively(name, desc);
		if (mn == null) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchMethodError, owner + '.' + name + desc);
		}
		return mn;
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class DynamicLinkResult {
		final InstanceValue handle;
		final String desc;
	}
}
