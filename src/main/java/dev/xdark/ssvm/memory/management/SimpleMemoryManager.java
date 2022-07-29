package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.allocation.MemoryAddress;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.gc.GarbageCollector;
import dev.xdark.ssvm.memory.gc.NoopGarbageCollector;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.tlc.ThreadLocalStorage;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.SimpleArrayValue;
import dev.xdark.ssvm.value.SimpleInstanceValue;
import dev.xdark.ssvm.value.SimpleJavaValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple and dumb implementation of a memory manager.
 *
 * @author xDark
 */
public class SimpleMemoryManager implements MemoryManager {

	private final Map<MemoryAddress, ObjectValue> objects = new HashMap<>();

	protected final VirtualMachine vm;
	private final MemoryAllocator allocator;
	private final GarbageCollector garbageCollector;
	private final NullValue nullValue;
	private final int gcReserved;
	private final int objectHeaderSize;
	private final int arrayLengthOffset;

	/**
	 * @param vm VM instance.
	 */
	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
		MemoryAllocator allocator = vm.getMemoryAllocator();
		this.allocator = allocator;
		GarbageCollector garbageCollector = createGarbageCollector();
		this.garbageCollector = garbageCollector;
		MemoryBlock emptyHeapBlock = allocator.emptyHeapBlock();
		NullValue value = new NullValue(emptyHeapBlock);
		objects.put(MemoryAddress.of(emptyHeapBlock.getAddress()), value);
		nullValue = value;
		int gcReserved = garbageCollector.reservedHeaderSize();
		this.gcReserved = gcReserved;
		int addressSize = allocator.addressSize() + gcReserved; // Reserve space for GC
		objectHeaderSize = addressSize + 4;
		arrayLengthOffset = addressSize;
	}

	@Override
	public ObjectValue nullValue() {
		return nullValue;
	}

	@Override
	public ObjectValue getValue(long address) {
		return objects.get(tlcAddress(address));
	}

	@Override
	public InstanceValue tryNewInstance(InstanceJavaClass javaClass) {
		MemoryBlock memory = allocateObjectMemory(javaClass);
		if (memory == null) {
			return null;
		}
		setClass(memory, javaClass);
		SimpleInstanceValue value = new SimpleInstanceValue(this, memory);
		objects.put(MemoryAddress.of(memory.getAddress()), value);
		return value;
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		InstanceValue value = tryNewInstance(javaClass);
		if (value == null) {
			outOfMemory();
		}
		return value;
	}

	@Override
	public <V> JavaValue<V> tryNewJavaInstance(InstanceJavaClass javaClass, V value) {
		MemoryBlock memory = allocateObjectMemory(javaClass);
		if (memory == null) {
			return null;
		}
		setClass(memory, javaClass);
		SimpleJavaValue<V> wrapper = new SimpleJavaValue<>(this, memory, value);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
		return wrapper;
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value) {
		JavaValue<V> wrapper = tryNewJavaInstance(javaClass, value);
		if (wrapper == null) {
			outOfMemory();
		}
		return wrapper;
	}

	@Override
	public JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass) {
		MemoryBlock memory = allocateClassMemory(javaClass, javaClass);
		if (memory == null) {
			outOfMemory();
		}
		SimpleJavaValue<InstanceJavaClass> wrapper = new SimpleJavaValue<>(this, memory, javaClass);
		javaClass.setOop(wrapper);
		//noinspection ConstantConditions
		setClass(memory, javaClass);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
		return wrapper;
	}

	@Override
	public ArrayValue tryNewArray(ArrayJavaClass javaClass, int length) {
		MemoryBlock memory = allocateArrayMemory(length, sizeOfType(javaClass.getComponentType()));
		if (memory == null) {
			return null;
		}
		setClass(memory, javaClass);
		SimpleArrayValue value = new SimpleArrayValue(this, memory);
		memory.getData().writeInt(arrayLengthOffset, length);
		objects.put(MemoryAddress.of(memory.getAddress()), value);
		return value;
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length) {
		ArrayValue value = tryNewArray(javaClass, length);
		if (value == null) {
			outOfMemory();
		}
		return value;
	}

	@Override
	public ObjectValue readValue(ObjectValue object, long offset) {
		long address = object.getMemory().getData().readLong(offset);
		return objects.get(tlcAddress(address));
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		ObjectValue value = objects.get(tlcAddress(object.getMemory().getData().readLong(gcReserved)));
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
		return array.getMemory().getData().readInt(arrayLengthOffset);
	}

	@Override
	public void writeValue(ObjectValue object, long offset, ObjectValue value) {
		object.getMemory().getData().writeLong(offset, value.getMemory().getAddress());
	}

	@Override
	public ObjectValue getAndWriteValue(ObjectValue object, long offset, ObjectValue value) {
		MemoryData data = object.getMemory().getData();
		ObjectValue old = objects.get(tlcAddress(data.readLong(offset)));
		data.writeLong(offset, value.getMemory().getAddress());
		return old;
	}

	@Override
	public <C extends JavaClass> JavaValue<C> tryNewClassOop(C javaClass) {
		InstanceJavaClass javaLangClass = vm.getSymbols().java_lang_Class();
		MemoryBlock memory = allocateClassMemory(javaLangClass, javaClass);
		if (memory == null) {
			return null;
		}
		setClass(memory, javaLangClass);
		SimpleJavaValue<C> wrapper = new SimpleJavaValue<>(this, memory, javaClass);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
		return wrapper;
	}

	@Override
	public <C extends JavaClass> JavaValue<C> newClassOop(C javaClass) {
		JavaValue<C> value = tryNewClassOop(javaClass);
		if (value == null) {
			outOfMemory();
		}
		return value;
	}

	@Override
	public int valueBaseOffset(ObjectValue value) {
		return objectHeaderSize;
	}

	@Override
	public int valueBaseOffset(JavaClass value) {
		return objectHeaderSize;
	}

	@Override
	public int arrayBaseOffset(JavaClass javaClass) {
		return objectHeaderSize;
	}

	@Override
	public int arrayBaseOffset(ArrayValue array) {
		return objectHeaderSize;
	}

	@Override
	public int arrayBaseOffset(Class<?> javaClass) {
		return objectHeaderSize;
	}

	@Override
	public int sizeOfType(JavaClass javaClass) {
		VMPrimitives primitives = vm.getPrimitives();
		if (javaClass == primitives.longPrimitive()) {
			return longSize();
		}
		if (javaClass == primitives.doublePrimitive()) {
			return doubleSize();
		}
		if (javaClass == primitives.intPrimitive()) {
			return intSize();
		}
		if (javaClass == primitives.floatPrimitive()) {
			return floatSize();
		}
		if (javaClass == primitives.charPrimitive()) {
			return charSize();
		}
		if (javaClass == primitives.shortPrimitive()) {
			return 2;
		}
		if (javaClass == primitives.bytePrimitive()) {
			return byteSize();
		}
		if (javaClass == primitives.booleanPrimitive()) {
			return booleanSize();
		}
		return objectSize();
	}

	@Override
	public int sizeOfType(Class<?> javaClass) {
		if (javaClass == long.class) {
			return longSize();
		}
		if (javaClass == double.class) {
			return doubleSize();
		}
		if (javaClass == int.class) {
			return intSize();
		}
		if (javaClass == float.class) {
			return floatSize();
		}
		if (javaClass == char.class) {
			return charSize();
		}
		if (javaClass == short.class) {
			return 2;
		}
		if (javaClass == byte.class) {
			return byteSize();
		}
		if (javaClass == boolean.class) {
			return booleanSize();
		}
		return objectSize();
	}

	@Override
	public int longSize() {
		return 8;
	}

	@Override
	public int doubleSize() {
		return 8;
	}

	@Override
	public int intSize() {
		return 4;
	}

	@Override
	public int floatSize() {
		return 4;
	}

	@Override
	public int charSize() {
		return 2;
	}

	@Override
	public int shortSize() {
		return 2;
	}

	@Override
	public int byteSize() {
		return 1;
	}

	@Override
	public int booleanSize() {
		return 1;
	}

	@Override
	public int objectSize() {
		return 8;
	}

	@Override
	public long getStaticOffset(JavaClass jc) {
		JavaClass jlc = vm.getSymbols().java_lang_Class();
		return objectHeaderSize + jlc.getVirtualFieldLayout().getSize();
	}

	@Override
	public Collection<ObjectValue> listObjects() {
		return objects.values();
	}

	@Override
	public void writeDefaults(ObjectValue value) {
		if (value.isNull()) {
			throw new PanicException("Segfault");
		}
		MemoryBlock block = value.getMemory();
		MemoryData data = block.getData();
		int arrayLengthOffset = this.arrayLengthOffset;
		data.set(arrayLengthOffset, data.length() - arrayLengthOffset, (byte) 0);
	}

	@Override
	public GarbageCollector getGarbageCollector() {
		return garbageCollector;
	}

	/**
	 * Creates new garbage collector.
	 *
	 * @return garbage collector.
	 */
	protected GarbageCollector createGarbageCollector() {
		return new NoopGarbageCollector();
	}

	private void outOfMemory() {
		VirtualMachine vm = this.vm;
		vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError(), "heap space");
	}

	private MemoryBlock allocateObjectMemory(JavaClass javaClass) {
		long objectSize = objectHeaderSize + javaClass.getVirtualFieldLayout().getSize();
		return allocator.allocateHeap(objectSize);
	}

	private MemoryBlock allocateClassMemory(JavaClass jlc, JavaClass javaClass) {
		long size = objectHeaderSize + jlc.getVirtualFieldLayout().getSize() + javaClass.getStaticFieldLayout().getSize();
		return allocator.allocateHeap(size);
	}

	private MemoryBlock allocateArrayMemory(int length, long componentSize) {
		long size = objectHeaderSize + (long) length * componentSize;
		return allocator.allocateHeap(size);
	}

	private void setClass(MemoryBlock memory, JavaClass jc) {
		long address = jc.getOop().getMemory().getAddress();
		memory.getData().writeLong(gcReserved, address);
	}

	private static MemoryAddress tlcAddress(long addr) {
		return ThreadLocalStorage.get().memoryAddress(addr);
	}
}
