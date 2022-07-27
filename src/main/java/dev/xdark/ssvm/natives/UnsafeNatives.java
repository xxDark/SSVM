package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.constant.ConstPoolEntry;
import me.coley.cafedude.classfile.constant.CpString;
import me.coley.cafedude.classfile.constant.CpUtf8;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;

/**
 * Sets up /misc/Unsafe.
 *
 * @author xDark
 */
@UtilityClass
public class UnsafeNatives {

	/**
	 * @param vm VM instance.
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
	 * @param vm      VM instance.
	 * @param unsafe  Unsafe class.
	 * @param uhelper Platform-specific implementation provider.
	 */
	private static void init(VirtualMachine vm, InstanceJavaClass unsafe, UnsafeHelper uhelper) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(unsafe, uhelper.allocateMemory(), "(J)J", ctx -> {
			MemoryBlock block = vm.getMemoryAllocator().allocateDirect(ctx.getLocals().loadLong(1));
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError());
			}
			ctx.setResult(LongValue.of(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.reallocateMemory(), "(JJ)J", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			long bytes = locals.loadLong(3);
			MemoryBlock block = vm.getMemoryAllocator().reallocateDirect(address, bytes);
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError());
			}
			ctx.setResult(LongValue.of(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "freeMemory", "(J)V", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			if (!vm.getMemoryAllocator().freeDirect(address)) {
				throw new PanicException("Segfault");
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.setMemory(), "(Ljava/lang/Object;JJB)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			long bytes = locals.loadLong(4);
			byte b = (byte) locals.loadInt(6);
			data.set(0L, bytes, b);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayBaseOffset(), "(Ljava/lang/Class;)I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> value = locals.<JavaValue<JavaClass>>load(1);
			JavaClass klass = value.getValue();
			JavaClass component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			} else {
				ctx.setResult(IntValue.of(vm.getMemoryManager().arrayBaseOffset(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayIndexScale(), "(Ljava/lang/Class;)I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> value = locals.<JavaValue<JavaClass>>load(1);
			JavaClass klass = value.getValue();
			JavaClass component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			} else {
				ctx.setResult(IntValue.of(vm.getMemoryManager().sizeOfType(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.addressSize(), "()I", ctx -> {
			ctx.setResult(IntValue.of(vm.getMemoryAllocator().addressSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(vm.getMemoryAllocator().getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> klass = locals.<JavaValue<JavaClass>>load(1);
			JavaClass wrapper = klass.getValue();
			if (!(wrapper instanceof InstanceJavaClass)) {
				ctx.setResult(LongValue.M_ONE);
			} else {
				String utf = vm.getHelper().readUtf8(locals.load(2));
				long offset = ((InstanceJavaClass) wrapper).getVirtualFieldOffsetRecursively(utf);
				if (offset != -1L) {
					offset += vm.getMemoryManager().valueBaseOffset(klass);
				}
				ctx.setResult(LongValue.of(offset));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "storeFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "fullFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, uhelper.compareAndSetInt(), "(Ljava/lang/Object;JII)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			ObjectValue value = (ObjectValue) obj;
			long offset = locals.loadLong(2);
			int expected = locals.loadInt(4);
			int x = locals.loadInt(5);
			MemoryManager memoryManager = vm.getMemoryManager();
			boolean result = memoryManager.readInt(value, offset) == expected;
			if (result) {
				memoryManager.writeInt(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		MethodInvoker getObjectVolatile = ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			MemoryManager memoryManager = vm.getMemoryManager();
			ctx.setResult(nonNull(memoryManager.getValue(data.readLongVolatile(0L))));
			return Result.ABORT;
		};
		for (String str : new String[]{"getReferenceVolatile", "getObjectVolatile"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;J)Ljava/lang/Object;", getObjectVolatile)) {
				break;
			}
		}
		MethodInvoker compareAndSetReference = ctx -> {
			Locals locals = ctx.getLocals();
			Value obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			ObjectValue value = (ObjectValue) obj;
			long offset = locals.loadLong(2);
			ObjectValue expected = locals.<ObjectValue>load(4);
			ObjectValue x = locals.<ObjectValue>load(5);
			MemoryManager memoryManager = vm.getMemoryManager();
			ObjectValue oldValue = memoryManager.readValue(value, offset);
			boolean result = oldValue == expected;
			if (result) {
				memoryManager.writeValue(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		};
		vmi.setInvoker(unsafe, uhelper.compareAndSetReference(), "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", compareAndSetReference);
		String compareAndSetObject = uhelper.compareAndSetObject();
		if (compareAndSetObject != null) {
			vmi.setInvoker(unsafe, compareAndSetObject, "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", compareAndSetReference);
		}
		vmi.setInvoker(unsafe, uhelper.compareAndSetLong(), "(Ljava/lang/Object;JJJ)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $value = locals.load(1);
			if ($value.isNull()) {
				throw new PanicException("Segfault");
			}
			ObjectValue value = (ObjectValue) $value;
			long offset = locals.loadLong(2);
			long expected = locals.loadLong(4);
			long x = locals.loadLong(6);
			MemoryManager memoryManager = vm.getMemoryManager();
			boolean result = memoryManager.readLong(value, offset) == expected;
			if (result) {
				memoryManager.writeLong(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		MethodInvoker putObjectVolatile = ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getDataNonNull(locals.load(1), offset);
			buffer.writeLongVolatile(0L, locals.<ObjectValue>load(4).getMemory().getAddress());
			return Result.ABORT;
		};
		for (String str : new String[]{"putReferenceVolatile", "putObjectVolatile"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;JLjava/lang/Object;)V", putObjectVolatile)) {
				break;
			}
		}
		vmi.setInvoker(unsafe, "getIntVolatile", "(Ljava/lang/Object;J)I", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(IntValue.of(data.readIntVolatile(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.ensureClassInitialized(), "(Ljava/lang/Class;)V", ctx -> {
			Value value = ctx.getLocals().load(1);
			vm.getHelper().<JavaValue<JavaClass>>checkNotNull(value).getValue().initialize();
			return Result.ABORT;
		});
		MethodInvoker getObject = ctx -> {
			Locals locals = ctx.getLocals();
			MemoryManager memoryManager = vm.getMemoryManager();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(nonNull(memoryManager.getValue(data.readLong(0L))));
			return Result.ABORT;
		};
		for (String str : new String[]{"getReference", "getObject"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;J)Ljava/lang/Object;", getObject)) {
				break;
			}
		}
		vmi.setInvoker(unsafe, uhelper.objectFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			VMHelper helper = vm.getHelper();
			InstanceValue field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			InstanceJavaClass declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			int slot = field.getInt("slot");
			JavaField fn = helper.getFieldBySlot(declaringClass, slot);
			if (fn != null) {
				long offset = vm.getMemoryManager().valueBaseOffset(declaringClass) + fn.getOffset();
				ctx.setResult(LongValue.of(offset));
			} else {
				ctx.setResult(LongValue.M_ONE);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			VMHelper helper = vm.getHelper();
			InstanceValue field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			InstanceJavaClass declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			int slot = field.getInt("slot");
			JavaField fn = helper.getFieldBySlot(declaringClass, slot);
			if (fn != null) {
				long offset = vm.getMemoryManager().getStaticOffset(declaringClass) + fn.getOffset();
				ctx.setResult(LongValue.of(offset));
			} else {
				ctx.setResult(LongValue.M_ONE);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putByte", "(JB)V", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			MemoryBlock block = nonNull(vm.getMemoryAllocator().findDirectBlock(address));
			block.getData().writeByte(address - block.getAddress(), (byte) locals.loadInt(3));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			MemoryBlock block = nonNull(vm.getMemoryAllocator().findDirectBlock(address));
			block.getData().writeLong(address - block.getAddress(), locals.loadLong(3));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getByte", "(J)B", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			MemoryBlock block = nonNull(vm.getMemoryAllocator().findDirectBlock(address));
			ctx.setResult(IntValue.of(block.getData().readByte(address - block.getAddress())));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putInt", "(Ljava/lang/Object;JI)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			buffer.writeInt(0L, locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldBase(), "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", ctx -> {
			InstanceValue field = vm.getHelper().<InstanceValue>checkNotNull(ctx.getLocals().<ObjectValue>load(1));
			JavaClass klass = ((JavaValue<JavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.defineClass(), "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = ctx.getHelper();
			ObjectValue loader = locals.load(5);
			ObjectValue name = locals.load(1);
			ArrayValue b = helper.checkNotNull(locals.load(2));
			int off = locals.loadInt(3);
			int length = locals.loadInt(4);
			ObjectValue pd = locals.load(6);
			byte[] bytes = helper.toJavaBytes(b);
			InstanceJavaClass defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, "JVM_DefineClass");
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.shouldBeInitialized(), "(Ljava/lang/Class;)Z", ctx -> {
			JavaClass value = vm.getHelper().<JavaValue<JavaClass>>checkNotNull(ctx.getLocals().load(1)).getValue();
			ctx.setResult(value instanceof InstanceJavaClass && ((InstanceJavaClass) value).shouldBeInitialized() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		MethodInvoker pageSize = ctx -> {
			ctx.setResult(IntValue.of(vm.getMemoryAllocator().pageSize()));
			return Result.ABORT;
		};
		for (String str : new String[]{"pageSize0", "pageSize"}) {
			if (vmi.setInvoker(unsafe, str, "()I", pageSize)) {
				break;
			}
		}
		vmi.setInvoker(unsafe, "getLongVolatile", "(Ljava/lang/Object;J)J", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(LongValue.of(data.readLongVolatile(0L)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLong", "(J)J", ctx -> {
			long address = ctx.getLocals().loadLong(1);
			MemoryBlock block = nonNull(vm.getMemoryAllocator().findDirectBlock(address));
			ctx.setResult(LongValue.of(block.getData().readLong(address - block.getAddress())));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "defineAnonymousClass", "(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			JavaValue<JavaClass> host = helper.checkNotNull(locals.load(1));
			ArrayValue bytes = helper.checkNotNull(locals.load(2));
			JavaClass klass = host.getValue();
			byte[] array = helper.toJavaBytes(bytes);
			ClassParseResult result = vm.getClassDefiner().parseClass(null, array, 0, array.length, "JVM_DefineClass");
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException(), "Invalid class");
			}
			ObjectValue loader = klass.getClassLoader();
			InstanceJavaClass generated = helper.newInstanceClass(loader, vm.getMemoryManager().nullValue(), result.getClassReader(), result.getNode());

			// force link
			ClassNode node = generated.getNode();
			node.access |= ACC_VM_HIDDEN;
			vm.getClassLoaders().getClassLoaderData(loader).forceLinkClass(generated);
			generated.link();

			// handle cpPatches
			Value cpPatches = locals.load(3);
			if (!cpPatches.isNull()) {
				ArrayValue arr = (ArrayValue) cpPatches;
				Value[] values = helper.toJavaValues(arr);

				ConstPool cp = generated.getRawClassFile().getPool();
				// TODO implement this in cafedude

				// https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/oops/constantPool.cpp#L1858
				ArrayList<ConstPoolEntry> tmp = new ArrayList<ConstPoolEntry>(cp.size() + 1);
				tmp.add(null);
				for (ConstPoolEntry entry : cp) {
					tmp.add(entry);
					if (entry.isWide()) {
						tmp.add(null);
					}
				}
				Map<String, List<LdcInsnNode>> strings = ((Stream<LdcInsnNode>) (Stream) generated.getNode().methods
					.stream()
					.map(x -> x.instructions)
					.flatMap(x -> StreamSupport.stream(x.spliterator(), false))
					.filter(x -> x instanceof LdcInsnNode))
					.filter(x -> x.cst instanceof String)
					.collect(Collectors.groupingBy(x -> (String) x.cst, Collectors.mapping(Function.identity(), Collectors.toList())));
				for (int i = 1; i < values.length; i++) {
					Value v = values[i];
					if (!v.isNull()) {
						String utf = ((CpUtf8) cp.get(((CpString) tmp.get(i)).getIndex())).getText();
						List<LdcInsnNode> ldcs = strings.get(utf);
						if (ldcs != null) {
							for (LdcInsnNode ldc : ldcs) {
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
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(IntValue.of(data.readInt(0L)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getShort", "(Ljava/lang/Object;J)S", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(IntValue.of(data.readShort(0L)));
			return Result.ABORT;
		});
		MethodInvoker putObject = ctx -> {
			Locals locals = ctx.getLocals();
			MemoryManager memoryManager = vm.getMemoryManager();
			long offset = locals.loadLong(2);
			MemoryData data = getDataNonNull(locals.load(1), offset);
			data.writeLong(0L, locals.<ObjectValue>load(4).getMemory().getAddress());
			return Result.ABORT;
		};
		for (String str : new String[]{"putReference", "putObject"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;JLjava/lang/Object;)V", putObject)) {
				break;
			}
		}
		vmi.setInvoker(unsafe, "getLong", "(Ljava/lang/Object;J)J", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(LongValue.of(data.readLong(0L)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getByte", "(Ljava/lang/Object;J)B", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(IntValue.of(data.readByte(0L)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			JavaClass klass = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1)).getValue();
			InstanceValue instance = ctx.getOperations().allocateInstance(klass);
			ctx.setResult(instance);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putBoolean", "(Ljava/lang/Object;JZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			buffer.writeByte(0L, (byte) locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getBoolean", "(Ljava/lang/Object;J)Z", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(buffer.readByte(0L) != 0 ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(Ljava/lang/Object;JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			data.writeLong(0L, locals.loadLong(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getChar", "(Ljava/lang/Object;J)C", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			ctx.setResult(IntValue.of(buffer.readChar(0L)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putChar", "(Ljava/lang/Object;JC)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.load(1), offset);
			data.writeChar(0L, (char) locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.copyMemory(), "(Ljava/lang/Object;JLjava/lang/Object;JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			MemoryAllocator memoryAllocator = vm.getMemoryAllocator();
			Value src = locals.load(1);
			long srcOffset = locals.loadLong(2);
			Value dst = locals.load(4);
			long dstOffset = locals.loadLong(5);
			long bytes = locals.loadLong(7);
			MemoryData srcData = getData(memoryAllocator, src, srcOffset);
			MemoryData dstData = getData(memoryAllocator, dst, dstOffset);
			srcData.copy(0L, dstData, 0L, bytes);
			return Result.ABORT;
		});
	}

	private static MemoryData getDataNonNull(Value instance, long offset) {
		if (instance.isNull()) {
			throw new PanicException("Segfault");
		}
		MemoryData data = ((ObjectValue) instance).getMemory().getData();
		return data.slice(offset, data.length() - offset);
	}

	private static MemoryData getData(MemoryAllocator allocator, Value instance, long offset) {
		MemoryData data;
		if (instance.isNull()) {
			MemoryBlock memory = nonNull(allocator.findDirectBlock(offset));
			data = memory.getData();
			offset -= memory.getAddress();
		} else {
			data = ((ObjectValue) instance).getMemory().getData();
		}
		return data.slice(offset, data.length() - offset);
	}

	private static <T> T nonNull(T v) {
		if (v == null) {
			throw new PanicException("Segfault");
		}
		return v;
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

		String compareAndSetObject();

		String copyMemory();
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
		public String compareAndSetObject() {
			return null; // No method for JDK 8
		}

		@Override
		public String copyMemory() {
			return "copyMemory";
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
		public String compareAndSetObject() {
			return "compareAndSetObject";
		}

		@Override
		public String copyMemory() {
			return "copyMemory0";
		}
	}
}
