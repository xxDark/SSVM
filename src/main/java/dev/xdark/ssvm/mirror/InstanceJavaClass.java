package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class InstanceJavaClass implements JavaClass {

	private final Map<FieldInfo, Long> virtualFields = new HashMap<>();
	private final VirtualMachine vm;
	private final Value classLoader;
	private final Lock initializationLock;
	private final Condition signal;
	private final ClassReader classReader;
	private final ClassNode node;
	private InstanceValue oop;
	private ClassLayout virtualLayout;
	private ClassLayout staticLayout;
	private InstanceJavaClass superClass;
	private InstanceJavaClass[] interfaces;
	private ArrayJavaClass arrayClass;

	// Initialization
	private volatile State state = State.PENDING;
	private volatile Thread initializer;

	// Stuff to cache
	private String normalName;
	private String descriptor;

	/**
	 * @param vm
	 * 		VM.
	 * @param classLoader
	 * 		Loader of the class.
	 * @param classReader
	 * 		Source of the class.
	 * @param node
	 * 		ASM class data.
	 * @param oop
	 * 		Clas oop.
	 */
	public InstanceJavaClass(VirtualMachine vm, Value classLoader, ClassReader classReader, ClassNode node, InstanceValue oop) {
		this.vm = vm;
		this.classLoader = classLoader;
		this.classReader = classReader;
		this.node = node;
		this.oop = oop;
		var lock = new ReentrantLock();
		initializationLock = lock;
		signal = lock.newCondition();
	}

	/**
	 * This constructor must be invoked ONLY
	 * by the VM.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param classLoader
	 * 		Loader of the class.
	 * @param classReader
	 * 		Source of the class.
	 * @param node
	 * 		ASM class data.
	 */
	public InstanceJavaClass(VirtualMachine vm, Value classLoader, ClassReader classReader, ClassNode node) {
		this(vm, classLoader, classReader, node, null);
	}

	@Override
	public String getName() {
		var normalName = this.normalName;
		if (normalName == null) {
			return this.normalName = node.name.replace('/', '.');
		}
		return normalName;
	}

	@Override
	public String getInternalName() {
		return node.name;
	}

	@Override
	public String getDescriptor() {
		var descriptor = this.descriptor;
		if (descriptor == null) {
			return this.descriptor = 'L' + node.name + ';';
		}
		return descriptor;
	}

	@Override
	public int getModifiers() {
		return node.access;
	}

	@Override
	public Value getClassLoader() {
		return classLoader;
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public void initialize() {
		var lock = initializationLock;
		lock.lock();
		if (state == State.COMPLETE) {
			lock.unlock();
			return;
		}
		if (state == State.FAILED) {
			lock.unlock();
			var vm = this.vm;
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError, getInternalName());
		}
		if (state == State.IN_PROGRESS) {
			if (Thread.currentThread() == initializer) {
				lock.unlock();
				return;
			}
			// Wait for initialization to complete
			// and invoke initialize again
			// 'cause maybe we crashed
			var signal = this.signal;
			while (true) {
				try {
					signal.wait();
					break;
				} catch (InterruptedException ignored) {
				}
			}
			lock.unlock();
			initialize();
			return;
		}
		state = State.IN_PROGRESS;
		initializer = Thread.currentThread();
		var vm = this.vm;
		var helper = vm.getHelper();
		helper.initializeStaticFields(this);
		loadSuperClass(true);
		loadInterfaces(true);
		// Build class layout
		// VM might've set it already, do not override.
		if (virtualLayout == null) {
			virtualLayout = createVirtualLayout();
		}
		if (virtualFields.isEmpty()) {
			buildVirtualFields();
		}
		var clinit = getMethod("<clinit>", "()V");
		try {
			if (clinit != null) {
				helper.invokeStatic(this, clinit, new Value[0], new Value[0]);
			}
			state = State.COMPLETE;
		} catch (VMException ex) {
			state = State.FAILED;
			var oop = ex.getOop();
			var symbols = vm.getSymbols();
			if (!symbols.java_lang_Error.isAssignableFrom(oop.getJavaClass())) {
				oop = vm.getHelper().newException(symbols.java_lang_ExceptionInInitializerError, oop);
				throw new VMException(oop);
			}
			throw ex;
		} finally {
			signal.signalAll();
			lock.unlock();
			initializer = null;
		}
	}

	@Override
	public boolean isAssignableFrom(JavaClass other) {
		if (other == null) {
			var vm = this.vm;
			vm.getHelper().throwException(vm.getSymbols().java_lang_NullPointerException);
			// keep javac happy.
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other.isPrimitive()) {
			return false;
		}
		if (other.isArray()) {
			if (isInterface()) {
				var internalName = node.name;
				return "java/io/Serializable".equals(internalName) || "java/lang/Cloneable".equals(internalName);
			} else {
				return this == vm.getSymbols().java_lang_Object;
			}
		} else if (other.isInterface()) {
			if (isInterface()) {
				var toCheck = new ArrayDeque<>(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
				return false;
			} else {
				return this == vm.getSymbols().java_lang_Object;
			}
		} else {
			var toCheck = new ArrayDeque<JavaClass>();
			if (isInterface()) {
				var superClass = other.getSuperClass();
				if (superClass != null)
					toCheck.add(superClass);
				toCheck.addAll(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) return true;
					superClass = popped.getSuperClass();
					if (superClass != null)
						toCheck.add(superClass);
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
			} else {
				var superClass = other.getSuperClass();
				if (superClass != null)
					toCheck.add(superClass);
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) return true;
					superClass = popped.getSuperClass();
					if (superClass != null)
						toCheck.add(superClass);
				}
			}
			return false;
		}
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return (node.access & Opcodes.ACC_INTERFACE) != 0;
	}

	@Override
	public JavaClass getComponentType() {
		return null;
	}

	/**
	 * Computes virtual fields.
	 */
	public void buildVirtualFields() {
		virtualFields.putAll(virtualLayout.getOffsetMap()
				.entrySet()
				.stream().filter(x -> this == x.getKey().getOwner())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	@Override
	public ClassLayout getVirtualLayout() {
		return virtualLayout;
	}

	@Override
	public ClassLayout getStaticLayout() {
		var staticLayout = this.staticLayout;
		// Build class layout
		// VM might've set it already, do not override.
		if (staticLayout == null) {
			return this.staticLayout = createStaticLayout();
		}
		return staticLayout;
	}

	@Override
	public InstanceJavaClass getSuperClass() {
		initialize();
		return superClass;
	}

	@Override
	public InstanceJavaClass[] getInterfaces() {
		initialize();
		return interfaces;
	}

	@Override
	public ArrayJavaClass newArrayClass() {
		var arrayClass = this.arrayClass;
		if (arrayClass == null) {
			var vm = this.vm;
			arrayClass = this.arrayClass = new ArrayJavaClass(vm, '[' + getDescriptor(), 1, this);
			vm.getHelper().setComponentType(arrayClass, this);
		}
		return arrayClass;
	}

	/**
	 * Returns VM instance in which this class
	 * was loaded.
	 *
	 * @return VM instance.
	 */
	public VirtualMachine getVM() {
		return vm;
	}

	/**
	 * Sets oop of the class.
	 *
	 * @param oop
	 * 		Class oop.
	 */
	public void setOop(InstanceValue oop) {
		this.oop = oop;
	}

	/**
	 * Searches for a method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public MethodNode getMethod(String name, String desc) {
		var methods = node.methods;
		for (int i = 0, j = methods.size(); i < j; i++) {
			var mn = methods.get(i);
			if (name.equals(mn.name) && desc.equals(mn.desc)) {
				return mn;
			}
		}
		return null;
	}

	/**
	 * Returns static value of a field.
	 *
	 * @param field
	 * 		Field info.
	 *
	 * @return static value of a field or {@code null},
	 * if field was not found.
	 */
	public Value getStaticValue(FieldInfo field) {
		initialize();

		var offset = (int) staticLayout.getFieldOffset(field);
		if (offset == -1L) return null;
		var oop = this.oop;
		var memoryManager = vm.getMemoryManager();
		var resultingOffset = memoryManager.getStaticOffset(this) + offset;
		switch (field.getDesc()) {
			case "J":
				return new LongValue(memoryManager.readLong(oop, resultingOffset));
			case "D":
				return new DoubleValue(memoryManager.readDouble(oop, resultingOffset));
			case "I":
				return new IntValue(memoryManager.readInt(oop, resultingOffset));
			case "F":
				return new FloatValue(memoryManager.readFloat(oop, resultingOffset));
			case "C":
				return new IntValue(memoryManager.readChar(oop, resultingOffset));
			case "S":
				return new IntValue(memoryManager.readShort(oop, resultingOffset));
			case "B":
			case "Z":
				return new IntValue(memoryManager.readByte(oop, resultingOffset));
			default:
				return memoryManager.readValue(oop, resultingOffset);
		}
	}

	/**
	 * Returns static value of a field.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return static value of a field or {@code null},
	 * if field was not found.
	 */
	public Value getStaticValue(String name, String desc) {
		return getStaticValue(new FieldInfo(this, name, desc));
	}

	/**
	 * Sets static value for a field.
	 *
	 * @param field
	 * 		Field info.
	 * @param value
	 * 		New value.
	 *
	 * @return whether the value was changed or not.
	 * This method will return {@code false} if there is no such field.
	 */
	public boolean setFieldValue(FieldInfo field, Value value) {
		initialize();
		var offset = (int) staticLayout.getFieldOffset(field);
		if (offset == -1L) return false;
		var oop = this.oop;
		var memoryManager = vm.getMemoryManager();
		var resultingOffset = memoryManager.getStaticOffset(this) + offset;
		switch (field.getDesc()) {
			case "J":
				memoryManager.writeLong(oop, resultingOffset, value.asLong());
				return true;
			case "D":
				memoryManager.writeDouble(oop, resultingOffset, value.asDouble());
				return true;
			case "I":
				memoryManager.writeInt(oop, resultingOffset, value.asInt());
				return true;
			case "F":
				memoryManager.writeFloat(oop, resultingOffset, value.asFloat());
				return true;
			case "C":
				memoryManager.writeChar(oop, resultingOffset, value.asChar());
				return true;
			case "S":
				memoryManager.writeShort(oop, resultingOffset, value.asShort());
				return true;
			case "B":
			case "Z":
				memoryManager.writeByte(oop, resultingOffset, value.asByte());
				return true;
			default:
				memoryManager.writeValue(oop, resultingOffset, (ObjectValue) value);
				return true;
		}
	}

	/**
	 * Sets static value for a field.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 * @param value
	 * 		New value.
	 *
	 * @return whether the value was changed or not.
	 * This method will return {@code false} if there is no such field.
	 */
	public boolean setFieldValue(String name, String desc, Value value) {
		return setFieldValue(new FieldInfo(this, name, desc), value);
	}

	/**
	 * Searches for field offset.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffset(String name, String desc) {
		initialize();
		return virtualLayout.getFieldOffset(new FieldInfo(this, name, desc));
	}

	/**
	 * Searches for field offset recursively.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffsetRecursively(String name, String desc) {
		initialize();
		var layout = this.virtualLayout;
		var jc = this;
		do {
			var offset = layout.getFieldOffset(new FieldInfo(jc, name, desc));
			if (offset != -1L) return offset;
		} while ((jc = jc.getSuperClass()) != null);
		return -1L;
	}

	/**
	 * Searches for field offset recursively.
	 *
	 * @param name
	 * 		Field name.
	 *
	 * @return field offset or {@code -1L} if not found.
	 */
	public long getFieldOffsetRecursively(String name) {
		initialize();
		var layout = this.virtualLayout;
		var jc = this;
		do {
			var offset = layout.getFieldOffset(jc, name);
			if (offset != -1L) return offset;
		} while ((jc = jc.getSuperClass()) != null);
		return -1L;
	}

	/**
	 * Checks whether virtual field exists.
	 *
	 * @param info
	 * 		Field info.
	 *
	 * @return {@code true} if field exists, {@code  false}
	 * otherwise.
	 */
	public boolean hasVirtualField(FieldInfo info) {
		initialize();
		return virtualFields.containsKey(info);
	}

	/**
	 * Checks whether virtual field exists.
	 *
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return {@code true} if field exists, {@code  false}
	 * otherwise.
	 */
	public boolean hasVirtualField(String name, String desc) {
		return hasVirtualField(new FieldInfo(this, name, desc));
	}

	/**
	 * Returns all virtual fields this class defines.
	 *
	 * @return virtual fields this class defines.
	 */
	public Map<FieldInfo, Long> getVirtualFields() {
		return virtualFields;
	}

	/**
	 * Sets virtual class layout.
	 *
	 * @param layout
	 * 		Layout to use.
	 */
	public void setVirtualLayout(ClassLayout layout) {
		this.virtualLayout = layout;
	}

	/**
	 * Sets static class layout.
	 *
	 * @param layout
	 * 		Layout to use.
	 */
	public void setStaticLayout(ClassLayout layout) {
		this.staticLayout = layout;
	}

	/**
	 * Builds static class layout.
	 *
	 * @return static class layout.
	 */
	public ClassLayout createStaticLayout() {
		var offsetMap = new HashMap<FieldInfo, Long>();
		var offset = 0L;
		var fields = node.fields;
		for (int i = 0, j = fields.size(); i < j; i++) {
			var field = fields.get(i);
			if ((field.access & Opcodes.ACC_STATIC) != 0) {
				var desc = field.desc;
				offsetMap.put(new FieldInfo(this, field.name, desc), offset);
				offset += UnsafeUtil.getSizeFor(desc);
			}
		}
		return new ClassLayout(Collections.unmodifiableMap(offsetMap), offset);
	}

	/**
	 * Builds virtual class layout.
	 *
	 * @return virtual class layout.
	 */
	public ClassLayout createVirtualLayout() {
		var offsetMap = new HashMap<FieldInfo, Long>();
		var deque = new ArrayDeque<InstanceJavaClass>();
		var offset = 0L;
		var javaClass = this;
		while (javaClass != null) {
			deque.addFirst(javaClass);
			javaClass = javaClass.superClass;
		}
		while ((javaClass = deque.pollFirst()) != null) {
			var fields = javaClass.node.fields;
			for (int i = 0, j = fields.size(); i < j; i++) {
				var field = fields.get(i);
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					var desc = field.desc;
					offsetMap.put(new FieldInfo(javaClass, field.name, desc), offset);
					offset += UnsafeUtil.getSizeFor(desc);
				}
			}
		}
		return new ClassLayout(Collections.unmodifiableMap(offsetMap), offset);
	}

	/**
	 * Returns ASM node.
	 *
	 * @return asm node.
	 */
	public ClassNode getNode() {
		return node;
	}

	/**
	 * Returns class source.
	 *
	 * @return class source.
	 */
	public ClassReader getClassReader() {
		return classReader;
	}

	/**
	 * Loads hierarchy of classes without marking them as resolved.
	 */
	public void loadClassesWithoutMarkingResolved() {
		var lock = this.initializationLock;
		lock.lock();
		var vm = this.vm;
		if (state == State.FAILED) {
			lock.unlock();
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError);
		}
		try {
			loadSuperClass(false);
			loadInterfaces(false);
			for (var ifc : interfaces) {
				ifc.loadClassesWithoutMarkingResolved();
			}
		} catch (VMException ex) {
			state = State.FAILED;
			throw ex;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	private void loadSuperClass(boolean initialize) {
		var superClass = this.superClass;
		if (superClass == null) {
			var vm = this.vm;
			var superName = node.superName;
			if (superName != null) {
				// Load parent class.
				superClass = (InstanceJavaClass) vm.findClass(classLoader, superName, initialize);
				if (superClass == null) {
					vm.getHelper().throwException(vm.getSymbols().java_lang_NoClassDefFoundError, superName);
				}
				this.superClass = superClass;
			}
		}
		if (initialize && superClass != null) superClass.initialize();
	}

	private void loadInterfaces(boolean initialize) {
		var $interfaces = this.interfaces;
		if ($interfaces == null) {
			var _interfaces = node.interfaces;
			$interfaces = new InstanceJavaClass[_interfaces.size()];
			var vm = this.vm;
			var classLoader = this.classLoader;
			for (int i = 0, j = _interfaces.size(); i < j; i++) {
				var iface = $interfaces[i] = (InstanceJavaClass) vm.findClass(classLoader, _interfaces.get(i), initialize);
				if (iface == null) {
					vm.getHelper().throwException(vm.getSymbols().java_lang_NoClassDefFoundError, _interfaces.get(i));
				}
			}
			this.interfaces = $interfaces;
		}
		if (initialize) {
			for (var ifc : $interfaces) ifc.initialize();
		}
	}

	private enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
