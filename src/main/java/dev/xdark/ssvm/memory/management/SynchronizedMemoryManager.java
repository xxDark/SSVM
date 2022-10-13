package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
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
	public InstanceValue tryNewInstance(InstanceClass javaClass) {
		synchronized (mutex) {
			return memoryManager.tryNewInstance(javaClass);
		}
	}

	@Override
	public InstanceValue newInstance(InstanceClass javaClass) {
		synchronized (mutex) {
			return memoryManager.newInstance(javaClass);
		}
	}

	@Override
	public <V> JavaValue<V> tryNewJavaInstance(InstanceClass javaClass, V value) {
		synchronized (mutex) {
			return memoryManager.tryNewJavaInstance(javaClass, value);
		}
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceClass javaClass, V value) {
		synchronized (mutex) {
			return memoryManager.newJavaInstance(javaClass, value);
		}
	}

	@Override
	public JavaValue<InstanceClass> newJavaLangClass(InstanceClass javaClass) {
		synchronized (mutex) {
			return memoryManager.newJavaLangClass(javaClass);
		}
	}

	@Override
	public ArrayValue newArray(ArrayClass javaClass, int length) {
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
	public InstanceValue newClassOop(JavaClass javaClass) {
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
	public long sizeOfType(JavaClass javaClass) {
		return memoryManager.sizeOfType(javaClass);
	}

	@Override
	public long sizeOfType(Class<?> javaClass) {
		return memoryManager.sizeOfType(javaClass);
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
