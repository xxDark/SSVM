package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpString;
import me.coley.cafedude.constant.CpUtf8;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LdcInsnNode;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;

/**
 * Sets up /misc/Unsafe.
 *
 * @author xDark
 */
@UtilityClass
public class UnsafeNatives {

	private final String CLASS_LOADER_OOP = NativeJava.CLASS_LOADER_OOP;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass unsafe = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Unsafe");
		UnsafeHelper unsafeHelper;
		if (unsafe == null) {
			unsafe = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Unsafe");
			unsafeHelper = new OldUnsafeHelper();
		} else {
			unsafeHelper = new NewUnsafeHelper();
		}
		vm.getInterface().setInvoker(unsafe, "registerNatives", "()V", MethodInvoker.noop());
		init(vm, unsafe, unsafeHelper);
	}

	/**
	 * @param vm
	 * 		VM instance.
	 * @param unsafe
	 * 		Unsafe class.
	 * @param uhelper
	 * 		Platform-specific implementation provider.
	 */
	private static void init(VirtualMachine vm, InstanceJavaClass unsafe, UnsafeHelper uhelper) {
		val vmi = vm.getInterface();
		vmi.setInvoker(unsafe, uhelper.allocateMemory(), "(J)J", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val block = memoryManager.allocateDirect(ctx.getLocals().load(1).asLong());
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(LongValue.of(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.reallocateMemory(), "(JJ)J", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val address = locals.load(1).asLong();
			val bytes = locals.load(3).asLong();
			val block = memoryManager.reallocateDirect(address, bytes);
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(LongValue.of(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "freeMemory", "(J)V", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val address = locals.load(1).asLong();
			if (!memoryManager.freeMemory(address)) {
				throw new PanicException("Segfault");
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.setMemory(), "(Ljava/lang/Object;JJB)V", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<ObjectValue>load(1);
			val memory = value.getMemory().getData();
			long offset = locals.load(2).asLong();
			val bytes = locals.load(4).asLong();
			val b = locals.load(6).asByte();
			for (; offset < bytes; offset++) {
				memory.put((int) offset, b);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayBaseOffset(), "(Ljava/lang/Class;)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<JavaValue<JavaClass>>load(1);
			val klass = value.getValue();
			val component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(IntValue.of(vm.getMemoryManager().arrayBaseOffset(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayIndexScale(), "(Ljava/lang/Class;)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<JavaValue<JavaClass>>load(1);
			val klass = value.getValue();
			val component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(IntValue.of(vm.getMemoryManager().arrayIndexScale(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.addressSize(), "()I", ctx -> {
			ctx.setResult(IntValue.of(vm.getMemoryManager().addressSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(1);
			val wrapper = klass.getValue();
			if (!(wrapper instanceof InstanceJavaClass)) {
				ctx.setResult(LongValue.M_ONE);
			} else {
				val utf = vm.getHelper().readUtf8(locals.load(2));
				long offset = ((InstanceJavaClass) wrapper).getFieldOffsetRecursively(utf);
				if (offset != -1L) offset += vm.getMemoryManager().valueBaseOffset(klass);
				ctx.setResult(LongValue.of(offset));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "storeFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "fullFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, uhelper.compareAndSetInt(), "(Ljava/lang/Object;JII)Z", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.load(4).asInt();
			val x = locals.load(5).asInt();
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readInt(value, offset) == expected;
			if (result) {
				memoryManager.writeInt(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObjectVolatile", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue(value, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.compareAndSetReference(), "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", ctx -> {
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.<ObjectValue>load(4);
			val x = locals.<ObjectValue>load(5);
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readValue(value, offset) == expected;
			if (result) {
				memoryManager.writeValue(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.compareAndSetLong(), "(Ljava/lang/Object;JJJ)Z", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val $value = locals.load(1);
			if ($value.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) $value;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.load(4).asLong();
			val x = locals.load(6).asLong();
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readLong(value, offset) == expected;
			if (result) {
				memoryManager.writeLong(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putObjectVolatile", "(Ljava/lang/Object;JLjava/lang/Object;)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val o = locals.load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = (int) locals.load(2).asLong();
			val value = locals.load(4);
			val memoryManager = vm.getMemoryManager();
			memoryManager.writeValue((ObjectValue) o, offset, (ObjectValue) value);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getIntVolatile", "(Ljava/lang/Object;J)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(IntValue.of(memoryManager.readInt((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.ensureClassInitialized(), "(Ljava/lang/Class;)V", ctx -> {
			val value = ctx.getLocals().load(1);
			vm.getHelper().<JavaValue<JavaClass>>checkNotNull(value).getValue().initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue((ObjectValue) value, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.objectFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			val helper = vm.getHelper();
			val field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			val declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val slot = field.getInt("slot");
			for (val fn : declaringClass.getDeclaredFields(false)) {
				if (slot == fn.getSlot()) {
					val offset = vm.getMemoryManager().valueBaseOffset(declaringClass) + fn.getOffset();
					ctx.setResult(LongValue.of(offset));
					return Result.ABORT;
				}
			}
			ctx.setResult(LongValue.M_ONE);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			val helper = vm.getHelper();
			val field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			val declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val slot = field.getInt("slot");
			for (val fn : declaringClass.getDeclaredFields(false)) {
				if (slot == fn.getSlot()) {
					val offset = vm.getMemoryManager().getStaticOffset(declaringClass) + fn.getOffset();
					ctx.setResult(LongValue.of(offset));
					return Result.ABORT;
				}
			}
			ctx.setResult(LongValue.M_ONE);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(JJ)V", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val block = memoryManager.getMemory(locals.load(1).asLong());
			if (block == null) {
				throw new PanicException("Segfault");
			}
			block.getData().putLong(0, locals.load(3).asLong());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getByte", "(J)B", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val block = memoryManager.getMemory(locals.load(1).asLong());
			if (block == null) {
				throw new PanicException("Segfault");
			}
			ctx.setResult(IntValue.of(block.getData().get(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putInt", "(Ljava/lang/Object;JI)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			vm.getMemoryManager().writeInt(o, locals.load(2).asLong(), locals.load(4).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldBase(), "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", ctx -> {
			val field = vm.getHelper().<InstanceValue>checkNotNull(ctx.getLocals().<ObjectValue>load(1));
			val klass = ((JavaValue<JavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.defineClass(), "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = ctx.getHelper();
			val loader = locals.<ObjectValue>load(5);
			val name = locals.<ObjectValue>load(1);
			val b = helper.checkArray(locals.load(2));
			val off = locals.load(3).asInt();
			val length = locals.load(4).asInt();
			val pd = locals.<ObjectValue>load(6);
			val bytes = helper.toJavaBytes(b);
			val defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, "JVM_DefineClass");
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.shouldBeInitialized(), "(Ljava/lang/Class;)Z", ctx -> {
			val value = vm.getHelper().<JavaValue<JavaClass>>checkNotNull(ctx.getLocals().load(1)).getValue();
			ctx.setResult(value instanceof InstanceJavaClass && ((InstanceJavaClass) value).shouldBeInitialized() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.pageSize(), "()I", ctx -> {
			ctx.setResult(IntValue.of(vm.getMemoryManager().pageSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLongVolatile", "(Ljava/lang/Object;J)J", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(LongValue.of(memoryManager.readLong((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLong", "(J)J", ctx -> {
			val address = ctx.getLocals().load(1).asLong();
			val block = vm.getMemoryManager().getMemory(address);
			if (block == null) {
				throw new PanicException("Segfault");
			}
			ctx.setResult(LongValue.of(block.getData().getLong((int) (address - block.getAddress()))));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "defineAnonymousClass", "(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val host = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1));
			val bytes = helper.checkArray(locals.load(2));
			val klass = host.getValue();
			val array = helper.toJavaBytes(bytes);
			val result = vm.getClassDefiner().parseClass(null, array, 0, array.length, "JVM_DefineClass");
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Invalid class");
			}
			val loader = klass.getClassLoader();
			val generated = helper.newInstanceClass(loader, NullValue.INSTANCE, result.getClassReader(), result.getNode());

			// force link
			val node = generated.getNode();
			node.access |= ACC_VM_HIDDEN;
			ClassLoaderData classLoaderData;
			if (loader.isNull()) {
				classLoaderData = vm.getBootClassLoaderData();
			} else {
				classLoaderData = ((JavaValue<ClassLoaderData>) ((InstanceValue) loader).getValue(CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
			}
			classLoaderData.forceLinkClass(generated);

			// handle cpPatches
			val cpPatches = locals.load(3);
			if (!cpPatches.isNull()) {
				val arr = (ArrayValue) cpPatches;
				val values = helper.toJavaValues(arr);

				val cp = generated.getRawClassFile().getPool();
				// TODO implement this in cafedude

				// https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/oops/constantPool.cpp#L1858
				val tmp = new ArrayList<ConstPoolEntry>(cp.size() + 1);
				tmp.add(null);
				for (val entry : cp) {
					tmp.add(entry);
					if (entry.isWide()) {
						tmp.add(null);
					}
				}
				val strings = generated.getNode().methods
						.stream()
						.map(x -> x.instructions)
						.flatMap(x -> StreamSupport.stream(x.spliterator(), false))
						.filter(LdcInsnNode.class::isInstance)
						.map(LdcInsnNode.class::cast)
						.filter(x -> x.cst instanceof String)
						.collect(Collectors.groupingBy(x -> (String) x.cst, Collectors.mapping(Function.identity(), Collectors.toList())));
				for (int i = 1; i < values.length; i++) {
					val v = values[i];
					if (!v.isNull()) {
						val utf = ((CpUtf8) cp.get(((CpString) tmp.get(i)).getIndex())).getText();
						val ldcs = strings.get(utf);
						if (ldcs != null) {
							for (val ldc : ldcs) {
								ldc.cst = v;
							}
						}
					}
				}
			}
			ctx.setResult(generated.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getInt", "(Ljava/lang/Object;J)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(IntValue.of(memoryManager.readInt((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			vm.getMemoryManager().writeValue(o, locals.load(2).asLong(), locals.load(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLong", "(Ljava/lang/Object;J)J", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(LongValue.of(memoryManager.readLong((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val klass = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1)).getValue();
			if (!canAllocateInstance(klass)) {
				helper.throwException(vm.getSymbols().java_lang_InstantiationException, "Cannot instantiate " + klass.getName());
			}
			klass.initialize();
			val instance = vm.getMemoryManager().newInstance((InstanceJavaClass) klass);
			helper.initializeDefaultValues(instance);
			ctx.setResult(instance);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putBoolean", "(Ljava/lang/Object;JZ)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			vm.getMemoryManager().writeBoolean(o, locals.load(2).asLong(), locals.load(4).asBoolean());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getBoolean", "(Ljava/lang/Object;J)Z", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readBoolean((ObjectValue) value, offset) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(Ljava/lang/Object;JJ)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			vm.getMemoryManager().writeLong(o, locals.load(2).asLong(), locals.load(4).asLong());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getChar", "(Ljava/lang/Object;J)C", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(IntValue.of(memoryManager.readChar((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putChar", "(Ljava/lang/Object;JC)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			vm.getMemoryManager().writeChar(o, locals.load(2).asLong(), locals.load(4).asChar());
			return Result.ABORT;
		});
	}

	private static boolean canAllocateInstance(JavaClass jc) {
		if (!(jc instanceof InstanceJavaClass) || jc == ((InstanceJavaClass) jc).getVM().getSymbols().java_lang_Class)
			return false;
		int acc = jc.getModifiers();
		return (acc & Opcodes.ACC_ABSTRACT) == 0;
	}

	private interface UnsafeHelper {

		String allocateMemory();

		String reallocateMemory();

		String setMemory();

		String arrayBaseOffset();

		String arrayIndexScale();

		String addressSize();

		String ensureClassInitialized();

		String objectFieldOffset();

		String staticFieldOffset();

		String compareAndSetReference();

		String compareAndSetInt();

		String compareAndSetLong();

		String staticFieldBase();

		String defineClass();

		String shouldBeInitialized();

		String pageSize();
	}

	private static class OldUnsafeHelper implements UnsafeHelper {

		@Override
		public String allocateMemory() {
			return "allocateMemory";
		}

		@Override
		public String reallocateMemory() {
			return "reallocateMemory";
		}

		@Override
		public String setMemory() {
			return "setMemory";
		}

		@Override
		public String arrayBaseOffset() {
			return "arrayBaseOffset";
		}

		@Override
		public String arrayIndexScale() {
			return "arrayIndexScale";
		}

		@Override
		public String addressSize() {
			return "addressSize";
		}

		@Override
		public String ensureClassInitialized() {
			return "ensureClassInitialized";
		}

		@Override
		public String objectFieldOffset() {
			return "objectFieldOffset";
		}

		@Override
		public String staticFieldOffset() {
			return "staticFieldOffset";
		}

		@Override
		public String compareAndSetReference() {
			return "compareAndSwapObject";
		}

		@Override
		public String compareAndSetInt() {
			return "compareAndSwapInt";
		}

		@Override
		public String compareAndSetLong() {
			return "compareAndSwapLong";
		}

		@Override
		public String staticFieldBase() {
			return "staticFieldBase";
		}

		@Override
		public String defineClass() {
			return "defineClass";
		}

		@Override
		public String shouldBeInitialized() {
			return "shouldBeInitialized";
		}

		@Override
		public String pageSize() {
			return "pageSize";
		}
	}

	private static final class NewUnsafeHelper implements UnsafeHelper {

		@Override
		public String allocateMemory() {
			return "allocateMemory0";
		}

		@Override
		public String reallocateMemory() {
			return "reallocateMemory0";
		}

		@Override
		public String setMemory() {
			return "setMemory0";
		}

		@Override
		public String arrayBaseOffset() {
			return "arrayBaseOffset0";
		}

		@Override
		public String arrayIndexScale() {
			return "arrayIndexScale0";
		}

		@Override
		public String addressSize() {
			return "addressSize0";
		}

		@Override
		public String ensureClassInitialized() {
			return "ensureClassInitialized0";
		}

		@Override
		public String objectFieldOffset() {
			return "objectFieldOffset0";
		}

		@Override
		public String staticFieldOffset() {
			return "staticFieldOffset0";
		}

		@Override
		public String compareAndSetReference() {
			return "compareAndSetReference";
		}

		@Override
		public String compareAndSetInt() {
			return "compareAndSetInt";
		}

		@Override
		public String compareAndSetLong() {
			return "compareAndSetLong";
		}

		@Override
		public String staticFieldBase() {
			return "staticFieldBase0";
		}

		@Override
		public String defineClass() {
			return "defineClass0";
		}

		@Override
		public String shouldBeInitialized() {
			return "shouldBeInitialized0";
		}

		@Override
		public String pageSize() {
			return "pageSize0";
		}
	}
}
