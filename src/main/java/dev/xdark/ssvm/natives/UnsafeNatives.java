package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
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
		InstanceClass unsafe = (InstanceClass) vm.findBootstrapClass("jdk/internal/misc/Unsafe");
		UnsafeHelper unsafeHelper;
		if (unsafe == null) {
			unsafe = (InstanceClass) vm.findBootstrapClass("sun/misc/Unsafe");
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
	private static void init(VirtualMachine vm, InstanceClass unsafe, UnsafeHelper uhelper) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(unsafe, uhelper.allocateMemory(), "(J)J", ctx -> {
			MemoryBlock block = vm.getMemoryAllocator().allocateDirect(ctx.getLocals().loadLong(1));
			if (block == null) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_OutOfMemoryError());
			}
			ctx.setResult(block.getAddress());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.reallocateMemory(), "(JJ)J", ctx -> {
			Locals locals = ctx.getLocals();
			long address = locals.loadLong(1);
			long bytes = locals.loadLong(3);
			MemoryBlock block = vm.getMemoryAllocator().reallocateDirect(address, bytes);
			if (block == null) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_OutOfMemoryError());
			}
			ctx.setResult(block.getAddress());
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
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			long bytes = locals.loadLong(4);
			byte b = (byte) locals.loadInt(6);
			data.set(0L, bytes, b);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayBaseOffset(), "(Ljava/lang/Class;)I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> value = locals.loadReference(1);
			JavaClass klass = value.getValue();
			JavaClass component = klass.getComponentType();
			if (component == null) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			} else {
				ctx.setResult(vm.getMemoryManager().arrayBaseOffset(component));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayIndexScale(), "(Ljava/lang/Class;)I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> value = locals.loadReference(1);
			JavaClass klass = value.getValue();
			JavaClass component = klass.getComponentType();
			if (component == null) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			} else {
				ctx.setResult(vm.getMemoryManager().sizeOfType(component));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.addressSize(), "()I", ctx -> {
			ctx.setResult(vm.getMemoryAllocator().addressSize());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(vm.getMemoryAllocator().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			Locals locals = ctx.getLocals();
			JavaValue<JavaClass> klass = locals.loadReference(1);
			JavaClass wrapper = klass.getValue();
			if (!(wrapper instanceof InstanceClass)) {
				ctx.setResult(-1L);
			} else {
				search: {
					String utf = vm.getOperations().readUtf8(locals.loadReference(2));
					List<JavaField> fields = ((InstanceClass) wrapper).virtualFieldArea().list();
					for (JavaField field : fields) {
						if (utf.equals(field.getName())) {
							ctx.setResult(field.getOffset());
							break search;
						}
					}
					ctx.setResult(-1L);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "storeFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "fullFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, uhelper.compareAndSetInt(), "(Ljava/lang/Object;JII)Z", ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue obj = locals.loadReference(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			long offset = locals.loadLong(2);
			int expected = locals.loadInt(4);
			int x = locals.loadInt(5);
			MemoryData data = obj.getData();
			boolean result = data.readInt(offset) == expected;
			if (result) {
				data.writeInt(offset, x);
			}
			ctx.setResult(result ? 1 : 0);
			return Result.ABORT;
		});
		MethodInvoker getObjectVolatile = ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			MemoryManager memoryManager = vm.getMemoryManager();
			ctx.setResult(nonNull(memoryManager.getReference(data.readLongVolatile(0L))));
			return Result.ABORT;
		};
		for (String str : new String[]{"getReferenceVolatile", "getObjectVolatile"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;J)Ljava/lang/Object;", getObjectVolatile)) {
				break;
			}
		}
		MethodInvoker compareAndSetReference = ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue obj = locals.loadReference(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			long offset = locals.loadLong(2);
			ObjectValue expected = locals.loadReference(4);
			ObjectValue x = locals.loadReference(5);
			MemoryManager memoryManager = vm.getMemoryManager();
			ObjectValue oldValue = memoryManager.readReference(obj, offset);
			boolean result = oldValue == expected;
			if (result) {
				memoryManager.writeValue(obj, offset, x);
			}
			ctx.setResult(result ? 1 : 0);
			return Result.ABORT;
		};
		vmi.setInvoker(unsafe, uhelper.compareAndSetReference(), "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", compareAndSetReference);
		String compareAndSetObject = uhelper.compareAndSetObject();
		if (compareAndSetObject != null) {
			vmi.setInvoker(unsafe, compareAndSetObject, "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", compareAndSetReference);
		}
		vmi.setInvoker(unsafe, uhelper.compareAndSetLong(), "(Ljava/lang/Object;JJJ)Z", ctx -> {
			Locals locals = ctx.getLocals();
			ObjectValue value = locals.loadReference(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			long offset = locals.loadLong(2);
			long expected = locals.loadLong(4);
			long x = locals.loadLong(6);
			MemoryData data = value.getData();
			boolean result = data.readLong(offset) == expected;
			if (result) {
				data.writeLong(offset, x);
			}
			ctx.setResult(result ? 1 : 0);
			return Result.ABORT;
		});
		MethodInvoker putObjectVolatile = ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getDataNonNull(locals.loadReference(1), offset);
			buffer.writeLongVolatile(0L, locals.loadReference(4).getMemory().getAddress());
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
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readIntVolatile(0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.ensureClassInitialized(), "(Ljava/lang/Class;)V", ctx -> {
			ObjectValue value = ctx.getLocals().loadReference(1);
			VMOperations ops = vm.getOperations();
			JavaClass klass = vm.getClassStorage().lookup(ops.checkNotNull(value));
			if (klass instanceof InstanceClass) {
				ops.initialize((InstanceClass) klass);
			}
			return Result.ABORT;
		});
		MethodInvoker getObject = ctx -> {
			Locals locals = ctx.getLocals();
			MemoryManager memoryManager = vm.getMemoryManager();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(nonNull(memoryManager.getReference(data.readLong(0L))));
			return Result.ABORT;
		};
		for (String str : new String[]{"getReference", "getObject"}) {
			if (vmi.setInvoker(unsafe, str, "(Ljava/lang/Object;J)Ljava/lang/Object;", getObject)) {
				break;
			}
		}
		vmi.setInvoker(unsafe, uhelper.objectFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			VMOperations ops = vm.getOperations();
			InstanceValue field = ops.checkNotNull(ctx.getLocals().loadReference(1));
			InstanceClass declaringClass = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(field, "clazz", "Ljava/lang/Class;")));
			int slot = ops.getInt(field, "slot");
			JavaField fn = declaringClass.getFieldBySlot(slot);
			if (fn != null) {
				ctx.setResult(fn.getOffset());
			} else {
				ctx.setResult(-1L);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			VMOperations ops = vm.getOperations();
			InstanceValue field = ops.checkNotNull(ctx.getLocals().loadReference(1));
			InstanceClass declaringClass = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(field, "clazz", "Ljava/lang/Class;")));
			int slot = ops.getInt(field, "slot");
			JavaField fn = declaringClass.getFieldBySlot(slot);
			if (fn != null) {
				ctx.setResult(fn.getOffset());
			} else {
				ctx.setResult(-1L);
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
			ctx.setResult(block.getData().readByte(address - block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putInt", "(Ljava/lang/Object;JI)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			buffer.writeInt(0L, locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldBase(), "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", ctx -> {
			InstanceValue field = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(1));
			ctx.setResult(vm.getOperations().getReference(field, "clazz", "Ljava/lang/Class;"));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.defineClass(), "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			ObjectValue loader = locals.loadReference(5);
			ObjectValue name = locals.loadReference(1);
			ArrayValue b = ops.checkNotNull(locals.loadReference(2));
			int off = locals.loadInt(3);
			int length = locals.loadInt(4);
			ObjectValue pd = locals.loadReference(6);
			byte[] bytes = ops.toJavaBytes(b);
			InstanceClass defined = ops.defineClass(loader, ops.readUtf8(name), bytes, off, length, pd, "JVM_DefineClass", true);
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.shouldBeInitialized(), "(Ljava/lang/Class;)Z", ctx -> {
			JavaClass value = vm.getClassStorage().lookup(vm.getOperations().checkNotNull(ctx.getLocals().loadReference(1)));
			ctx.setResult(value instanceof InstanceClass && ((InstanceClass) value).shouldBeInitialized() ? 1 : 0);
			return Result.ABORT;
		});
		MethodInvoker pageSize = ctx -> {
			ctx.setResult(vm.getMemoryAllocator().pageSize());
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
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readLongVolatile(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLong", "(J)J", ctx -> {
			long address = ctx.getLocals().loadLong(1);
			MemoryBlock block = nonNull(vm.getMemoryAllocator().findDirectBlock(address));
			ctx.setResult(block.getData().readLong(address - block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "defineAnonymousClass", "(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			JavaValue<JavaClass> host = ops.checkNotNull(locals.loadReference(1));
			ArrayValue bytes = ops.checkNotNull(locals.loadReference(2));
			JavaClass klass = host.getValue();
			byte[] array = ops.toJavaBytes(bytes);
			ParsedClassData result = vm.getClassDefiner().parseClass(null, array, 0, array.length, "JVM_DefineClass");
			if (result == null) {
				ops.throwException(vm.getSymbols().java_lang_ClassNotFoundException(), "Invalid class");
			}
			ObjectValue loader = klass.getClassLoader();
			InstanceClass generated = ops.defineClass(loader, result, vm.getMemoryManager().nullValue(), "JVM_DefineClass", true);

			// force link
			ClassNode node = generated.getNode();
			node.access |= ACC_VM_HIDDEN;

			// handle cpPatches
			ObjectValue cpPatches = locals.loadReference(3);
			if (!cpPatches.isNull()) {
				ArrayValue arr = (ArrayValue) cpPatches;
				ObjectValue[] values = ops.toJavaValues(arr);

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
					ObjectValue v = values[i];
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
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readInt(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getShort", "(Ljava/lang/Object;J)S", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readShort(0L));
			return Result.ABORT;
		});
		MethodInvoker putObject = ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getDataNonNull(locals.loadReference(1), offset);
			data.writeLong(0L, locals.loadReference(4).getMemory().getAddress());
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
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readLong(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getByte", "(Ljava/lang/Object;J)B", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(data.readByte(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			JavaClass klass = ops.<JavaValue<JavaClass>>checkNotNull(locals.loadReference(1)).getValue();
			InstanceValue instance = ops.allocateInstance(klass);
			ctx.setResult(instance);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putBoolean", "(Ljava/lang/Object;JZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			buffer.writeByte(0L, (byte) locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getBoolean", "(Ljava/lang/Object;J)Z", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(buffer.readByte(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(Ljava/lang/Object;JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			data.writeLong(0L, locals.loadLong(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getChar", "(Ljava/lang/Object;J)C", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData buffer = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			ctx.setResult(buffer.readChar(0L));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putChar", "(Ljava/lang/Object;JC)V", ctx -> {
			Locals locals = ctx.getLocals();
			long offset = locals.loadLong(2);
			MemoryData data = getData(vm.getMemoryAllocator(), locals.loadReference(1), offset);
			data.writeChar(0L, (char) locals.loadInt(4));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.copyMemory(), "(Ljava/lang/Object;JLjava/lang/Object;JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			MemoryAllocator memoryAllocator = vm.getMemoryAllocator();
			ObjectValue src = locals.loadReference(1);
			long srcOffset = locals.loadLong(2);
			ObjectValue dst = locals.loadReference(4);
			long dstOffset = locals.loadLong(5);
			long bytes = locals.loadLong(7);
			MemoryData srcData = getData(memoryAllocator, src, srcOffset);
			MemoryData dstData = getData(memoryAllocator, dst, dstOffset);
			srcData.write(0L, dstData, 0L, bytes);
			return Result.ABORT;
		});
	}

	private static MemoryData getDataNonNull(ObjectValue instance, long offset) {
		if (instance.isNull()) {
			throw new PanicException("Segfault");
		}
		MemoryData data = instance.getMemory().getData();
		return data.slice(offset, data.length() - offset);
	}

	private static MemoryData getData(MemoryAllocator allocator, ObjectValue instance, long offset) {
		MemoryData data;
		if (instance.isNull()) {
			MemoryBlock memory = nonNull(allocator.findDirectBlock(offset));
			data = memory.getData();
			offset -= memory.getAddress();
		} else {
			data = instance.getMemory().getData();
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
