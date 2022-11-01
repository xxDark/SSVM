package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.LanguageSpecification;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.allocation.MemoryAddress;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.threadlocal.ThreadLocalStorage;
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
	private final VirtualMachine vm;
	private final NullValue nullValue;
	private final int objectHeaderSize;
	private final int arrayHeaderSize;
	private final int arrayLengthOffset;

	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
		MemoryAllocator allocator = vm.getMemoryAllocator();
		MemoryBlock emptyHeapBlock = allocator.emptyHeapBlock();
		NullValue value = new NullValue(emptyHeapBlock);
		objects.put(MemoryAddress.of(emptyHeapBlock.getAddress()), value);
		nullValue = value;
		// TODO rework object headers.
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
	public InstanceValue tryNewInstance(InstanceClass javaClass) {
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
	public InstanceValue newInstance(InstanceClass javaClass) {
		return tryNewInstance(javaClass);
	}

	@Override
	public <V> JavaValue<V> tryNewJavaInstance(InstanceClass javaClass, V value) {
		MemoryBlock memory = allocateInstanceMemory(javaClass);
		setClass(memory, javaClass);
		SimpleJavaValue<V> wrapper = new SimpleJavaValue<>(this, memory, value);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
		return wrapper;
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceClass javaClass, V value) {
		return tryNewJavaInstance(javaClass, value);
	}

	@Override
	public void newJavaLangClass(InstanceClass javaClass) {
		MemoryBlock memory = allocateClassMemory(javaClass, javaClass);
		InstanceValue wrapper = new SimpleInstanceValue(this, memory);
		javaClass.setOop(wrapper);
		setClass(memory, javaClass);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
	}

	@Override
	public ArrayValue newArray(ArrayClass javaClass, int length) {
		MemoryBlock memory = allocateArrayMemory(length, sizeOfType(javaClass.getComponentType()));
		setClass(memory, javaClass);
		SimpleArrayValue value = new SimpleArrayValue(this, memory);
		memory.getData().writeInt(arrayLengthOffset, length);
		objects.put(MemoryAddress.of(memory.getAddress()), value);
		return value;
	}

	@Override
	public ObjectValue readReference(ObjectValue object, long offset) {
		long address = object.getMemory().getData().readLong(offset);
		return getReference(address);
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		if (object.isNull()) {
			throw new PanicException("Null value");
		}
		ObjectValue classValue = getReference(object.getData().readLong(0L));
		return vm.getClassStorage().lookup(classValue);
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
	public InstanceValue newClassOop(JavaClass javaClass) {
		InstanceClass javaLangClass = vm.getSymbols().java_lang_Class();
		MemoryBlock memory = allocateClassMemory(javaLangClass, javaClass);
		setClass(memory, javaLangClass);
		InstanceValue wrapper = new SimpleInstanceValue(this, memory);
		objects.put(MemoryAddress.of(memory.getAddress()), wrapper);
		return wrapper;
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
	public long sizeOfType(JavaClass javaClass) {
		if (javaClass.isPrimitive()) {
			return LanguageSpecification.primitiveSize(javaClass.getSort());
		}
		return objectSize();
	}

	@Override
	public long sizeOfType(Class<?> javaClass) {
		if (javaClass.isPrimitive()) {
			return LanguageSpecification.primitiveSize(javaClass);
		}
		return objectSize();
	}

	@Override
	public int objectSize() {
		return vm.getMemoryAllocator().addressSize();
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

	private MemoryBlock allocateInstanceMemory(InstanceClass javaClass) {
		long objectSize = objectHeaderSize + javaClass.getOccupiedInstanceSpace();
		return touch(vm.getMemoryAllocator().allocateHeap(objectSize));
	}

	private MemoryBlock allocateClassMemory(InstanceClass javaLangClass, JavaClass javaClass) {
		long size = objectHeaderSize + javaLangClass.getOccupiedInstanceSpace() + (javaClass instanceof InstanceClass ? ((InstanceClass) javaClass).getOccupiedStaticSpace() : 0);
		return touch(vm.getMemoryAllocator().allocateHeap(size));
	}

	private MemoryBlock allocateArrayMemory(int length, long componentSize) {
		long size = arrayHeaderSize + (long) length * componentSize;
		return touch(vm.getMemoryAllocator().allocateHeap(size));
	}

	private MemoryBlock touch(MemoryBlock block) {
		if (block == null) {
			return null; // out of memory
		}
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
