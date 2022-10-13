package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * JIT functions, used to avoid
 * excessive code generation.
 *
 * @author xDark
 */
@SuppressWarnings("unused")
@Deprecated
@UtilityClass
public class JitFunctions {

	public ObjectValue loadNull(ExecutionContext<?> ctx) {
		return ctx.getMemoryManager().nullValue();
	}

	/*  Monitor enter/exit */
	public void monitorEnter(ObjectValue value, ExecutionContext<?> ctx) {
		ctx.monitorEnter(value);
	}

	public void monitorExit(ObjectValue value, ExecutionContext<?> ctx) {
		ctx.monitorExit(value);
	}

	/* debug */
	public void setLineNumber(int line, ExecutionContext<?> ctx) {
		ctx.setLineNumber(line);
	}

	/* array load/store */
	public long loadLong(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadLong(array, index);
	}

	public double loadDouble(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadDouble(array, index);
	}

	public int loadInt(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadInt(array, index);
	}

	public float loadFloat(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadFloat(array, index);
	}

	public char loadChar(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadChar(array, index);
	}

	public short loadShort(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadShort(array, index);
	}

	public byte loadByte(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadByte(array, index);
	}

	public ObjectValue loadReference(ObjectValue array, int index, ExecutionContext<?> ctx) {
		return ctx.getOperations().arrayLoadReference(array, index);
	}

	public void storeLong(ObjectValue array, int index, long value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreLong(array, index, value);
	}

	public void storeDouble(ObjectValue array, int index, double value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreDouble(array, index, value);
	}

	public void storeInt(ObjectValue array, int index, int value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreInt(array, index, value);
	}

	public void storeFloat(ObjectValue array, int index, float value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreFloat(array, index, value);
	}

	public void storeChar(ObjectValue array, int index, char value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreChar(array, index, value);
	}

	public void storeShort(ObjectValue array, int index, short value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreShort(array, index, value);
	}

	public void storeByte(ObjectValue array, int index, byte value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreByte(array, index, value);
	}

	public void storeReference(ObjectValue array, int index, ObjectValue value, ExecutionContext<?> ctx) {
		ctx.getOperations().arrayStoreReference(array, index, value);
	}

	/* exception handling */
	public ObjectValue exceptionCaught(VMException ex, Object /* ExceptionInfo */ rawInfo) {
		ExceptionInfo info = (ExceptionInfo) rawInfo;
		if (ex == null) {
			return info.nullConstant;
		}
		InstanceValue oop = ex.getOop();
		InstanceClass type = oop.getJavaClass();
		for (InstanceClass candidate : info.types) {
			if (candidate.isAssignableFrom(type)) {
				return oop;
			}
		}
		throw ex;
	}

	public void throwException(ObjectValue ex, ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ex);
	}

	/* ldc */
	public InstanceValue makeMethodType(Object /* Type */ type, ExecutionContext<?> ctx) {
		return ctx.getHelper().methodType(ctx.getClassLoader(), (Type) type);
	}

	public InstanceValue loadClass(Object /* String */ type, ExecutionContext<?> ctx) {
		return ctx.getVM().findClass(ctx.getClassLoader(), (String) type, false).getOop();
	}

	public InstanceValue makeMethodHandle(Object /* Handle */ handle, ExecutionContext<?> ctx) {
		return ctx.getHelper().linkMethodHandleConstant(ctx.getOwner(), (Handle) handle);
	}

	/* allocation */
	public InstanceValue newInstance(Object /* InstanceJavaClass */ type, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateInstance((InstanceClass) type);
	}

	public ArrayValue newReferenceArray(int length, Object /* JavaClass */ type, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateArray((JavaClass) type, length);
	}

	public ArrayValue newLongArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateLongArray(length);
	}

	public ArrayValue newDoubleArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateDoubleArray(length);
	}

	public ArrayValue newIntArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateIntArray(length);
	}

	public ArrayValue newFloatArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateFloatArray(length);
	}

	public ArrayValue newCharArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateCharArray(length);
	}

	public ArrayValue newShortArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateShortArray(length);
	}

	public ArrayValue newByteArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateByteArray(length);
	}

	public ArrayValue newBooleanArray(int length, ExecutionContext<?> ctx) {
		return ctx.getOperations().allocateBooleanArray(length);
	}

	public int getLength(ObjectValue value, ExecutionContext<?> ctx) {
		return ctx.getOperations().getArrayLength(value);
	}

	/* checkcast */
	public ObjectValue checkCast(ObjectValue value, Object /* JavaClass */ type, ExecutionContext<?> ctx) {
		return ctx.getOperations().checkCast(value, (JavaClass) type);
	}

	/* invocation */
	public Locals newLocals(int size, ExecutionContext<?> ctx) {
		return ctx.getThreadStorage().newLocals(size);
	}

	public ObjectValue invokeReference(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		return ctx.getHelper().invokeReference(method, locals);
	}

	public long invokeLong(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		return ctx.getHelper().invokeLong(method, locals);
	}

	public double invokeDouble(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		return ctx.getHelper().invokeDouble(method, locals);
	}

	public int invokeInt(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		return ctx.getHelper().invokeInt(method, locals);
	}

	public float invokeFloat(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		return ctx.getHelper().invokeFloat(method, locals);
	}

	public void invokeVoid(JavaMethod method, Locals locals, ExecutionContext<?> ctx) {
		ctx.getHelper().invoke(method, locals);
	}

	public JavaMethod resolveVirtualCall(ObjectValue ref, Object /* MethodResolution */ resolution, ExecutionContext<?> ctx) {
		MethodResolution mr = (MethodResolution) resolution;
		return ctx.getLinkResolver().resolveVirtualMethod(ctx.getHelper().checkNotNull(ref).getJavaClass(), mr.klass, mr.name, mr.desc);
	}

	/* null check */
	public void checkNotNull(ObjectValue value, ExecutionContext<?> ctx) {
		ctx.getHelper().checkNotNull(value);
	}

	/* field support */
	public void putReference(ObjectValue instance, long offset, ObjectValue value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		ctx.getMemoryManager().writeValue(instance, offset, value);
	}

	public void putLong(ObjectValue instance, long offset, long value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		instance.getMemory().getData().writeLong(offset, value);
	}

	public void putDouble(ObjectValue instance, long offset, double value, ExecutionContext<?> ctx) {
		putLong(instance, offset, Double.doubleToRawLongBits(value), ctx);
	}

	public void putInt(ObjectValue instance, long offset, int value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		instance.getMemory().getData().writeInt(offset, value);
	}

	public void putFloat(ObjectValue instance, long offset, float value, ExecutionContext<?> ctx) {
		putInt(instance, offset, Float.floatToRawIntBits(value), ctx);
	}

	public void putChar(ObjectValue instance, long offset, char value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		instance.getMemory().getData().writeChar(offset, value);
	}

	public void putShort(ObjectValue instance, long offset, short value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		instance.getMemory().getData().writeShort(offset, value);
	}

	public void putByte(ObjectValue instance, long offset, byte value, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		instance.getMemory().getData().writeByte(offset, value);
	}
	public ObjectValue getReference(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return ctx.getMemoryManager().readReference(instance, offset);
	}

	public long getLong(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return instance.getMemory().getData().readLong(offset);
	}

	public double getDouble(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		return Double.longBitsToDouble(getLong(instance, offset, ctx));
	}

	public int getInt(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return instance.getMemory().getData().readInt(offset);
	}

	public float getFloat(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		return Float.intBitsToFloat(getInt(instance, offset, ctx));
	}

	public char getChar(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return instance.getMemory().getData().readChar(offset);
	}

	public short getShort(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return instance.getMemory().getData().readShort(offset);
	}

	public byte getByte(ObjectValue instance, long offset, ExecutionContext<?> ctx) {
		checkNotNull(instance, ctx);
		return instance.getMemory().getData().readByte(offset);
	}
}
