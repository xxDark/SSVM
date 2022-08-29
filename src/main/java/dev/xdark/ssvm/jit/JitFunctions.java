package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * @author xDark
 */
@SuppressWarnings("unused")
@Deprecated
@UtilityClass
public class JitFunctions {

	public ObjectValue loadNull(ExecutionContext<?> ctx) {
		return ctx.getMemoryManager().nullValue();
	}

	/*  Monitor enter/exit support */
	public void monitorEnter(ObjectValue value, ExecutionContext<?> ctx) {
		ctx.monitorEnter(value);
	}

	public void monitorExit(ObjectValue value, ExecutionContext<?> ctx) {
		ctx.monitorExit(value);
	}

	/* debug support */
	public void setLineNumber(int line, ExecutionContext<?> ctx) {
		ctx.setLineNumber(line);
	}

	/* array load/store support */
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
		InstanceJavaClass type = oop.getJavaClass();
		for (InstanceJavaClass candidate : info.types) {
			if (candidate.isAssignableFrom(type)) {
				return oop;
			}
		}
		throw ex;
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
		return ctx.getOperations().allocateInstance((InstanceJavaClass) type);
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
}
