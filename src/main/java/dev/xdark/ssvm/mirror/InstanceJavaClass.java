package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class InstanceJavaClass implements JavaClass {

	private final VirtualMachine vm;
	private final Value classLoader;
	private final Lock initializationLock;
	private final Condition signal;
	private final ClassReader classReader;
	private final ClassNode node;
	private InstanceValue oop;
	private FieldLayout vrtFieldLayout;
	private FieldLayout staticFieldLayout;
	private MethodLayout vrtMethodLayout;
	private MethodLayout staticMethodLayout;
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
		loadSuperClass(true);
		loadInterfaces(true);
		// Build class layout
		// VM might've set it already, do not override.
		if (vrtFieldLayout == null) {
			vrtFieldLayout = createVirtualFieldLayout();
		}
		helper.initializeStaticFields(this);
		var clinit = getStaticMethod("<clinit>", "()V");
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

	@Override
	public FieldLayout getVirtualFieldLayout() {
		return vrtFieldLayout;
	}

	@Override
	public FieldLayout getStaticFieldLayout() {
		var staticLayout = this.staticFieldLayout;
		// Build class layout
		// VM might've set it already, do not override.
		if (staticLayout == null) {
			return this.staticFieldLayout = createStaticFieldLayout();
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
	 * Searches for a virtual method by it's name and descriptor recursively.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public JavaMethod getVirtualMethodRecursively(String name, String desc) {
		var layout = getVirtualMethodLayout();
		var methods = layout.getMethods();
		var jc = this;
		JavaMethod method;
		do {
			method = methods.get(new MemberKey(jc, name, desc));
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return method;
	}

	/**
	 * Searches for a virtual method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public JavaMethod getVirtualMethod(String name, String desc) {
		return getVirtualMethodLayout().getMethods().get(new MemberKey(this, name, desc));
	}

	/**
	 * Searches for a static method by it's name and descriptor recursively.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public JavaMethod getStaticMethodRecursively(String name, String desc) {
		var jc = this;
		JavaMethod method;
		do {
			method = jc.getStaticMethodLayout().getMethods().get(new MemberKey(jc, name, desc));
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return method;
	}

	/**
	 * Searches for a static method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public JavaMethod getStaticMethod(String name, String desc) {
		return getStaticMethodLayout().getMethods().get(new MemberKey(this, name, desc));
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
	public JavaMethod getMethod(String name, String desc) {
		var key = new MemberKey(this, name, desc);
		var jm = getVirtualMethodLayout().getMethods().get(key);
		if (jm == null) {
			jm = getStaticMethodLayout().getMethods().get(key);
		}
		return jm;
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
	public Value getStaticValue(MemberKey field) {
		initialize();

		var offset = (int) staticFieldLayout.getFieldOffset(field);
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
		return getStaticValue(new MemberKey(this, name, desc));
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
	public boolean setFieldValue(MemberKey field, Value value) {
		initialize();
		var offset = (int) staticFieldLayout.getFieldOffset(field);
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
		return setFieldValue(new MemberKey(this, name, desc), value);
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
		return vrtFieldLayout.getFieldOffset(new MemberKey(this, name, desc));
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
		var layout = this.vrtFieldLayout;
		var jc = this;
		do {
			var offset = layout.getFieldOffset(new MemberKey(jc, name, desc));
			if (offset != -1L) return offset;
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
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
		var layout = this.vrtFieldLayout;
		var jc = this;
		do {
			var offset = layout.getFieldOffset(jc, name);
			if (offset != -1L) return offset;
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
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
	public boolean hasVirtualField(MemberKey info) {
		initialize();
		return vrtFieldLayout.getFieldMap().containsKey(info);
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
		return hasVirtualField(new MemberKey(this, name, desc));
	}

	/**
	 * Sets virtual class layout.
	 *
	 * @param layout
	 * 		Layout to use.
	 */
	public void setVirtualFieldLayout(FieldLayout layout) {
		this.vrtFieldLayout = layout;
	}

	/**
	 * Sets static class layout.
	 *
	 * @param layout
	 * 		Layout to use.
	 */
	public void setStaticFieldLayout(FieldLayout layout) {
		this.staticFieldLayout = layout;
	}

	/**
	 * Builds static class layout.
	 *
	 * @return static class layout.
	 */
	public FieldLayout createStaticFieldLayout() {
		var map = new HashMap<MemberKey, JavaField>();
		var offset = 0L;
		int slot = getVirtualFieldCount();
		var fields = node.fields;
		for (var field : fields) {
			if ((field.access & Opcodes.ACC_STATIC) != 0) {
				var desc = field.desc;
				map.put(new MemberKey(this, field.name, desc), new JavaField(this, field, slot++, offset));
				offset += UnsafeUtil.getSizeFor(desc);
			}
		}
		return new FieldLayout(Collections.unmodifiableMap(map), offset);
	}

	/**
	 * Builds virtual class layout.
	 *
	 * @return virtual class layout.
	 */
	public FieldLayout createVirtualFieldLayout() {
		var map = new HashMap<MemberKey, JavaField>();
		var deque = new ArrayDeque<InstanceJavaClass>();
		var offset = 0L;
		var javaClass = this;
		while (javaClass != null) {
			deque.addFirst(javaClass);
			javaClass = javaClass.getSuperclassWithoutResolving();
		}
		int slot = 0;
		while ((javaClass = deque.pollFirst()) != null) {
			var fields = javaClass.node.fields;
			for (var field : fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					var desc = field.desc;
					map.put(new MemberKey(javaClass, field.name, desc), new JavaField(javaClass, field, slot++, offset));
					offset += UnsafeUtil.getSizeFor(desc);
				}
			}
		}
		return new FieldLayout(Collections.unmodifiableMap(map), offset);
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

	private int getVirtualFieldCount() {
		int count = 0;
		var jc = this;
		do {
			for (var field : jc.node.fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private MethodLayout getVirtualMethodLayout() {
		var vrtMethodLayout = this.vrtMethodLayout;
		if (vrtMethodLayout == null) {
			var map = new HashMap<MemberKey, JavaMethod>();
			var deque = new ArrayDeque<InstanceJavaClass>();
			var javaClass = this;
			while (javaClass != null) {
				deque.addFirst(javaClass);
				javaClass = javaClass.getSuperclassWithoutResolving();
			}
			int slot = 0;
			while ((javaClass = deque.pollFirst()) != null) {
				var methods = javaClass.node.methods;
				for (var method : methods) {
					if ((method.access & Opcodes.ACC_STATIC) == 0) {
						var desc = method.desc;
						map.put(new MemberKey(javaClass, method.name, desc), new JavaMethod(javaClass, method, slot++));
					}
				}
			}
			vrtMethodLayout = new MethodLayout(Collections.unmodifiableMap(map));
			this.vrtMethodLayout = vrtMethodLayout;
		}
		return vrtMethodLayout;
	}

	private MethodLayout getStaticMethodLayout() {
		var staticMethodLayout = this.staticMethodLayout;
		if (staticMethodLayout == null) {
			var map = new HashMap<MemberKey, JavaMethod>();
			int slot = getVirtualMethodCount();
			var methods = node.methods;
			for (var method : methods) {
				if ((method.access & Opcodes.ACC_STATIC) != 0) {
					var desc = method.desc;
					map.put(new MemberKey(this, method.name, desc), new JavaMethod(this, method, slot++));
				}
			}
			staticMethodLayout = new MethodLayout(Collections.unmodifiableMap(map));
			this.staticMethodLayout = staticMethodLayout;
		}
		return staticMethodLayout;
	}

	private int getVirtualMethodCount() {
		int count = 0;
		var jc = this;
		do {
			for (var field : jc.node.methods) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private InstanceJavaClass getSuperclassWithoutResolving() {
		var superName = node.superName;
		if (superName == null) return null;
		var vm = this.vm;
		var jc = (InstanceJavaClass) vm.findClass(classLoader, superName, false);
		if (jc == null) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_NoClassDefFoundError, superName);
		}
		return jc;
	}

	private enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
