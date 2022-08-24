package dev.xdark.ssvm.memory.gc;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.SafePoint;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.FieldLayout;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Primitive mark and sweep garbage collector.
 *
 * @author xDark
 */
public class MarkAndSweepGarbageCollector implements GarbageCollector {
	protected static final byte MARK_NONE = 0;
	private static final byte MARK_SET = 1;
	private static final byte MARK_GLOBAL_REF = 2;
	private final Map<ObjectValue, GCHandle> handles = new IdentityHashMap<>();
	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public MarkAndSweepGarbageCollector(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public int reservedHeaderSize() {
		return 1;
	}

	@Override
	public synchronized GCHandle makeHandle(ObjectValue value) {
		return handles.computeIfAbsent(value, k -> new SimpleGCHandle() {
			@Override
			protected void deallocate() {
				handles.remove(k);
			}
		});
	}

	@Override
	public synchronized GCHandle getHandle(ObjectValue value) {
		return handles.get(value);
	}

	@Override
	public void makeGlobalReference(ObjectValue value) {
		value.getMemory().getData().writeByte(0, MARK_GLOBAL_REF);
	}

	@Override
	public synchronized boolean invoke() {
		/*
		VirtualMachine vm = this.vm;
		SafePoint safePoint = vm.getSafePoint();
		ThreadManager threadManager = vm.getThreadManager();
		safePoint.request();
		try {
			MemoryManager memoryManager = vm.getMemoryManager();
			Collection<ObjectValue> allObjects = memoryManager.listObjects();
			ClassLoaders classLoaders = vm.getClassLoaders();
			Collection<InstanceValue> loaderList = classLoaders.getAll();
			for (InstanceValue classLoader : loaderList) {
				setMark(classLoader);
				ClassLoaderData data = classLoaders.getClassLoaderData(classLoader);
				markClassLoaderData(data);
			}
			markClassLoaderData(vm.getBootClassLoaderData());
			VMPrimitives primitives = vm.getPrimitives();
			markClass(primitives.longPrimitive());
			markClass(primitives.doublePrimitive());
			markClass(primitives.intPrimitive());
			markClass(primitives.floatPrimitive());
			markClass(primitives.charPrimitive());
			markClass(primitives.shortPrimitive());
			markClass(primitives.bytePrimitive());
			markClass(primitives.booleanPrimitive());
			for (VMThread thread : threadManager.getThreads()) {
				setMark(thread.getOop());
				Backtrace backtrace = thread.getBacktrace();
				ThreadStorage threadStorage = thread.getThreadStorage();
				for (Value value : threadStorage) {
					tryMark(value);
				}
				for (StackFrame frame : backtrace) {
					ExecutionContext ctx = frame.getExecutionContext();
					if (ctx != null) {
						tryMark(ctx.getResult());
					}
				}
			}
			for (ObjectValue value : handles.keySet()) {
				setMark(value);
			}
			MemoryAllocator allocator = vm.getMemoryAllocator();
			// We need to do one more pass to find all
			// global references
			for (ObjectValue value : allObjects) {
				if (!value.isNull()) {
					MemoryData data = value.getMemory().getData();
					if (data.readByte(0) == MARK_GLOBAL_REF) {
						setMark(value);
					}
				}
			}
			allObjects.removeIf(x -> {
				if (x.isNull()) {
					return false;
				}
				MemoryBlock block = x.getMemory();
				MemoryData data = block.getData();
				byte mark = data.readByte(0L);
				boolean result = mark == MARK_NONE;
				if (result) {
					if (!allocator.freeHeap(block.getAddress())) {
						throw new PanicException("Failed to free heap memory");
					}
				} else if (mark != MARK_GLOBAL_REF) {
					data.writeByte(0L, MARK_NONE);
				}
				return result;
			});
			return true;
		} finally {
			safePoint.complete();
			threadManager.resumeAll();
		}
		*/
		return false;
	}

	private void markClassLoaderData(ClassLoaderData data) {
		MemoryManager memoryManager = vm.getMemoryManager();
		for (InstanceJavaClass klass : data.getAll()) {
			markClass(klass);
			if (klass.getState() != InstanceJavaClass.State.PENDING) {
				MemoryBlock memory = klass.getOop().getMemory();
				markAllFields(klass.getStaticFieldLayout(), memory.getData(), memoryManager.getStaticOffset(klass));
			}
		}
	}

	private void markClass(JavaClass klass) {
		while (klass != null) {
			setMark(klass.getOop());
			klass = klass.getArrayClass();
		}
	}

	private void tryMark(Value value) {
		if (value instanceof ObjectValue) {
			setMark((ObjectValue) value);
		}
	}

	private void setMark(ObjectValue value) {
		if (!value.isNull()) {
			MemoryData data = value.getMemory().getData();
			byte mark = data.readByte(0L);
			if (mark == MARK_SET) {
				// Already marked
				return;
			}
			if (mark != MARK_GLOBAL_REF) {
				// Don't rewrite MARK_GLOBAL_REF
				data.writeByte(0L, MARK_SET);
			}
			setMarkImpl(value);
		}
	}

	private void setMarkImpl(ObjectValue value) {
		MemoryData data = value.getMemory().getData();
		MemoryManager memoryManager = vm.getMemoryManager();
		long offset = memoryManager.valueBaseOffset(value);
		long objectSize = memoryManager.objectSize();
		if (value instanceof ArrayValue) {
			ArrayValue arrayValue = (ArrayValue) value;
			if (!arrayValue.getJavaClass().getComponentType().isPrimitive()) {
				for (int i = 0, j = arrayValue.getLength(); i < j; i++) {
					setMark(memoryManager.getReference(data.readLong(offset + objectSize * (long) i)));
				}
			}
		} else {
			InstanceValue instanceValue = (InstanceValue) value;
			InstanceJavaClass klass = instanceValue.getJavaClass();
			while (klass != null) {
				markAllFields(klass.getVirtualFieldLayout(), data, offset);
				klass = klass.getSuperclassWithoutResolving();
			}
		}
	}

	private void markAllFields(FieldLayout layout, MemoryData data, long offset) {
		MemoryManager memoryManager = vm.getMemoryManager();
		for (JavaField field : layout.getAll()) {
			if (field.getType().getSort() >= Type.ARRAY) {
				setMark(memoryManager.getReference(data.readLong(offset + field.getOffset())));
			}
		}
	}
}
