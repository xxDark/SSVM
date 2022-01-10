package dev.xdark.ssvm;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.tree.FieldNode;

import java.nio.ByteOrder;

import static org.objectweb.asm.Opcodes.*;

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	public static final String CLASS_LOADER_OOP = "classLoaderOop";

	/**
	 * Sets up VM instance.
	 *
	 * @param vm
	 * 		VM to set up.
	 */
	static void vmInit(VirtualMachine vm) {
		var vmi = vm.getVmInterface();
		injectVMFields(vm);
		setInstructions(vmi);
		var symbols = vm.getSymbols();
		var jlc = symbols.java_lang_Class;
		vmi.setInvoker(jlc, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			var name = vm.getHelper().readUtf8(ctx.getLocals().load(0));
			var primitives = vm.getPrimitives();
			Value result;
			switch (name) {
				case "long":
					result = primitives.longPrimitive.getOop();
					break;
				case "double":
					result = primitives.doublePrimitive.getOop();
					break;
				case "int":
					result = primitives.intPrimitive.getOop();
					break;
				case "float":
					result = primitives.floatPrimitive.getOop();
					break;
				case "char":
					result = primitives.charPrimitive.getOop();
					break;
				case "short":
					result = primitives.shortPrimitive.getOop();
					break;
				case "byte":
					result = primitives.bytePrimitive.getOop();
					break;
				case "boolean":
					result = primitives.booleanPrimitive.getOop();
					break;
				case "void":
					result = primitives.voidPrimitive.getOop();
					break;
				default:
					throw new IllegalStateException(name);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "desiredAssertionStatus0", "(Ljava/lang/Class;)Z", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			var locals = ctx.getLocals();
			var helper = vm.getHelper();
			var name = helper.readUtf8(locals.load(0));
			var initialize = locals.load(1).asBoolean();
			var loader = locals.load(2);
			var klass = vm.findClass(loader, name, initialize);
			if (klass == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, name);
			} else {
				ctx.setResult(klass.getOop());
			}
			return Result.ABORT;
		});
		var classNameInit = (MethodInvoker) ctx -> {
			ctx.setResult(vm.getHelper().newUtf8(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getName()));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(jlc, "getName0", "()Ljava/lang/String;", classNameInit)) {
			if (!vmi.setInvoker(jlc, "initClassName", "()Ljava/lang/String;", classNameInit)) {
				throw new IllegalStateException("Unable to locate Class name init method");
			}
		}
		vmi.setInvoker(jlc, "isArray", "()Z", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().isArray() ? 1 : 0));
			return Result.ABORT;
		});

		var object = symbols.java_lang_Object;
		vmi.setInvoker(object, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(object, "<init>", "()V", ctx -> {
			ctx.getLocals().<InstanceValue>load(0).initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "getClass", "()Ljava/lang/Class;", ctx -> {
			ctx.setResult(ctx.getLocals().<ObjectValue>load(0).getJavaClass().getOop());
			return Result.ABORT;
		});

		var sys = symbols.java_lang_System;
		vmi.setInvoker(sys, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(new LongValue(System.currentTimeMillis()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(new LongValue(System.nanoTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var $src = locals.load(0);
			helper.checkNotNull($src);
			helper.checkArray($src);
			var $dst = locals.load(2);
			helper.checkNotNull($dst);
			helper.checkArray($dst);
			var srcPos = locals.load(1).asInt();
			var dstPos = locals.load(3).asInt();
			var length = locals.load(4).asInt();
			var src = (ArrayValue) $src;
			var dst = (ArrayValue) $dst;
			var memoryManager = vm.getMemoryManager();
			var srcComponent = src.getJavaClass().getComponentType();
			var dstComponent = dst.getJavaClass().getComponentType();

			var srcScale = memoryManager.arrayIndexScale(srcComponent);
			var dstScale = memoryManager.arrayIndexScale(dstComponent);
			if (srcScale != dstScale) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var srcStart = memoryManager.arrayBaseOffset(srcComponent);
			var dstStart = memoryManager.arrayBaseOffset(dstComponent);
			if (srcStart != dstStart) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			for (int i = 0; i < length; i++) {
				switch (srcScale) {
					case 8:
						dst.setLong(dstPos++, src.getLong(srcPos++));
						break;
					case 4:
						dst.setInt(dstPos++, src.getInt(srcPos++));
						break;
					case 2:
						dst.setShort(dstPos++, src.getShort(srcPos++));
						break;
					case 1:
						dst.setByte(dstPos++, src.getByte(srcPos++));
						break;
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			var value = ctx.getLocals().<InstanceValue>load(0);
			var jc = (InstanceJavaClass) value.getJavaClass();
			var mn = jc.getMethod("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			var properties = vm.getProperties();
			var helper = vm.getHelper();

			for (var entry : properties.entrySet()) {
				var key = entry.getKey();
				var property = entry.getValue();
				helper.invokeExact(jc, mn, new Value[0], new Value[]{
						value,
						helper.newUtf8(key.toString()),
						helper.newUtf8(property.toString())
				});
			}
			ctx.setResult(value);
			return Result.ABORT;
		});

		var thread = symbols.java_lang_Thread;
		vmi.setInvoker(thread, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentThread().getOop());
			return Result.ABORT;
		});

		var classLoader = symbols.java_lang_ClassLoader;
		vmi.setInvoker(classLoader, "registerNatives", "()V", ctx -> Result.ABORT);
		var clInitHook = (MethodInvoker) ctx -> {
			var oop = vm.getMemoryManager().newJavaInstance(object, new ClassLoaderData());
			ctx.getLocals().<InstanceValue>load(0)
					.setValue(CLASS_LOADER_OOP, "Ljava/lang/Object", oop);
			return Result.CONTINUE;
		};
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", clInitHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", clInitHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			var locals = ctx.getLocals();
			var loader = locals.<ObjectValue>load(0);
			var name = locals.load(1);
			var b = locals.<ArrayValue>load(2);
			var off = locals.load(3).asInt();
			var length = locals.load(4).asInt();
			var pd = locals.load(5);
			var source = locals.load(6);
			var helper = vm.getHelper();
			var bytes = helper.toJavaBytes(b);
			var defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source));
			//noinspection ConstantConditions
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});

		var throwable = symbols.java_lang_Throwable;
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			var exception = ctx.getLocals().<InstanceValue>load(0);
			var backtrace = vm.getMemoryManager().newJavaInstance(object, vm.currentThread().getBacktrace().copy());
			exception.setValue("backtrace", "Ljava/lang/Object;", backtrace);
			ctx.setResult(exception);
			return Result.ABORT;
		});

		// JDK9+
		var utf16 = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StringUTF16");
		if (utf16 != null) {
			vmi.setInvoker(utf16, "isBigEndian", "()Z", ctx -> {
				ctx.setResult(new IntValue(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0));
				return Result.ABORT;
			});
		}
		var jdkVM = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/VM");
		if (jdkVM != null) {
			vmi.setInvoker(jdkVM, "initialize", "()V", ctx -> Result.ABORT);
			vmi.setInvoker(jdkVM, "initializeFromArchive", "(Ljava/lang/Class;)V", ctx -> Result.ABORT);
		}

		var unsafe = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Unsafe");
		var newUnsafe = unsafe != null;
		if (unsafe == null) {
			unsafe = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Unsafe");
		}
		vmi.setInvoker(unsafe, "registerNatives", "()V", ctx -> Result.ABORT);
		if (newUnsafe) {
			initNewUnsafe(vm, unsafe);
		}
		var runtime = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Runtime");
		vmi.setInvoker(runtime, "availableProcessors", "()I", ctx -> {
			ctx.setResult(new IntValue(Runtime.getRuntime().availableProcessors()));
			return Result.ABORT;
		});

		var doubleClass = symbols.java_lang_Double;
		vmi.setInvoker(doubleClass, "doubleToRawLongBits", "(D)J", ctx -> {
			ctx.setResult(new LongValue(Double.doubleToRawLongBits(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(doubleClass, "longBitsToDouble", "(J)D", ctx -> {
			ctx.setResult(new DoubleValue(Double.longBitsToDouble(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});

		var floatClass = symbols.java_lang_Float;
		vmi.setInvoker(floatClass, "floatToRawIntBits", "(F)I", ctx -> {
			ctx.setResult(new IntValue(Float.floatToRawIntBits(ctx.getLocals().load(0).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(floatClass, "intBitsToFloat", "(I)F", ctx -> {
			ctx.setResult(new FloatValue(Float.intBitsToFloat(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});

		var array = symbols.java_lang_reflect_Array;
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			var value = ctx.getLocals().load(0);
			vm.getHelper().checkArray(value);
			ctx.setResult(new IntValue(((ArrayValue) value).getLength()));
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			var locals = ctx.getLocals();
			var local = locals.load(0);
			var helper = vm.getHelper();
			helper.checkNotNull(local);
			if (!(local instanceof JavaValue)) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var wrapper = ((JavaValue<?>) local).getValue();
			if (!(wrapper instanceof JavaClass)) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var klass = (JavaClass) wrapper;
			if (klass.isArray()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var length = locals.load(1).asInt();
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.newArray(klass.newArrayClass(), length, memoryManager.arrayIndexScale(klass));
			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	/**
	 * Sets up jdk/internal/misc/Unsafe.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param unsafe
	 * 		Unsafe class.
	 */
	private static void initNewUnsafe(VirtualMachine vm, InstanceJavaClass unsafe) {
		var vmi = vm.getVmInterface();
		vmi.setInvoker(unsafe, "allocateMemory0", "(J)J", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var block = memoryManager.allocateDirect(ctx.getLocals().load(1).asLong());
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "reallocateMemory0", "(JJ)J", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var locals = ctx.getLocals();
			var address = locals.load(1).asLong();
			var bytes = locals.load(3).asLong();
			var block = memoryManager.reallocateDirect(address, bytes);
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "freeMemory", "(J)V", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var locals = ctx.getLocals();
			var address = locals.load(1).asLong();
			memoryManager.freeMemory(address);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "setMemory0", "(Ljava/lang/Object;JJB)V", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.load(1);
			if (!(value instanceof ObjectValue)) {
				throw new PanicException("Segfault");
			}
			var memory = ((ObjectValue) value).getMemory().getData();
			var offset = locals.load(2).asLong();
			var bytes = locals.load(4).asLong();
			var b = locals.load(6).asByte();
			for (; offset < bytes; offset++) {
				memory.put((int) offset, b);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "arrayBaseOffset0", "(Ljava/lang/Class;)I", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.load(1);
			if (!(value instanceof JavaValue)) {
				throw new PanicException("Segfault");
			}
			var wrapper = ((JavaValue<?>) value).getValue();
			if (!(wrapper instanceof JavaClass)) {
				throw new PanicException("Segfault");
			}
			var klass = (JavaClass) wrapper;
			var component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayBaseOffset(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "arrayIndexScale0", "(Ljava/lang/Class;)I", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.load(1);
			if (!(value instanceof JavaValue)) {
				throw new PanicException("Segfault");
			}
			var wrapper = ((JavaValue<?>) value).getValue();
			if (!(wrapper instanceof JavaClass)) {
				throw new PanicException("Segfault");
			}
			var klass = (JavaClass) wrapper;
			var component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayIndexScale(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "addressSize0", "()I", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().addressSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			var locals = ctx.getLocals();
			var klass = locals.load(1);
			if (!(klass instanceof JavaValue)) {
				throw new PanicException("Segfault");
			}
			var wrapper = ((JavaValue<?>) klass).getValue();
			if (!(wrapper instanceof JavaClass)) {
				throw new PanicException("Segfault");
			}
			if (!(wrapper instanceof InstanceJavaClass)) {
				ctx.setResult(new LongValue(-1L));
			} else {
				var utf = vm.getHelper().readUtf8(locals.load(2));
				ctx.setResult(new LongValue(((InstanceJavaClass) wrapper).getFieldOffsetRecursively(utf)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "storeFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "fullFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "compareAndSetInt", "(Ljava/lang/Object;JII)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.load(4).asInt();
			var x = locals.load(5).asInt();
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readInt(obj, offset) == expected;
			if (result) {
				memoryManager.writeInt(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObjectVolatile", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue(obj, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "compareAndSetObject", "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.<ObjectValue>load(4);
			var x = locals.<ObjectValue>load(5);
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readValue(obj, offset) == expected;
			if (result) {
				memoryManager.writeValue(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "compareAndSetLong", "(Ljava/lang/Object;JJJ)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.load(4).asLong();
			var x = locals.load(6).asLong();
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readLong(obj, offset) == expected;
			if (result) {
				memoryManager.writeLong(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
	}

	/**
	 * Injects VM fields.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void injectVMFields(VirtualMachine vm) {
		var classLoader = vm.getSymbols().java_lang_ClassLoader;
		classLoader.getNode().fields.add(new FieldNode(
				ACC_PRIVATE | ACC_FINAL,
				CLASS_LOADER_OOP,
				"Ljava/lang/Object;",
				null,
				null
		));
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vmi
	 * 		VM interface.
	 */
	private static void setInstructions(VMInterface vmi) {
		var nop = new NopProcessor();
		vmi.setProcessor(NOP, nop);

		vmi.setProcessor(ACONST_NULL, new ConstantProcessor(NullValue.INSTANCE));

		// ICONST_M1..INCONST_5
		for (int x = ICONST_M1; x <= ICONST_5; x++) {
			vmi.setProcessor(x, new ConstantIntProcessor(x - ICONST_0));
		}

		vmi.setProcessor(LCONST_0, new ConstantLongProcessor(0L));
		vmi.setProcessor(LCONST_1, new ConstantLongProcessor(1L));

		vmi.setProcessor(FCONST_0, new ConstantFloatProcessor(0.0F));
		vmi.setProcessor(FCONST_1, new ConstantFloatProcessor(1.0F));
		vmi.setProcessor(FCONST_2, new ConstantFloatProcessor(2.0F));

		vmi.setProcessor(DCONST_0, new ConstantDoubleProcessor(0.0D));
		vmi.setProcessor(DCONST_1, new ConstantDoubleProcessor(1.0D));

		vmi.setProcessor(BIPUSH, new BytePushProcessor());
		vmi.setProcessor(SIPUSH, new ShortPushProcessor());
		vmi.setProcessor(LDC, new LdcProcessor());

		vmi.setProcessor(LLOAD, new LongLoadProcessor());
		vmi.setProcessor(ILOAD, new IntLoadProcessor());
		vmi.setProcessor(FLOAD, new FloatLoadProcessor());
		vmi.setProcessor(DLOAD, new DoubleLoadProcessor());
		vmi.setProcessor(ALOAD, new ValueLoadProcessor());

		vmi.setProcessor(IALOAD, new LoadArrayIntProcessor());
		vmi.setProcessor(LALOAD, new LoadArrayLongProcessor());
		vmi.setProcessor(FALOAD, new LoadArrayFloatProcessor());
		vmi.setProcessor(DALOAD, new LoadArrayDoubleProcessor());
		vmi.setProcessor(AALOAD, new LoadArrayValueProcessor());
		vmi.setProcessor(BALOAD, new LoadArrayByteProcessor());
		vmi.setProcessor(CALOAD, new LoadArrayCharProcessor());
		vmi.setProcessor(SALOAD, new LoadArrayShortProcessor());

		vmi.setProcessor(IASTORE, new StoreArrayIntProcessor());
		vmi.setProcessor(LASTORE, new StoreArrayLongProcessor());
		vmi.setProcessor(FASTORE, new StoreArrayFloatProcessor());
		vmi.setProcessor(DASTORE, new StoreArrayDoubleProcessor());
		vmi.setProcessor(AASTORE, new StoreArrayValueProcessor());
		vmi.setProcessor(BASTORE, new StoreArrayByteProcessor());
		vmi.setProcessor(CASTORE, new StoreArrayCharProcessor());
		vmi.setProcessor(SASTORE, new StoreArrayShortProcessor());

		vmi.setProcessor(ISTORE, new IntStoreProcessor());
		vmi.setProcessor(LSTORE, new LongStoreProcessor());
		vmi.setProcessor(FSTORE, new FloatStoreProcessor());
		vmi.setProcessor(DSTORE, new DoubleStoreProcessor());
		vmi.setProcessor(ASTORE, new ValueStoreProcessor());

		vmi.setProcessor(POP, new PopProcessor());
		vmi.setProcessor(POP2, new Pop2Processor());
		vmi.setProcessor(DUP, new DupProcessor());
		vmi.setProcessor(DUP_X1, new DupX1Processor());
		vmi.setProcessor(DUP_X2, new DupX2Processor());
		vmi.setProcessor(DUP2, new Dup2Processor());
		vmi.setProcessor(DUP2_X1, new Dup2X1Processor());
		vmi.setProcessor(DUP2_X2, new Dup2X2Processor());
		vmi.setProcessor(SWAP, new SwapProcessor());

		vmi.setProcessor(IADD, new BiIntProcessor(Integer::sum));
		vmi.setProcessor(LADD, new BiLongProcessor(Long::sum));
		vmi.setProcessor(FADD, new BiFloatProcessor(Float::sum));
		vmi.setProcessor(DADD, new BiDoubleProcessor(Double::sum));

		vmi.setProcessor(ISUB, new BiIntProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(LSUB, new BiLongProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(FSUB, new BiFloatProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(DSUB, new BiDoubleProcessor((v1, v2) -> v1 - v2));

		vmi.setProcessor(IMUL, new BiIntProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(LMUL, new BiLongProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(FMUL, new BiFloatProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(DMUL, new BiDoubleProcessor((v1, v2) -> v1 * v2));

		vmi.setProcessor(IDIV, new BiIntProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(LDIV, new BiLongProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(FDIV, new BiFloatProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(DDIV, new BiDoubleProcessor((v1, v2) -> v1 / v2));

		vmi.setProcessor(IREM, new BiIntProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(LREM, new BiLongProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(FREM, new BiFloatProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(DREM, new BiDoubleProcessor((v1, v2) -> v1 % v2));

		vmi.setProcessor(INEG, new NegativeIntProcessor());
		vmi.setProcessor(LNEG, new NegativeLongProcessor());
		vmi.setProcessor(FNEG, new NegativeFloatProcessor());
		vmi.setProcessor(DNEG, new NegativeDoubleProcessor());

		vmi.setProcessor(ISHL, new BiIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(LSHL, new LongIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(ISHR, new BiIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(LSHR, new LongIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(IUSHR, new BiIntProcessor((v1, v2) -> v1 >>> v2));
		vmi.setProcessor(LUSHR, new LongIntProcessor((v1, v2) -> v1 >>> v2));

		vmi.setProcessor(IAND, new BiIntProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(LAND, new BiLongProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(IOR, new BiIntProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(LOR, new BiLongProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(IXOR, new BiIntProcessor((v1, v2) -> v1 ^ v2));
		vmi.setProcessor(LXOR, new BiLongProcessor((v1, v2) -> v1 ^ v2));

		vmi.setProcessor(IINC, new VariableIncrementProcessor());

		vmi.setProcessor(I2L, new IntToLongProcessor());
		vmi.setProcessor(I2F, new IntToFloatProcessor());
		vmi.setProcessor(I2D, new IntToDoubleProcessor());
		vmi.setProcessor(L2I, new LongToIntProcessor());
		vmi.setProcessor(L2F, new LongToFloatProcessor());
		vmi.setProcessor(L2D, new LongToDoubleProcessor());
		vmi.setProcessor(F2I, new FloatToIntProcessor());
		vmi.setProcessor(F2L, new FloatToLongProcessor());
		vmi.setProcessor(F2D, new FloatToDoubleProcessor());
		vmi.setProcessor(D2I, new DoubleToIntProcessor());
		vmi.setProcessor(D2L, new DoubleToLongProcessor());
		vmi.setProcessor(D2F, new DoubleToFloatProcessor());
		vmi.setProcessor(I2B, new IntToByteProcessor());
		vmi.setProcessor(I2C, new IntToCharProcessor());
		vmi.setProcessor(I2S, new IntToShortProcessor());

		vmi.setProcessor(LCMP, new LongCompareProcessor());
		vmi.setProcessor(FCMPL, new FloatCompareProcessor(-1));
		vmi.setProcessor(FCMPG, new FloatCompareProcessor(1));
		vmi.setProcessor(DCMPL, new DoubleCompareProcessor(-1));
		vmi.setProcessor(DCMPG, new DoubleCompareProcessor(1));

		vmi.setProcessor(IFEQ, new IntJumpProcessor(value -> value == 0));
		vmi.setProcessor(IFNE, new IntJumpProcessor(value -> value != 0));
		vmi.setProcessor(IFLT, new IntJumpProcessor(value -> value < 0));
		vmi.setProcessor(IFGE, new IntJumpProcessor(value -> value >= 0));
		vmi.setProcessor(IFGT, new IntJumpProcessor(value -> value > 0));
		vmi.setProcessor(IFLE, new IntJumpProcessor(value -> value <= 0));

		vmi.setProcessor(IF_ICMPEQ, new BiIntJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ICMPNE, new BiIntJumpProcessor((v1, v2) -> v1 != v2));
		vmi.setProcessor(IF_ICMPLT, new BiIntJumpProcessor((v1, v2) -> v1 < v2));
		vmi.setProcessor(IF_ICMPGE, new BiIntJumpProcessor((v1, v2) -> v1 >= v2));
		vmi.setProcessor(IF_ICMPGT, new BiIntJumpProcessor((v1, v2) -> v1 > v2));
		vmi.setProcessor(IF_ICMPLE, new BiIntJumpProcessor((v1, v2) -> v1 <= v2));

		vmi.setProcessor(IF_ACMPEQ, new BiValueJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ACMPNE, new BiValueJumpProcessor((v1, v2) -> v1 != v2));

		vmi.setProcessor(GOTO, new GotoProcessor());

		// JSR/RET?

		vmi.setProcessor(TABLESWITCH, new TableSwitchProcessor());
		vmi.setProcessor(LOOKUPSWITCH, new LookupSwitchProcessor());

		vmi.setProcessor(IRETURN, new ReturnIntProcessor());
		vmi.setProcessor(LRETURN, new ReturnLongProcessor());
		vmi.setProcessor(FRETURN, new ReturnFloatProcessor());
		vmi.setProcessor(DRETURN, new ReturnDoubleProcessor());
		vmi.setProcessor(ARETURN, new ReturnValueProcessor());
		vmi.setProcessor(RETURN, new ReturnVoidProcessor());

		vmi.setProcessor(GETSTATIC, new GetStaticProcessor());
		vmi.setProcessor(PUTSTATIC, new PutStaticProcessor());
		vmi.setProcessor(GETFIELD, new GetFieldProcessor());
		vmi.setProcessor(PUTFIELD, new PutFieldProcessor());
		vmi.setProcessor(INVOKEVIRTUAL, new VirtualCallProcessor());
		vmi.setProcessor(INVOKESPECIAL, new SpecialCallProcessor());
		vmi.setProcessor(INVOKESTATIC, new StaticCallProcessor());
		vmi.setProcessor(INVOKEINTERFACE, new InterfaceCallProcessor());

		vmi.setProcessor(INVOKEDYNAMIC, new InvokeDynamicLinkerProcessor());
		vmi.setProcessor(NEW, new NewProcessor());
		vmi.setProcessor(ANEWARRAY, new InstanceArrayProcessor());
		vmi.setProcessor(NEWARRAY, new PrimitiveArrayProcessor());
		vmi.setProcessor(ARRAYLENGTH, new ArrayLengthProcessor());

		vmi.setProcessor(ATHROW, new ThrowProcessor());

		vmi.setProcessor(CHECKCAST, new CastProcessor());
		vmi.setProcessor(INSTANCEOF, new InstanceofProcessor());

		vmi.setProcessor(MONITORENTER, new MonitorEnterProcessor());
		vmi.setProcessor(MONITOREXIT, new MonitorExitProcessor());

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(Value::isNull));
	}
}
