package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.SimpleArrayValue;
import dev.xdark.ssvm.value.SimpleInstanceValue;
import dev.xdark.ssvm.value.SimpleJavaValue;
import dev.xdark.ssvm.value.Value;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
		NullValue nul = NullValue.INSTANCE;
		Memory nullMemory = nul.getMemory();
		long address = nullMemory.getAddress();
		MemoryKey key = newAddress(address);
		memoryBlocks.put(key, new MemoryRef(key, nullMemory));
		objects.put(key, nul);
	}

	@Override
	public Memory allocateDirect(long bytes) {
		return newMemoryBlock(bytes, true).memory;
	}

	@Override
	public synchronized Memory reallocateDirect(long address, long bytes) {
		TreeMap<MemoryKey, MemoryRef> memoryBlocks = this.memoryBlocks;
		MemoryRef ref = memoryBlocks.remove(keyAddress(address));
		Memory memory;
		if (ref == null || !(memory = ref.memory).isDirect()) {
			throw new PanicException("Segfault");
		}
		if (bytes == 0L) {
			return new SimpleMemory(this, null, 0L, true);
		}
		MemoryData buffer = memory.getData();
		long capacity = buffer.length();
		if (bytes < capacity) {
			// can we do that?
			// TODO verify
			throw new PanicException("Segfault");
		}
		Memory newBlock = newMemoryBlock(bytes, true).memory;
		MemoryData newBuffer = newBlock.getData();
		buffer.copy(0L, newBuffer, 0L, buffer.length());
		return newBlock;
	}

	@Override
	public Memory allocateHeap(long bytes) {
		return newMemoryBlock(bytes, false).memory;
	}

	@Override
	public synchronized boolean freeMemory(long address) {
		TreeMap<MemoryKey, MemoryRef> memoryBlocks = this.memoryBlocks;
		MemoryRef mem = memoryBlocks.remove(keyAddress(address));
		if (mem != null) {
			objects.remove(mem.key);
			return true;
		}
		return false;
	}

	@Override
	public synchronized Memory getMemory(long address) {
		TreeMap<MemoryKey, MemoryRef> memoryBlocks = this.memoryBlocks;
		MemoryRef block = memoryBlocks.get(memoryBlocks.floorKey(keyAddress(address)));
		if (block != null) {
			Memory memory = block.memory;
			long diff = address - memory.getAddress();
			if (diff >= memory.getData().length()) {
				return null;
			}
			return memory;
		}
		return null;
	}

	@Override
	public synchronized Value getValue(long address) {
		return objects.get(keyAddress(address));
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		MemoryRef ref = allocateObjectMemory(javaClass);
		Memory memory = ref.memory;
		setClass(memory, javaClass);
		SimpleInstanceValue value = new SimpleInstanceValue(memory);
		synchronized (this) {
			objects.put(ref.key, value);
		}
		return value;
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value) {
		MemoryRef ref = allocateObjectMemory(javaClass);
		Memory memory = ref.memory;
		setClass(memory, javaClass);
		SimpleJavaValue<V> wrapper = new SimpleJavaValue<>(memory, value);
		synchronized (this) {
			objects.put(ref.key, wrapper);
		}
		return wrapper;
	}

	@Override
	public JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass) {
		MemoryRef ref = allocateClassMemory(javaClass, javaClass);
		Memory memory = ref.memory;
		SimpleJavaValue<InstanceJavaClass> wrapper = new SimpleJavaValue<>(memory, javaClass);
		javaClass.setOop(wrapper);
		setClass(memory, javaClass);
		synchronized (this) {
			objects.put(ref.key, wrapper);
		}
		return wrapper;
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length) {
		MemoryRef ref = allocateArrayMemory(length, sizeOfType(javaClass.getComponentType()));
		Memory memory = ref.memory;
		setClass(memory, javaClass);
		memory.getData().writeInt(ARRAY_LENGTH, length);
		SimpleArrayValue value = new SimpleArrayValue(memory);
		synchronized (this) {
			objects.put(ref.key, value);
		}
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
	public synchronized ObjectValue readValue(ObjectValue object, long offset) {
		long address = object.getMemory().getData().readLong(offset);
		return objects.get(keyAddress(address));
	}

	@Override
	public synchronized JavaClass readClass(ObjectValue object) {
		ObjectValue value = objects.get(keyAddress(object.getMemory().getData().readLong(0)));
		if (!(value instanceof JavaValue)) {
			throw new PanicException("Segfault");
		}
		Object wrapper = ((JavaValue<?>) value).getValue();
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
		return createOopForClass(vm.getSymbols().java_lang_Class(), javaClass);
	}

	@Override
	public <C extends JavaClass> JavaValue<C> createOopForClass(InstanceJavaClass javaLangClass, C javaClass) {
		MemoryRef ref = allocateClassMemory(javaLangClass, javaClass);
		Memory memory = ref.memory;
		setClass(memory, javaLangClass);
		SimpleJavaValue<C> wrapper = new SimpleJavaValue<>(memory, javaClass);
		synchronized (this) {
			objects.put(ref.key, wrapper);
		}
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
	public int sizeOfType(JavaClass javaClass) {
		VMPrimitives primitives = vm.getPrimitives();
		if (javaClass == primitives.longPrimitive() || javaClass == primitives.doublePrimitive()) {
			return 8;
		}
		if (javaClass == primitives.intPrimitive() || javaClass == primitives.floatPrimitive()) {
			return 4;
		}
		if (javaClass == primitives.charPrimitive() || javaClass == primitives.shortPrimitive()) {
			return 2;
		}
		if (javaClass == primitives.bytePrimitive() || javaClass == primitives.booleanPrimitive()) {
			return 1;
		}
		return 8;
	}

	@Override
	public int sizeOfType(Class<?> javaClass) {
		if (javaClass == long.class || javaClass == double.class) {
			return 8;
		}
		if (javaClass == int.class || javaClass == float.class) {
			return 4;
		}
		if (javaClass == char.class || javaClass == short.class) {
			return 2;
		}
		if (javaClass == byte.class || javaClass == boolean.class) {
			return 1;
		}
		return 8;
	}

	@Override
	public long longSize() {
		return 8L;
	}

	@Override
	public long doubleSize() {
		return 8L;
	}

	@Override
	public long intSize() {
		return 4L;
	}

	@Override
	public long floatSize() {
		return 4L;
	}

	@Override
	public long charSize() {
		return 2L;
	}

	@Override
	public long shortSize() {
		return 2L;
	}

	@Override
	public long byteSize() {
		return 1L;
	}

	@Override
	public long booleanSize() {
		return 1L;
	}

	@Override
	public long objectSize() {
		return 8L;
	}

	@Override
	public Memory zero() {
		return NullValue.INSTANCE.getMemory();
	}

	@Override
	public long getStaticOffset(JavaClass jc) {
		JavaClass jlc = vm.getSymbols().java_lang_Class();
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

	@Override
	public void claim(Memory memory) {
		MemoryRef copy = newMemoryBlock(memory.getAddress(), memory.isDirect());
		Memory newMemory = copy.memory;
		memory.getData().transferTo(newMemory.getData());
		memoryBlocks.put(copy.key, copy);
	}

	private synchronized MemoryRef newMemoryBlock(long size, boolean isDirect) {
		if (size > Integer.MAX_VALUE) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError());
			return null;
		}
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		TreeMap<MemoryKey, MemoryRef> memoryBlocks = this.memoryBlocks;
		MemoryKey tlc = MEMORY_KEY_TCL.get();
		long address;
		while(true) {
			address = rng.nextLong();
			if (memoryBlocks.isEmpty()) {
				break;
			}
			tlc.address = address;
			MemoryKey existingBlock = memoryBlocks.floorKey(tlc);
			if (existingBlock == null) {
				MemoryKey low = memoryBlocks.firstKey();
				if (address + size < low.address) {
					break;
				}
				continue;
			}
			MemoryRef ref = memoryBlocks.get(existingBlock);
			Memory block = ref.memory;
			long cap = block.getData().length();
			if (existingBlock.address + cap >= address) {
				continue;
			}
			break;
		}
		SimpleMemory block = new SimpleMemory(this, alloc((int) size), address, isDirect);
		MemoryKey key = newAddress(address);
		MemoryRef ref = new MemoryRef(key, block);
		memoryBlocks.put(key, ref);
		return ref;
	}

	private MemoryRef allocateObjectMemory(JavaClass javaClass) {
		long objectSize = OBJECT_HEADER_SIZE + javaClass.getVirtualFieldLayout().getSize();
		return newMemoryBlock(objectSize, true);
	}

	private MemoryRef allocateClassMemory(JavaClass jlc, JavaClass javaClass) {
		long size = OBJECT_HEADER_SIZE + jlc.getVirtualFieldLayout().getSize() + javaClass.getStaticFieldLayout().getSize();
		return newMemoryBlock(size, true);
	}

	private MemoryRef allocateArrayMemory(int length, long componentSize) {
		return newMemoryBlock(OBJECT_HEADER_SIZE + (long) length * componentSize, false);
	}

	private void setClass(Memory memory, JavaClass jc) {
		long address = jc.getOop().getMemory().getAddress();
		memory.getData().writeLong(0L, address);
	}

	private static MemoryData alloc(int size) {
		return MemoryData.buffer(ByteBuffer.allocate(size).order(ORDER));
	}

	private static MemoryKey keyAddress(long address) {
		MemoryKey key = MEMORY_KEY_TCL.get();
		key.address = address;
		return key;
	}

	private static MemoryKey newAddress(long address) {
		MemoryKey key = new MemoryKey();
		key.address = address;
		return key;
	}

	private static final class MemoryKey implements Comparable<MemoryKey> {
		long address;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof MemoryKey)) {
				return false;
			}

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
