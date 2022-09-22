package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.allocation.MemoryAddress;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.mirror.type.ArrayJavaClass;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.tlc.ThreadLocalStorage;
import dev.xdark.ssvm.util.Assertions;
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
	private final NullValue nullValue;
	private final int objectHeaderSize;
	private final int arrayHeaderSize;
	private final int arrayLengthOffset;

	/**
	 * @param vm VM instance.
	 */
	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
		MemoryAllocator allocator = vm.getMemoryAllocator();
		this.allocator = allocator;
		MemoryBlock emptyHeapBlock = allocator.emptyHeapBlock();
		NullValue value = new NullValue(emptyHeapBlock);
		objects.put(MemoryAddress.of(emptyHeapBlock.getAddress()), value);
		nullValue = value;
		// TODO shrink lock
		int addressSize = allocator.addressSize() + 4; // 4 bytes for lock
		objectHeaderSize = addressSize;
		arrayHeaderSize = addressSize + 4;
		arrayLengthOffset = addressSize;
	}

	@Override
	public ObjectValue nullValue() {
		return nullValue;
	}

	@Override
	public Mutex getMutex(ObjectValue reference) {
		Assertions.check(!reference.isNull(), "null reference");
		MemoryData data = reference.getMemory().getData();
		ObjectSynchronizer synchronizer = vm.getObjectSynchronizer();
		Mutex mutex;
		int id = data.readInt(8L);
		if (id == -1) {
			mutex = synchronizer.acquire();
			data.writeInt(8L, mutex.id());
		} else {
			mutex = synchronizer.get(id);
		}
		return mutex;
	}

	@Override
	public ObjectValue getReference(long address) {
		return objects.get(tlcAddress(address));
	}

	@Override
	public InstanceValue tryNewInstance(InstanceJavaClass javaClass) {
		MemoryBlock memory = allocateInstanceMemory(javaClass);
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
		MemoryBlock memory = allocateInstanceMemory(javaClass);
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
	public ObjectValue readReference(ObjectValue object, long offset) {
		long address = object.getMemory().getData().readLong(offset);
		return getReference(address);
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		ObjectValue value = objects.get(tlcAddress(object.getMemory().getData().readLong(0L)));
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
		return value instanceof ArrayValue ? arrayHeaderSize : objectHeaderSize;
	}

	@Override
	public int valueBaseOffset(JavaClass value) {
		return value.isArray() ? arrayHeaderSize : objectHeaderSize;
	}

	@Override
	public int arrayBaseOffset(JavaClass javaClass) {
		return arrayHeaderSize;
	}

	@Override
	public int arrayBaseOffset(ArrayValue array) {
		return arrayHeaderSize;
	}

	@Override
	public int arrayBaseOffset(Class<?> javaClass) {
		return arrayHeaderSize;
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

	private void outOfMemory() {
		VirtualMachine vm = this.vm;
		vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError(), "heap space");
	}

	private MemoryBlock allocateInstanceMemory(InstanceJavaClass javaClass) {
		long objectSize = objectHeaderSize + javaClass.getOccupiedInstanceSpace();
		return touch(allocator.allocateHeap(objectSize));
	}

	private MemoryBlock allocateClassMemory(InstanceJavaClass javaLangClass, JavaClass javaClass) {
		long size = objectHeaderSize + javaLangClass.getOccupiedInstanceSpace() + (javaClass instanceof InstanceJavaClass ? ((InstanceJavaClass) javaClass).getOccupiedStaticSpace() : 0);
		return touch(allocator.allocateHeap(size));
	}

	private MemoryBlock allocateArrayMemory(int length, long componentSize) {
		long size = arrayHeaderSize + (long) length * componentSize;
		return touch(allocator.allocateHeap(size));
	}

	private MemoryBlock touch(MemoryBlock block) {
		block.getData().writeInt(8L, -1);
		return block;
	}

	private void setClass(MemoryBlock memory, JavaClass jc) {
		long address = jc.getOop().getMemory().getAddress();
		memory.getData().writeLong(0L, address);
	}

	private static MemoryAddress tlcAddress(long addr) {
		return ThreadLocalStorage.get().memoryAddress(addr);
	}
}
