package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * JIT helper.
 *
 * @author xDark
 */
@SuppressWarnings("unused")
@UtilityClass
public class JitHelper {

	public Value arrayLoadInt(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return IntValue.of(arr.getInt(idx));
	}

	public Value arrayLoadLong(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return LongValue.of(arr.getLong(idx));
	}

	public Value arrayLoadFloat(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return new FloatValue(arr.getFloat(idx));
	}

	public Value arrayLoadDouble(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return new DoubleValue(arr.getDouble(idx));
	}

	public Value arrayLoadChar(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return IntValue.of(arr.getChar(idx));
	}

	public Value arrayLoadShort(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return IntValue.of(arr.getShort(idx));
	}

	public Value arrayLoadByte(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return IntValue.of(arr.getByte(idx));
	}

	public Value arrayLoadValue(Value array, Value index, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		return arr.getValue(idx);
	}

	public void arrayStoreLong(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setLong(idx, value.asLong());
	}

	public void arrayStoreDouble(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setDouble(idx, value.asDouble());
	}

	public void arrayStoreFloat(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setFloat(idx, value.asFloat());
	}

	public void arrayStoreInt(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setInt(idx, value.asInt());
	}

	public void arrayStoreChar(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setChar(idx, value.asChar());
	}

	public void arrayStoreShort(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setShort(idx, value.asShort());
	}

	public void arrayStoreByte(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);
		arr.setByte(idx, value.asByte());
	}

	public void arrayStoreValue(Value array, Value index, Value value, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val arr = helper.checkNotNullArray((ObjectValue) array);
		int idx = index.asInt();
		helper.rangeCheck(arr, idx);

		val o = (ObjectValue) value;
		if (!value.isNull()) {
			val type = arr.getJavaClass().getComponentType();
			val valueType = o.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				val symbols = ctx.getVM().getSymbols();
				helper.throwException(symbols.java_lang_ArrayStoreException, valueType.getName());
			}
		}

		arr.setValue(idx, (ObjectValue) value);
	}

	public Value addInt(Value a, Value b) {
		return IntValue.of(a.asInt() + b.asInt());
	}

	public Value addLong(Value a, Value b) {
		return LongValue.of(a.asLong() + b.asLong());
	}

	public Value addFloat(Value a, Value b) {
		return new FloatValue(a.asFloat() + b.asFloat());
	}

	public Value addDouble(Value a, Value b) {
		return new DoubleValue(a.asDouble() + b.asDouble());
	}

	public Value subInt(Value a, Value b) {
		return IntValue.of(a.asInt() - b.asInt());
	}

	public Value subLong(Value a, Value b) {
		return LongValue.of(a.asLong() - b.asLong());
	}

	public Value subFloat(Value a, Value b) {
		return new FloatValue(a.asFloat() - b.asFloat());
	}

	public Value subDouble(Value a, Value b) {
		return new DoubleValue(a.asDouble() - b.asDouble());
	}

	public Value mulInt(Value a, Value b) {
		return IntValue.of(a.asInt() * b.asInt());
	}

	public Value mulLong(Value a, Value b) {
		return LongValue.of(a.asLong() * b.asLong());
	}

	public Value mulFloat(Value a, Value b) {
		return new FloatValue(a.asFloat() * b.asFloat());
	}

	public Value mulDouble(Value a, Value b) {
		return new DoubleValue(a.asDouble() * b.asDouble());
	}

	public Value divInt(Value a, Value b) {
		return IntValue.of(a.asInt() / b.asInt());
	}

	public Value divLong(Value a, Value b) {
		return LongValue.of(a.asLong() / b.asLong());
	}

	public Value divFloat(Value a, Value b) {
		return new FloatValue(a.asFloat() / b.asFloat());
	}

	public Value divDouble(Value a, Value b) {
		return new DoubleValue(a.asDouble() / b.asDouble());
	}

	public Value remInt(Value a, Value b) {
		return IntValue.of(a.asInt() % b.asInt());
	}

	public Value remLong(Value a, Value b) {
		return LongValue.of(a.asLong() % b.asLong());
	}

	public Value remFloat(Value a, Value b) {
		return new FloatValue(a.asFloat() % b.asFloat());
	}

	public Value remDouble(Value a, Value b) {
		return new DoubleValue(a.asDouble() % b.asDouble());
	}

	public Value shlInt(Value a, Value b) {
		return IntValue.of(a.asInt() << b.asInt());
	}

	public Value shlLong(Value a, Value b) {
		return LongValue.of(a.asLong() << b.asInt());
	}

	public Value shrInt(Value a, Value b) {
		return IntValue.of(a.asInt() >> b.asInt());
	}

	public Value shrLong(Value a, Value b) {
		return LongValue.of(a.asLong() >> b.asInt());
	}

	public Value ushrInt(Value a, Value b) {
		return IntValue.of(a.asInt() >>> b.asInt());
	}

	public Value ushrLong(Value a, Value b) {
		return LongValue.of(a.asLong() >>> b.asInt());
	}

	public Value andInt(Value a, Value b) {
		return IntValue.of(a.asInt() & b.asInt());
	}

	public Value andLong(Value a, Value b) {
		return LongValue.of(a.asLong() & b.asLong());
	}

	public Value orInt(Value a, Value b) {
		return IntValue.of(a.asInt() | b.asInt());
	}

	public Value orLong(Value a, Value b) {
		return LongValue.of(a.asLong() | b.asLong());
	}

	public Value xorInt(Value a, Value b) {
		return IntValue.of(a.asInt() ^ b.asInt());
	}

	public Value xorLong(Value a, Value b) {
		return LongValue.of(a.asLong() ^ b.asLong());
	}

	public void localIncrement(Locals locals, int idx, int value) {
		locals.set(idx, IntValue.of(locals.load(idx).asInt() + value));
	}

	public Value compareLong(Value a, Value b) {
		return IntValue.of(Long.compare(a.asLong(), b.asLong()));
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

	public void getStatic(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		while (klass != null) {
			val value = klass.getStaticValue(name, desc);
			if (value != null) {
				ctx.getStack().pushGeneric(value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
	}

	// special intrinsic versions.
	public void getStaticIntrinsicFail(Object owner, Object field, long offset, ExecutionContext ctx) {
		if (owner instanceof String) {
			// Class was not found
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoClassDefFoundError, (String) owner);
		}
		if (offset == -1L) {
			// Field was not found.
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_NoSuchFieldError, (String) field);
		}
	}

	public void getStaticIntrinsicJ(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().pushWide(LongValue.of(memoryManager.readLong(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicD(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().pushWide(new DoubleValue(memoryManager.readDouble(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicI(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(IntValue.of(memoryManager.readInt(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicF(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(new FloatValue(memoryManager.readFloat(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicC(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(IntValue.of(memoryManager.readChar(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicS(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(IntValue.of(memoryManager.readShort(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicB(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(IntValue.of(memoryManager.readByte(((InstanceJavaClass) owner).getOop(), offset)));
	}

	public void getStaticIntrinsicA(Object owner, long offset, ExecutionContext ctx) {
		val memoryManager = ctx.getVM().getMemoryManager();
		ctx.getStack().push(memoryManager.readValue(((InstanceJavaClass) owner).getOop(), offset));
	}

	public void putStatic(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		val value = ctx.getStack().popGeneric();
		if (!klass.setFieldValue(name, desc, value)) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		}
	}

	public void getField(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		if (klass == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, owner);
		}
		val stack = ctx.getStack();
		val instance = helper.<InstanceValue>checkNotNull(stack.pop());
		long offset = helper.getFieldOffset((InstanceJavaClass) klass, instance.getJavaClass(), name, desc);
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
		stack.pushGeneric(value);
	}

	public void putField(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		if (klass == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, owner);
		}
		val stack = ctx.getStack();
		val value = stack.popGeneric();
		val instance = helper.<InstanceValue>checkNotNull(stack.pop());
		long offset = helper.getFieldOffset((InstanceJavaClass) klass, instance.getJavaClass(), name, desc);
		if (offset == -1L) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, name);
		}
		offset += vm.getMemoryManager().valueBaseOffset(instance);
		val manager = vm.getMemoryManager();
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
				break;
		}
	}

	public void invokeVirtual(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = vm.getHelper().invokeVirtual(name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeSpecial(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeExact(klass, name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeStatic(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		val mn = klass.getStaticMethodRecursively(name, desc);
		if (mn == null) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchMethodError, owner + '.' + name + desc);
		}
		val stack = ctx.getStack();
		val args = mn.getArgumentTypes();
		int localsLength = args.length;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeStatic(klass, mn, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	// special intrinsic version.
	public void invokeFail(Object owner, Object method, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val symbols = ctx.getSymbols();
		if (owner instanceof String) {
			// Class was not found
			helper.throwException(symbols.java_lang_NoClassDefFoundError, (String) owner);
		}
		ctx.getHelper().throwException(symbols.java_lang_NoSuchMethodError, (String) method);
	}

	public void invokeStaticIntrinsic(Object owner, Object method, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val mn = (JavaMethod) method;
		val stack = ctx.getStack();
		val args = mn.getArgumentTypes();
		int localsLength = args.length;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeStatic((InstanceJavaClass) owner, mn, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeSpecialIntrinsic(Object owner, Object method, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val stack = ctx.getStack();
		val mn = (JavaMethod) method;
		val args = mn.getArgumentTypes();
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeExact((InstanceJavaClass) owner, mn, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeVirtualIntrinsic(Object name, Object desc, Object args, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val stack = ctx.getStack();
		val arr = (Type[]) args;
		int localsLength = arr.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = vm.getHelper().invokeVirtual((String) name, (String) desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void invokeInterface(String owner, String name, String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), owner, true);
		val stack = ctx.getStack();
		val args = Type.getArgumentTypes(desc);
		int localsLength = args.length + 1;
		val locals = new Value[localsLength];
		while (localsLength-- != 0) {
			locals[localsLength] = stack.popGeneric();
		}
		val result = helper.invokeInterface(klass, name, desc, new Value[0], locals);
		val v = result.getResult();
		if (!v.isVoid()) {
			stack.pushGeneric(v);
		}
	}

	public void allocateInstance(String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val type = helper.findClass(ctx.getOwner().getClassLoader(), desc, true);
		// TODO checks like in UnsafeNatives
		val instance = vm.getMemoryManager().newInstance((InstanceJavaClass) type);
		helper.initializeDefaultValues(instance);
		ctx.getStack().push(instance);
	}

	public void allocatePrimitiveArray(int operand, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int length = stack.pop().asInt();
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
		stack.push(array);
	}

	public void allocateValueArray(String desc, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val length = stack.pop().asInt();
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = helper.findClass(ctx.getOwner().getClassLoader(), desc, false);
		helper.checkArrayLength(length);
		stack.push(helper.newArray(klass, length));
	}

	public void getArrayLength(ExecutionContext ctx) {
		val stack = ctx.getStack();
		val array = ctx.getHelper().checkNotNullArray(stack.pop());
		stack.push(IntValue.of(array.getLength()));
	}

	public void throwException(ExecutionContext ctx) {
		ObjectValue exception = ctx.getStack().pop();
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

	public void checkCast(String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val type = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), desc, true);
		val value = ctx.getStack().<ObjectValue>peek();
		if (!value.isNull()) {
			val against = value.getJavaClass();
			if (!type.isAssignableFrom(against)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_ClassCastException, against.getName() + " cannot be cast to " + type.getName());
			}
		}
	}

	public void instanceofResult(String desc, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val javaClass = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), desc, false);
		if (javaClass instanceof InstanceJavaClass) ((InstanceJavaClass) javaClass).loadClassesWithoutMarkingResolved();
		val stack = ctx.getStack();
		val value = stack.<ObjectValue>pop();
		if (value.isNull()) {
			stack.push(IntValue.ZERO);
		} else {
			stack.push(javaClass.isAssignableFrom(value.getJavaClass()) ? IntValue.ONE : IntValue.ZERO);
		}
	}

	public void monitorEnter(ExecutionContext ctx) {
		ctx.getStack().<ObjectValue>pop().monitorEnter();
	}

	public void monitorExit(ExecutionContext ctx) {
		try {
			ctx.getStack().<ObjectValue>pop().monitorExit();
		} catch (IllegalMonitorStateException ex) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalMonitorStateException);
		}
	}

	public void multiNewArray(String desc, int dimensions, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val type = helper.findClass(ctx.getOwner().getClassLoader(), desc, true);
		val stack = ctx.getStack();
		val lengths = new int[dimensions];
		while (dimensions-- != 0) lengths[dimensions] = stack.pop().asInt();
		val array = helper.newMultiArray((ArrayJavaClass) type, lengths);
		stack.push(array);
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

	public void exceptionCaught(String type, Value ex, ExecutionContext ctx) {

	}
}
