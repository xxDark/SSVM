package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import lombok.val;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	// Reflection cache.
	private List<JavaMethod> declaredConstructors;
	private List<JavaMethod> publicConstructors;
	private List<JavaMethod> declaredMethods;
	private List<JavaMethod> publicMethods;
	private List<JavaField> declaredFields;
	private List<JavaField> publicFields;

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
		val lock = new ReentrantLock();
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
		val normalName = this.normalName;
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
		val descriptor = this.descriptor;
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
		val lock = initializationLock;
		lock.lock();
		if (state == State.COMPLETE) {
			lock.unlock();
			return;
		}
		if (state == State.FAILED) {
			lock.unlock();
			val vm = this.vm;
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
			val signal = this.signal;
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
		val vm = this.vm;
		val helper = vm.getHelper();
		loadSuperClass(true);
		loadInterfaces(true);
		// Build class layout
		// VM might've set it already, do not override.
		if (vrtFieldLayout == null) {
			vrtFieldLayout = createVirtualFieldLayout();
		}
		helper.initializeStaticFields(this);
		val clinit = getStaticMethod("<clinit>", "()V");
		try {
			if (clinit != null) {
				helper.invokeStatic(this, clinit, new Value[0], new Value[0]);
			}
			state = State.COMPLETE;
		} catch (VMException ex) {
			state = State.FAILED;
			InstanceValue oop = ex.getOop();
			val symbols = vm.getSymbols();
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
			val vm = this.vm;
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
				val internalName = node.name;
				return "java/io/Serializable".equals(internalName) || "java/lang/Cloneable".equals(internalName);
			} else {
				return this == vm.getSymbols().java_lang_Object;
			}
		} else if (other.isInterface()) {
			if (isInterface()) {
				val toCheck = new ArrayDeque<>(Arrays.asList(other.getInterfaces()));
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
			val toCheck = new ArrayDeque<JavaClass>();
			if (isInterface()) {
				JavaClass superClass = other.getSuperClass();
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
				JavaClass superClass = other.getSuperClass();
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
		val staticLayout = this.staticFieldLayout;
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
		ArrayJavaClass arrayClass = this.arrayClass;
		if (arrayClass == null) {
			val vm = this.vm;
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
	public void setOop(JavaValue<InstanceJavaClass> oop) {
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
		val layout = getVirtualMethodLayout();
		val methods = layout.getMethods();
		InstanceJavaClass jc = this;
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
		InstanceJavaClass jc = this;
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
		val key = new MemberKey(this, name, desc);
		JavaMethod jm = getVirtualMethodLayout().getMethods().get(key);
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

		val offset = (int) staticFieldLayout.getFieldOffset(field);
		if (offset == -1L) return null;
		val oop = this.oop;
		val memoryManager = vm.getMemoryManager();
		val resultingOffset = memoryManager.getStaticOffset(this) + offset;
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
				return new IntValue(memoryManager.readByte(oop, resultingOffset));
			case "Z":
				return memoryManager.readBoolean(oop, resultingOffset) ? IntValue.ONE : IntValue.ZERO;
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
		val offset = (int) staticFieldLayout.getFieldOffset(field);
		if (offset == -1L) return false;
		val oop = this.oop;
		val memoryManager = vm.getMemoryManager();
		val resultingOffset = memoryManager.getStaticOffset(this) + offset;
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
		val layout = this.vrtFieldLayout;
		InstanceJavaClass jc = this;
		do {
			val offset = layout.getFieldOffset(new MemberKey(jc, name, desc));
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
		val layout = this.vrtFieldLayout;
		InstanceJavaClass jc = this;
		do {
			val offset = layout.getFieldOffset(jc, name);
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
		return vrtFieldLayout.getFields().containsKey(info);
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
		val map = new HashMap<MemberKey, JavaField>();
		long offset = 0L;
		int slot = getVirtualFieldCount();
		val fields = node.fields;
		for (val field : fields) {
			if ((field.access & Opcodes.ACC_STATIC) != 0) {
				val desc = field.desc;
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
		val map = new HashMap<MemberKey, JavaField>();
		val deque = new ArrayDeque<InstanceJavaClass>();
		long offset = 0L;
		InstanceJavaClass javaClass = this;
		while (javaClass != null) {
			deque.addFirst(javaClass);
			javaClass = javaClass.getSuperclassWithoutResolving();
		}
		int slot = 0;
		while ((javaClass = deque.pollFirst()) != null) {
			val fields = javaClass.node.fields;
			for (val field : fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					val desc = field.desc;
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
		val lock = this.initializationLock;
		lock.lock();
		val vm = this.vm;
		if (state == State.FAILED) {
			lock.unlock();
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError);
		}
		try {
			loadSuperClass(false);
			loadInterfaces(false);
			for (val ifc : interfaces) {
				ifc.loadClassesWithoutMarkingResolved();
			}
		} catch (VMException ex) {
			state = State.FAILED;
			throw ex;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns list of all methods.
	 *
	 * @param publicOnly
	 * 		Should only public methods be included.
	 *
	 * @return all methods.
	 */
	public List<JavaMethod> getDeclaredMethods(boolean publicOnly) {
		if (publicOnly) {
			val publicMethods = this.publicMethods;
			if (publicMethods == null) {
				return this.publicMethods = getDeclaredMethods0(true, false);
			}
			return publicMethods;
		}
		val declaredMethods = this.declaredMethods;
		if (declaredMethods == null) {
			return this.declaredMethods = getDeclaredMethods0(false, false);
		}
		return declaredMethods;
	}

	/**
	 * Returns list of all constructors.
	 *
	 * @param publicOnly
	 * 		Should only public constructors be included.
	 *
	 * @return all constructors.
	 */
	public List<JavaMethod> getDeclaredConstructors(boolean publicOnly) {
		if (publicOnly) {
			val publicConstructors = this.publicConstructors;
			if (publicConstructors == null) {
				return this.publicConstructors = getDeclaredMethods0(true, true);
			}
			return publicConstructors;
		}
		val declaredConstructors = this.declaredConstructors;
		if (declaredConstructors == null) {
			return this.declaredConstructors = getDeclaredMethods0(false, true);
		}
		return declaredConstructors;
	}

	/**
	 * Returns list of all fields.
	 *
	 * @param publicOnly
	 * 		Should only public fields be included.
	 *
	 * @return all fields.
	 */
	public List<JavaField> getDeclaredFields(boolean publicOnly) {
		if (publicOnly) {
			val publicFields = this.publicFields;
			if (publicFields == null) {
				return this.publicFields = getDeclaredFields0(true);
			}
			return publicFields;
		}
		val declaredFields = this.declaredFields;
		if (declaredFields == null) {
			return this.declaredFields = getDeclaredFields0(false);
		}
		return declaredFields;
	}

	@Override
	public String toString() {
		return getName();
	}

	private List<JavaMethod> getDeclaredMethods0(boolean publicOnly, boolean constructors) {
		val staticMethods = constructors ? Stream.<JavaMethod>empty() : getStaticMethods0(publicOnly);
		return Stream.concat(staticMethods, getVirtualMethodLayout()
						.getMethods()
						.values()
						.stream()
						.filter(x -> this == x.getOwner())
						.filter(x -> constructors == "<init>".equals(x.getName()))
						.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0))
				.collect(Collectors.toList());
	}

	private Stream<JavaMethod> getStaticMethods0(boolean publicOnly) {
		return getStaticMethodLayout()
				.getMethods()
				.values()
				.stream()
				.filter(x -> this == x.getOwner())
				.filter(x -> !"<clinit>".equals(x.getName()))
				.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0);
	}

	private List<JavaField> getDeclaredFields0(boolean publicOnly) {
		val staticFields = getDeclaredStaticFields0(publicOnly);
		return Stream.concat(staticFields, getVirtualFieldLayout()
						.getFields()
						.values()
						.stream()
						.filter(x -> this == x.getOwner())
						.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0))
				.collect(Collectors.toList());
	}

	private Stream<JavaField> getDeclaredStaticFields0(boolean publicOnly) {
		return getStaticFieldLayout()
				.getFields()
				.values()
				.stream()
				.filter(x -> this == x.getOwner())
				.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0);
	}

	private void loadSuperClass(boolean initialize) {
		InstanceJavaClass superClass = this.superClass;
		if (superClass == null) {
			val vm = this.vm;
			val superName = node.superName;
			if (superName != null) {
				// Load parent class.
				superClass = (InstanceJavaClass) vm.findClass(classLoader, superName, initialize);
				this.superClass = superClass;
			}
		}
		if (initialize && superClass != null) superClass.initialize();
	}

	private void loadInterfaces(boolean initialize) {
		InstanceJavaClass[] $interfaces = this.interfaces;
		if ($interfaces == null) {
			val _interfaces = node.interfaces;
			$interfaces = new InstanceJavaClass[_interfaces.size()];
			val vm = this.vm;
			val classLoader = this.classLoader;
			for (int i = 0, j = _interfaces.size(); i < j; i++) {
				val iface = $interfaces[i] = (InstanceJavaClass) vm.findClass(classLoader, _interfaces.get(i), initialize);
				if (iface == null) {
					vm.getHelper().throwException(vm.getSymbols().java_lang_NoClassDefFoundError, _interfaces.get(i));
				}
			}
			this.interfaces = $interfaces;
		}
		if (initialize) {
			for (val ifc : $interfaces) ifc.initialize();
		}
	}

	private int getVirtualFieldCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			for (val field : jc.node.fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	/**
	 * Returns virtual method layout.
	 *
	 * @return virtual method layout.
	 */
	public MethodLayout getVirtualMethodLayout() {
		MethodLayout vrtMethodLayout = this.vrtMethodLayout;
		if (vrtMethodLayout == null) {
			val map = new HashMap<MemberKey, JavaMethod>();
			val deque = new ArrayDeque<InstanceJavaClass>();
			InstanceJavaClass javaClass = this;
			while (javaClass != null) {
				deque.addFirst(javaClass);
				javaClass = javaClass.getSuperclassWithoutResolving();
			}
			int slot = 0;
			while ((javaClass = deque.pollFirst()) != null) {
				val methods = javaClass.node.methods;
				for (val method : methods) {
					if ((method.access & Opcodes.ACC_STATIC) == 0) {
						val desc = method.desc;
						map.put(new MemberKey(javaClass, method.name, desc), new JavaMethod(javaClass, method, slot++));
					}
				}
			}
			vrtMethodLayout = new MethodLayout(Collections.unmodifiableMap(map));
			this.vrtMethodLayout = vrtMethodLayout;
		}
		return vrtMethodLayout;
	}

	/**
	 * Returns static method layout.
	 *
	 * @return static method layout.
	 */
	public MethodLayout getStaticMethodLayout() {
		MethodLayout staticMethodLayout = this.staticMethodLayout;
		if (staticMethodLayout == null) {
			val map = new HashMap<MemberKey, JavaMethod>();
			int slot = getVirtualMethodCount();
			val methods = node.methods;
			for (val method : methods) {
				if ((method.access & Opcodes.ACC_STATIC) != 0) {
					val desc = method.desc;
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
		InstanceJavaClass jc = this;
		do {
			for (val field : jc.node.methods) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private InstanceJavaClass getSuperclassWithoutResolving() {
		val superName = node.superName;
		if (superName == null) return null;
		val vm = this.vm;
		return (InstanceJavaClass) vm.findClass(classLoader, superName, false);
	}

	private enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
