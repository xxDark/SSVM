package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * JIT helper.
 *
 * @deprecated For use by generated classes.
 *
 * @author xDark
 */
@Deprecated
@SuppressWarnings("unused")
@UtilityClass
public class JitHelper {

	public int arrayLoadInt(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadInt((ObjectValue) array, index);
	}

	public long arrayLoadLong(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadLong((ObjectValue) array, index);
	}

	public float arrayLoadFloat(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadFloat((ObjectValue) array, index);
	}

	public double arrayLoadDouble(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadDouble((ObjectValue) array, index);
	}

	public char arrayLoadChar(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadChar((ObjectValue) array, index);
	}

	public short arrayLoadShort(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadShort((ObjectValue) array, index);
	}

	public byte arrayLoadByte(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadByte((ObjectValue) array, index);

	}

	public Value arrayLoadValue(Value array, int index, ExecutionContext ctx) {
		return ctx.getOperations().arrayLoadReference((ObjectValue) array, index);
	}

	public void arrayStoreLong(Value array, int index, long value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreLong((ObjectValue) array, index, value);
	}

	public void arrayStoreDouble(Value array, int index, double value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreDouble((ObjectValue) array, index, value);
	}

	public void arrayStoreFloat(Value array, int index, float value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreFloat((ObjectValue) array, index, value);
	}

	public void arrayStoreInt(Value array, int index, int value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreInt((ObjectValue) array, index, value);
	}

	public void arrayStoreChar(Value array, int index, char value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreChar((ObjectValue) array, index, value);
	}

	public void arrayStoreShort(Value array, int index, short value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreShort((ObjectValue) array, index, value);
	}

	public void arrayStoreByte(Value array, int index, byte value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreByte((ObjectValue) array, index, value);
	}

	public void arrayStoreValue(Value array, int index, Value value, ExecutionContext ctx) {
		ctx.getOperations().arrayStoreReference((ObjectValue) array, index, (ObjectValue) value);
	}

	public Value getStaticA(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		return vm.getPublicOperations().getReference(klass, name, desc);
	}

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
		return getClassOop(owner).getData().readLong(offset);
	}

	public double getStaticD(Object owner, long offset, ExecutionContext ctx) {
		return Double.longBitsToDouble(getClassOop(owner).getData().readLong(offset));
	}

	public int getStaticI(Object owner, long offset, ExecutionContext ctx) {
		return getClassOop(owner).getData().readInt(offset);
	}

	public float getStaticF(Object owner, long offset, ExecutionContext ctx) {
		return Float.intBitsToFloat(getClassOop(owner).getData().readInt(offset));
	}

	public char getStaticC(Object owner, long offset, ExecutionContext ctx) {
		return getClassOop(owner).getData().readChar(offset);
	}

	public short getStaticS(Object owner, long offset, ExecutionContext ctx) {
		return getClassOop(owner).getData().readShort(offset);
	}

	public byte getStaticB(Object owner, long offset, ExecutionContext ctx) {
		return getClassOop(owner).getData().readByte(offset);
	}

	public Value getStaticA(Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		return memoryManager.readValue(getClassOop(owner), offset);
	}

	public void getStaticA(Value value, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		vm.getPublicOperations().putGeneric(klass, name, desc, value);
	}

	public void putStaticA(Value value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		memoryManager.writeValue(getClassOop(owner), offset, (ObjectValue) value);
	}

	public void putStaticJ(long value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeLong(offset, value);
	}

	public void putStaticD(double value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeLong(offset, Double.doubleToRawLongBits(value));
	}

	public void putStaticI(int value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeInt(offset, value);
	}

	public void putStaticF(float value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeInt(offset, Float.floatToRawIntBits(value));
	}

	public void putStaticS(short value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeShort(offset, value);
	}

	public void putStaticC(char value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeChar(offset, value);
	}

	public void putStaticB(byte value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeByte(offset, value);
	}

	public void putStaticZ(boolean value, Object owner, long offset, ExecutionContext ctx) {
		MemoryManager memoryManager = ctx.getVM().getMemoryManager();
		getClassOop(owner).getData().writeByte(offset, (byte) (value ? 1 : 0));
	}

	public Value getFieldA(Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getReference((ObjectValue) value, klass, name, desc);
	}

	public long getFieldJ(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getLong((ObjectValue) value, klass, name);
	}

	public double getFieldD(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getDouble((ObjectValue) value, klass, name);
	}

	public int getFieldI(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getInt((ObjectValue) value, klass, name);
	}

	public float getFieldF(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getFloat((ObjectValue) value, klass, name);
	}

	public char getFieldC(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getChar((ObjectValue) value, klass, name);
	}

	public short getFieldS(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getShort((ObjectValue) value, klass, name);
	}

	public byte getFieldB(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getByte((ObjectValue) value, klass, name);
	}

	public boolean getFieldZ(Value value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		return ctx.getOperations().getBoolean((ObjectValue) value, klass, name);
	}

	public Value getFieldA(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return ctx.getVM().getMemoryManager().readValue(o, offset);
	}

	public long getFieldJ(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readLong(offset);
	}

	public double getFieldD(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return Double.longBitsToDouble(o.getData().readLong(offset));
	}

	public int getFieldI(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readInt(offset);
	}

	public float getFieldF(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return Float.intBitsToFloat(o.getData().readInt(offset));
	}

	public char getFieldC(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readChar(offset);
	}

	public short getFieldS(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readShort(offset);
	}

	public byte getFieldB(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readByte(offset);
	}

	public boolean getFieldZ(Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(value, ctx);
		return o.getData().readByte(offset) != 0;
	}

	public void putFieldA(Value instance, Value value, Object owner, String name, String desc, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putReference((ObjectValue) instance, klass, name, desc, (ObjectValue) value);
	}

	public void putFieldJ(Value instance, long value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putLong((ObjectValue) instance, klass, name, value);
	}

	public void putFieldD(Value instance, double value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putDouble((ObjectValue) instance, klass, name, value);
	}

	public void putFieldI(Value instance, int value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putInt((ObjectValue) instance, klass, name, value);
	}

	public void putFieldF(Value instance, float value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putFloat((ObjectValue) instance, klass, name, value);
	}

	public void putFieldC(Value instance, char value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putChar((ObjectValue) instance, klass, name, value);
	}

	public void putFieldS(Value instance, short value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putShort((ObjectValue) instance, klass, name, value);
	}

	public void putFieldB(Value instance, byte value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putByte((ObjectValue) instance, klass, name, value);
	}

	public void putFieldZ(Value instance, boolean value, Object owner, String name, ExecutionContext ctx) {
		InstanceJavaClass klass = getOrFindInstanceClass(owner, ctx);
		ctx.getOperations().putBoolean((ObjectValue) instance, klass, name, value);
	}

	public void putFieldA(Value instance, Value value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		ctx.getVM().getMemoryManager().writeValue(o, offset, (ObjectValue) value);
	}

	public void putFieldJ(Value instance, long value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeLong(offset, value);
	}

	public void putFieldD(Value instance, double value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeLong(offset, Double.doubleToRawLongBits(value));
	}

	public void putFieldI(Value instance, int value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeInt(offset, value);
	}

	public void putFieldF(Value instance, float value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeInt(offset, Float.floatToRawIntBits(value));
	}

	public void putFieldC(Value instance, char value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeChar(offset, value);
	}

	public void putFieldS(Value instance, short value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeShort(offset, value);
	}

	public void putFieldB(Value instance, byte value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeByte(offset, value);
	}

	public void putFieldZ(Value instance, boolean value, long offset, ExecutionContext ctx) {
		ObjectValue o = nonNull(instance, ctx);
		o.getData().writeByte(offset, (byte) (value ? 1 : 0));
	}

	public Value invokeSpecial(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		//ExecutionContext result = helper.invokeExact(klass, name, desc, locals);
		//return result.getResult();
		throw new PanicException("Unimplemented");
	}

	public Value invokeStatic(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		/*
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		JavaMethod mn = resolveStaticMethod(owner, name, desc, ctx);
		Locals table = vm.getThreadStorage().newLocals(mn);
		table.copyFrom(locals, 0, 0, locals.length);
		ExecutionContext result = helper.invoke(mn, table);
		return result.getResult();
		*/
		throw new PanicException("Unimplemented");
	}

	public Value invokeVirtual(Value[] locals, String name, String desc, ExecutionContext ctx) {
		/*
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(helper.checkNotNull(locals[0]), name, desc);
		Locals table = vm.getThreadStorage().newLocals(method);
		table.copyFrom(locals, 0, 0, locals.length);
		ExecutionContext result = helper.invoke(method, table);
		return result.getResult();
		*/
		throw new PanicException("Unimplemented");
	}

	public Value invokeInterface(Value[] locals, String owner, String name, String desc, ExecutionContext ctx) {
		/*
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		JavaMethod method = vm.getPublicLinkResolver().resolveVirtualMethod(helper.checkNotNull(locals[0]).getJavaClass(), klass, name, desc);
		Locals table = vm.getThreadStorage().newLocals(method);
		table.copyFrom(locals, 0, 0, locals.length);
		ExecutionContext result = helper.invoke(method, table);
		return result.getResult();
		*/
		throw new PanicException("Unimplemented");
	}

	public ObjectValue allocateInstance(InstanceJavaClass klass, ExecutionContext ctx) {
		return ctx.getOperations().allocateInstance(klass);
	}

	public Value allocateInstance(Object type, ExecutionContext ctx) {
		return allocateInstance((InstanceJavaClass) type, ctx);
	}

	public Value allocateInstance(String desc, ExecutionContext ctx) {
		return allocateInstance(getOrFindInstanceClass(desc, ctx), ctx);
	}

	public Value allocateLongArray(int length, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		helper.checkArrayLength(length);
		return helper.newArray(vm.getPrimitives().longPrimitive(), length);
	}

	public Value allocateDoubleArray(int length, ExecutionContext ctx) {
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

	public Value allocateValueArray(int length, Object klass, ExecutionContext ctx) {
		return ctx.getOperations().allocateArray((JavaClass) klass, length);
	}

	public Value allocateValueArray(int length, String desc, ExecutionContext ctx) {
		VMHelper helper = ctx.getHelper();
		return allocateValueArray(length, helper.tryFindClass(ctx.getClassLoader(), desc, false), ctx);
	}

	public int getArrayLength(Value value, ExecutionContext ctx) {
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
		JavaClass type;
		if (!(rawDesc instanceof JavaClass)) {
			String desc = AsmUtil.normalizeDescriptor((String) rawDesc);
			type = ctx.getHelper().tryFindClass(ctx.getClassLoader(), desc, true);
		} else {
			type = (JavaClass) rawDesc;
		}
		return ctx.getOperations().checkCast((ObjectValue) value, type);
	}

	public boolean instanceofResult(Value value, Object javaClass, ExecutionContext ctx) {
		return ctx.getOperations().instanceofCheck((ObjectValue) value, (JavaClass) javaClass);
	}

	public boolean instanceofResult(Value value, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		JavaClass javaClass = vm.getHelper().tryFindClass(ctx.getOwner().getClassLoader(), desc, false);
		return instanceofResult(value, javaClass, ctx);
	}

	public void monitorEnter(Value value, ExecutionContext ctx) {
		ctx.monitorEnter((ObjectValue) value);
	}

	public void monitorExit(Value value, ExecutionContext ctx) {
		ctx.monitorExit((ObjectValue) value);
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
		throw new PanicException("Unimplemented");
		/*
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
		*/
	}

	public Value loadNull(ExecutionContext ctx) {
		return ctx.getMemoryManager().nullValue();
	}

	public Value invokeDirect(Locals locals, Object method, ExecutionContext ctx) {
		JavaMethod jm = (JavaMethod) method;
		jm.getOwner().initialize();
		return ctx.getHelper().invoke(jm, locals).getResult();
	}

	public Locals newLocals(int count, ExecutionContext ctx) {
		return ctx.getThreadStorage().newLocals(count);
	}

	public Stack newStack(int count, ExecutionContext ctx) {
		return ctx.getThreadStorage().newStack(count);
	}

	public int divInt(int a, int b, ExecutionContext ctx) {
		if (b == 0) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
		}
		return a / b;
	}

	public int remInt(int a, int b, ExecutionContext ctx) {
		if (b == 0) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
		}
		return a % b;
	}

	public long divLong(long a, long b, ExecutionContext ctx) {
		if (b == 0L) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
		}
		return a / b;
	}

	public long remLong(long a, long b, ExecutionContext ctx) {
		if (b == 0L) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
		}
		return a % b;
	}

	private static JavaMethod resolveStaticMethod(String owner, String name, String desc, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass klass = (InstanceJavaClass) helper.tryFindClass(ctx.getOwner().getClassLoader(), owner, true);
		return vm.getPublicLinkResolver().resolveStaticMethod(klass, name, desc);
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
