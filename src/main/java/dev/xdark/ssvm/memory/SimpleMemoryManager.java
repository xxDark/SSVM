package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Simple and dumb implementation of a memory manager.
 *
 * @author xDark
 */
public final class SimpleMemoryManager implements MemoryManager {

	private static final ThreadLocal<MemoryKey> MEMORY_KEY_TCL = ThreadLocal.withInitial(MemoryKey::new);
	private static final ByteOrder ORDER = ByteOrder.nativeOrder();
	private static final int ADDRESS_SIZE = 8;
	private static final long OBJECT_HEADER_SIZE = ADDRESS_SIZE + 4L;
	private static final long ARRAY_LENGTH = ADDRESS_SIZE;
	private final TreeMap<MemoryKey, MemoryRef> memoryBlocks = new TreeMap<>();
	private final Map<MemoryKey, ObjectValue> objects = new WeakHashMap<>();

	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
		val nul = NullValue.INSTANCE;
		val nullMemory = nul.getMemory();
		val address = nullMemory.getAddress();
		val key = newAddress(address);
		memoryBlocks.put(key, new MemoryRef(key, nullMemory));
		objects.put(key, nul);
	}

	@Override
	public Memory allocateDirect(long bytes) {
		return newMemoryBlock(bytes, true).memory;
	}

	@Override
	public Memory reallocateDirect(long address, long bytes) {
		val memoryBlocks = this.memoryBlocks;
		val ref = memoryBlocks.remove(keyAddress(address));
		Memory memory;
		if (ref == null || !(memory = ref.memory).isDirect()) {
			throw new PanicException("Segfault");
		}
		if (bytes == 0L) {
			return new SimpleMemory(this, null, 0L, true);
		}
		val buffer = memory.getData();
		val capacity = buffer.length();
		if (bytes < capacity) {
			// can we do that?
			// TODO verify
			throw new PanicException("Segfault");
		}
		val newBlock = newMemoryBlock(bytes, true).memory;
		val newBuffer = newBlock.getData();
		buffer.copy(0L, newBuffer, 0L, buffer.length());
		return newBlock;
	}

	@Override
	public Memory allocateHeap(long bytes) {
		return newMemoryBlock(bytes, false).memory;
	}

	@Override
	public boolean freeMemory(long address) {
		val memoryBlocks = this.memoryBlocks;
		val mem = memoryBlocks.remove(keyAddress(address));
		if (mem != null) {
			objects.remove(mem.key);
			return true;
		}
		return false;
	}

	@Override
	public Memory getMemory(long address) {
		val memoryBlocks = this.memoryBlocks;
		val block = memoryBlocks.get(memoryBlocks.floorKey(keyAddress(address)));
		if (block != null) {
			val memory = block.memory;
			val diff = address - memory.getAddress();
			if (diff >= memory.getData().length()) {
				return null;
			}
			return memory;
		}
		return null;
	}

	@Override
	public Value getValue(long address) {
		return objects.get(keyAddress(address));
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		val ref = allocateObjectMemory(javaClass);
		val memory = ref.memory;
		setClass(memory, javaClass);
		val value = new SimpleInstanceValue(memory);
		objects.put(ref.key, value);
		return value;
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value) {
		val ref = allocateObjectMemory(javaClass);
		val memory = ref.memory;
		setClass(memory, javaClass);
		val wrapper = new SimpleJavaValue<>(memory, value);
		objects.put(ref.key, wrapper);
		return wrapper;
	}

	@Override
	public JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass) {
		val ref = allocateClassMemory(javaClass, javaClass);
		val memory = ref.memory;
		val wrapper = new SimpleJavaValue<>(memory, javaClass);
		javaClass.setOop(wrapper);
		setClass(memory, javaClass);
		objects.put(ref.key, wrapper);
		return wrapper;
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length) {
		val ref = allocateArrayMemory(length, arrayIndexScale(javaClass.getComponentType()));
		val memory = ref.memory;
		setClass(memory, javaClass);
		memory.getData().writeInt(ARRAY_LENGTH, length);
		val value = new SimpleArrayValue(memory);
		objects.put(ref.key, value);
		return value;
	}

	@Override
	public long readLong(ObjectValue object, long offset) {
		return object.getMemory().getData().readLong(offset);
	}

	@Override
	public double readDouble(ObjectValue object, long offset) {
		return Double.longBitsToDouble(object.getMemory().getData().readLong(offset));
	}

	@Override
	public int readInt(ObjectValue object, long offset) {
		return object.getMemory().getData().readInt(offset);
	}

	@Override
	public float readFloat(ObjectValue object, long offset) {
		return Float.intBitsToFloat(object.getMemory().getData().readInt(offset));
	}

	@Override
	public char readChar(ObjectValue object, long offset) {
		return object.getMemory().getData().readChar(offset);
	}

	@Override
	public short readShort(ObjectValue object, long offset) {
		return object.getMemory().getData().readShort(offset);
	}

	@Override
	public byte readByte(ObjectValue object, long offset) {
		return object.getMemory().getData().readByte(offset);
	}

	@Override
	public boolean readBoolean(ObjectValue object, long offset) {
		return readByte(object, offset) != 0;
	}

	@Override
	public Object readOop(ObjectValue object, long offset) {
		return UnsafeUtil.byAddress(object.getMemory().getData().readLong(offset));
	}

	@Override
	public ObjectValue readValue(ObjectValue object, long offset) {
		val address = object.getMemory().getData().readLong(offset);
		return objects.get(keyAddress(address));
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		val value = objects.get(keyAddress(object.getMemory().getData().readLong(0)));
		if (!(value instanceof JavaValue)) {
			throw new PanicException("Segfault");
		}
		val wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			throw new PanicException("Segfault");
		}
		return (JavaClass) wrapper;
	}

	@Override
	public int readArrayLength(ArrayValue array) {
		return array.getMemory().getData().readInt(ARRAY_LENGTH);
	}

	@Override
	public void writeLong(ObjectValue object, long offset, long value) {
		object.getMemory().getData().writeLong(offset, value);
	}

	@Override
	public void writeDouble(ObjectValue object, long offset, double value) {
		object.getMemory().getData().writeLong(offset, Double.doubleToRawLongBits(value));
	}

	@Override
	public void writeInt(ObjectValue object, long offset, int value) {
		object.getMemory().getData().writeInt(offset, value);
	}

	@Override
	public void writeFloat(ObjectValue object, long offset, float value) {
		object.getMemory().getData().writeInt(offset, Float.floatToRawIntBits(value));
	}

	@Override
	public void writeChar(ObjectValue object, long offset, char value) {
		object.getMemory().getData().writeChar(offset, value);
	}

	@Override
	public void writeShort(ObjectValue object, long offset, short value) {
		object.getMemory().getData().writeShort(offset, value);
	}

	@Override
	public void writeByte(ObjectValue object, long offset, byte value) {
		object.getMemory().getData().writeByte(offset, value);
	}

	@Override
	public void writeBoolean(ObjectValue object, long offset, boolean value) {
		writeByte(object, offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void writeOop(ObjectValue object, long offset, Object value) {
		object.getMemory().getData().writeLong(offset, UnsafeUtil.addressOf(value));
	}

	@Override
	public void writeValue(ObjectValue object, long offset, ObjectValue value) {
		object.getMemory().getData().writeLong(offset, value.getMemory().getAddress());
	}

	@Override
	public <C extends JavaClass> JavaValue<C> createOopForClass(C javaClass) {
		val jlc = vm.findBootstrapClass("java/lang/Class");
		val ref = allocateClassMemory(jlc, javaClass);
		val memory = ref.memory;
		setClass(memory, jlc);
		val wrapper = new SimpleJavaValue<>(memory, javaClass);
		objects.put(ref.key, wrapper);
		return wrapper;
	}

	@Override
	public ByteOrder getByteOrder() {
		return ORDER;
	}

	@Override
	public int addressSize() {
		return ADDRESS_SIZE;
	}

	@Override
	public int pageSize() {
		return UnsafeUtil.get().pageSize();
	}

	@Override
	public int valueBaseOffset(ObjectValue value) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int valueBaseOffset(JavaClass value) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(JavaClass javaClass) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(ArrayValue array) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(Class<?> javaClass) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayIndexScale(JavaClass javaClass) {
		val primitives = vm.getPrimitives();
		if (javaClass == primitives.longPrimitive || javaClass == primitives.doublePrimitive) return 8;
		if (javaClass == primitives.intPrimitive || javaClass == primitives.floatPrimitive) return 4;
		if (javaClass == primitives.charPrimitive || javaClass == primitives.shortPrimitive) return 2;
		if (javaClass == primitives.bytePrimitive || javaClass == primitives.booleanPrimitive) return 1;
		return 8;
	}

	@Override
	public int arrayIndexScale(Class<?> javaClass) {
		if (javaClass == long.class || javaClass == double.class) return 8;
		if (javaClass == int.class || javaClass == float.class) return 4;
		if (javaClass == char.class || javaClass == short.class) return 2;
		if (javaClass == byte.class || javaClass == boolean.class) return 1;
		return 8;
	}

	@Override
	public Memory zero() {
		return NullValue.INSTANCE.getMemory();
	}

	@Override
	public long getStaticOffset(JavaClass jc) {
		val jlc = vm.findBootstrapClass("java/lang/Class");
		return OBJECT_HEADER_SIZE + jlc.getVirtualFieldLayout().getSize();
	}

	@Override
	public Collection<Memory> listMemory() {
		return memoryBlocks.values().stream().map(x -> x.memory).collect(Collectors.toList());
	}

	@Override
	public Collection<ObjectValue> listObjects() {
		return objects.values();
	}

	@Override
	public long freeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	@Override
	public long totalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	@Override
	public long maxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	private MemoryRef newMemoryBlock(long size, boolean isDirect) {
		if (size > Integer.MAX_VALUE) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			return null;
		}
		val rng = ThreadLocalRandom.current();
		val memoryBlocks = this.memoryBlocks;
		val tlc = MEMORY_KEY_TCL.get();
		long address;
		while (true) {
			address = rng.nextLong();
			if (memoryBlocks.isEmpty()) break;
			tlc.address = address;
			val existingBlock = memoryBlocks.floorKey(tlc);
			if (existingBlock == null) {
				val low = memoryBlocks.firstKey();
				if (address + size < low.address) break;
				continue;
			}
			val ref = memoryBlocks.get(existingBlock);
			val block = ref.memory;
			val cap = block.getData().length();
			if (existingBlock.address + cap >= address) {
				continue;
			}
			break;
		}
		val block = new SimpleMemory(this, alloc((int) size), address, isDirect);
		val key = newAddress(address);
		val ref = new MemoryRef(key, block);
		memoryBlocks.put(key, ref);
		return ref;
	}

	private MemoryRef allocateObjectMemory(JavaClass javaClass) {
		val objectSize = OBJECT_HEADER_SIZE + javaClass.getVirtualFieldLayout().getSize();
		return newMemoryBlock(objectSize, true);
	}

	private MemoryRef allocateClassMemory(JavaClass jlc, JavaClass javaClass) {
		val size = OBJECT_HEADER_SIZE + jlc.getVirtualFieldLayout().getSize() + javaClass.getStaticFieldLayout().getSize();
		return newMemoryBlock(size, true);
	}

	private MemoryRef allocateArrayMemory(int length, long componentSize) {
		return newMemoryBlock(OBJECT_HEADER_SIZE + (long) length * componentSize, false);
	}

	private void setClass(Memory memory, JavaClass jc) {
		val address = jc.getOop().getMemory().getAddress();
		memory.getData().writeLong(0L, address);
	}

	private static MemoryData alloc(int size) {
		return MemoryData.buffer(ByteBuffer.allocate(size).order(ORDER));
	}

	private static MemoryKey keyAddress(long address) {
		val key = MEMORY_KEY_TCL.get();
		key.address = address;
		return key;
	}

	public static MemoryKey newAddress(long address) {
		val key = new MemoryKey();
		key.address = address;
		return key;
	}

	public static MemoryKey newAddress(Memory memory) {
		val key = new MemoryKey();
		key.address = memory.getAddress();
		return key;
	}

	private static final class MemoryKey implements Comparable<MemoryKey> {
		long address;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof MemoryKey)) return false;

			MemoryKey memoryKey = (MemoryKey) o;

			return address == memoryKey.address;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(address);
		}

		@Override
		public int compareTo(MemoryKey o) {
			return Long.compare(address, o.address);
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class MemoryRef {

		final MemoryKey key;
		final Memory memory;
	}
}
