package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Collection;

/**
 * Memory allocator that blocks on object allocation request.
 *
 * @author xDark
 */
public class SynchronizedMemoryManager implements MemoryManager {
	protected final MemoryManager memoryManager;
	protected final Object mutex;

	/**
	 * @param memoryManager Backing manager.
	 * @param mutex         Memory mutex.
	 */
	public SynchronizedMemoryManager(MemoryManager memoryManager, Object mutex) {
		this.memoryManager = memoryManager;
		this.mutex = mutex;
	}

	/**
	 * @param memoryManager Backing manager.
	 */
	public SynchronizedMemoryManager(MemoryManager memoryManager) {
		this.memoryManager = memoryManager;
		this.mutex = this;
	}

	@Override
	public ObjectValue nullValue() {
		return memoryManager.nullValue();
	}

	@Override
	public Mutex getMutex(ObjectValue reference) {
		synchronized (mutex) {
			return memoryManager.getMutex(reference);
		}
	}

	@Override
	public ObjectValue getReference(long address) {
		synchronized (mutex) {
			return memoryManager.getReference(address);
		}
	}

	@Override
	public InstanceValue tryNewInstance(InstanceJavaClass javaClass) {
		synchronized (mutex) {
			return memoryManager.tryNewInstance(javaClass);
		}
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		synchronized (mutex) {
			return memoryManager.newInstance(javaClass);
		}
	}

	@Override
	public <V> JavaValue<V> tryNewJavaInstance(InstanceJavaClass javaClass, V value) {
		synchronized (mutex) {
			return memoryManager.tryNewJavaInstance(javaClass, value);
		}
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value) {
		synchronized (mutex) {
			return memoryManager.newJavaInstance(javaClass, value);
		}
	}

	@Override
	public JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass) {
		synchronized (mutex) {
			return memoryManager.newJavaLangClass(javaClass);
		}
	}

	@Override
	public ArrayValue tryNewArray(ArrayJavaClass javaClass, int length) {
		synchronized (mutex) {
			return memoryManager.tryNewArray(javaClass, length);
		}
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length) {
		synchronized (mutex) {
			return memoryManager.newArray(javaClass, length);
		}
	}

	@Override
	public ObjectValue readReference(ObjectValue object, long offset) {
		synchronized (mutex) {
			return memoryManager.readReference(object, offset);
		}
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		return memoryManager.readClass(object);
	}

	@Override
	public void writeValue(ObjectValue object, long offset, ObjectValue value) {
		synchronized (mutex) {
			memoryManager.writeValue(object, offset, value);
		}
	}

	@Override
	public ObjectValue getAndWriteValue(ObjectValue object, long offset, ObjectValue value) {
		synchronized (mutex) {
			return memoryManager.getAndWriteValue(object, offset, value);
		}
	}

	@Override
	public int readArrayLength(ArrayValue array) {
		return memoryManager.readArrayLength(array);
	}

	@Override
	public <C extends JavaClass> JavaValue<C> tryNewClassOop(C javaClass) {
		synchronized (mutex) {
			return memoryManager.tryNewClassOop(javaClass);
		}
	}

	@Override
	public <C extends JavaClass> JavaValue<C> newClassOop(C javaClass) {
		synchronized (mutex) {
			return memoryManager.newClassOop(javaClass);
		}
	}

	@Override
	public int valueBaseOffset(ObjectValue value) {
		return memoryManager.valueBaseOffset(value);
	}

	@Override
	public int valueBaseOffset(JavaClass value) {
		return memoryManager.valueBaseOffset(value);
	}

	@Override
	public int arrayBaseOffset(JavaClass javaClass) {
		return memoryManager.arrayBaseOffset(javaClass);
	}

	@Override
	public int arrayBaseOffset(ArrayValue array) {
		return memoryManager.arrayBaseOffset(array);
	}

	@Override
	public int arrayBaseOffset(Class<?> javaClass) {
		return memoryManager.arrayBaseOffset(javaClass);
	}

	@Override
	public int sizeOfType(JavaClass javaClass) {
		return memoryManager.sizeOfType(javaClass);
	}

	@Override
	public int sizeOfType(Class<?> javaClass) {
		return memoryManager.sizeOfType(javaClass);
	}

	@Override
	public int longSize() {
		return memoryManager.longSize();
	}

	@Override
	public int doubleSize() {
		return memoryManager.doubleSize();
	}

	@Override
	public int intSize() {
		return memoryManager.intSize();
	}

	@Override
	public int floatSize() {
		return memoryManager.floatSize();
	}

	@Override
	public int charSize() {
		return memoryManager.charSize();
	}

	@Override
	public int shortSize() {
		return memoryManager.shortSize();
	}

	@Override
	public int byteSize() {
		return memoryManager.byteSize();
	}

	@Override
	public int booleanSize() {
		return memoryManager.booleanSize();
	}

	@Override
	public int objectSize() {
		return memoryManager.objectSize();
	}

	@Override
	public long getStaticOffset(JavaClass jc) {
		return memoryManager.getStaticOffset(jc);
	}

	@Override
	public Collection<ObjectValue> listObjects() {
		return memoryManager.listObjects();
	}

	@Override
	public void writeDefaults(ObjectValue value) {
		memoryManager.writeDefaults(value);
	}
}
