package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.tlc.ThreadLocalStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleInstanceJavaClass implements InstanceJavaClass {

	private static final String POLYMORPHIC_DESC = "([Ljava/lang/Object;)Ljava/lang/Object;";
	private static final Predicate<JavaField> NON_HIDDEN_FIELD = nonHidden(JavaField::getAccess);
	private static final Predicate<JavaMethod> NON_HIDDEN_METHOD = nonHidden(JavaMethod::getAccess);

	private final VirtualMachine vm;
	private final ObjectValue classLoader;

	private final Lock initializationLock;
	private final Condition signal;

	private ClassReader classReader;
	private ClassNode node;
	private ClassFile rawClassFile;

	private InstanceValue oop;

	private FieldLayout vrtFieldLayout;
	private FieldLayout staticFieldLayout;
	private JavaField[] fieldArray;

	private MethodLayout vrtMethodLayout;
	private MethodLayout staticMethodLayout;
	private JavaMethod[] methodArray;

	private InstanceJavaClass superClass;
	private InstanceJavaClass[] interfaces;
	private volatile ArrayJavaClass arrayClass;

	private volatile State state = State.PENDING;

	// Stuff to cache
	private String normalName;
	private String descriptor;

	// Reflection cache
	private List<JavaMethod> declaredConstructors;
	private List<JavaMethod> publicConstructors;
	private List<JavaMethod> declaredMethods;
	private List<JavaMethod> publicMethods;
	private List<JavaField> declaredFields;
	private List<JavaField> publicFields;
	private Boolean allocationStatus;

	/**
	 * This constructor must be invoked ONLY
	 * by the VM.
	 *
	 * @param vm          VM.
	 * @param classLoader Loader of the class.
	 * @param classReader Source of the class.
	 * @param node        ASM class data.
	 * @param oop         Clas oop.
	 */
	public SimpleInstanceJavaClass(VirtualMachine vm, ObjectValue classLoader, ClassReader classReader, ClassNode node, InstanceValue oop) {
		this.vm = vm;
		this.classLoader = classLoader;
		this.classReader = classReader;
		this.node = node;
		this.oop = oop;
		ReentrantLock lock = new ReentrantLock();
		initializationLock = lock;
		signal = lock.newCondition();
	}

	/**
	 * This constructor must be invoked ONLY
	 * by the VM.
	 *
	 * @param vm          VM instance.
	 * @param classLoader Loader of the class.
	 * @param classReader Source of the class.
	 * @param node        ASM class data.
	 */
	public SimpleInstanceJavaClass(VirtualMachine vm, ObjectValue classLoader, ClassReader classReader, ClassNode node) {
		this(vm, classLoader, classReader, node, null);
	}

	@Override
	public String getName() {
		String normalName = this.normalName;
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
		String descriptor = this.descriptor;
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
	public ObjectValue getClassLoader() {
		return classLoader;
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public void initialize() {
		Lock lock = initializationLock;
		lock.lock();
		State state = this.state;
		if (state == State.COMPLETE || state == State.IN_PROGRESS) {
			lock.unlock();
			return;
		}
		if (state == State.FAILED) {
			lock.unlock();
			VirtualMachine vm = this.vm;
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError(), getInternalName());
		}
		this.state = State.IN_PROGRESS;
		VirtualMachine vm = this.vm;
		VMHelper helper = vm.getHelper();
		// Build class layout
		// VM might've set it already, do not override.
		if (vrtFieldLayout == null) {
			vrtFieldLayout = createVirtualFieldLayout();
		}
		// Initialize all hierarchy
		InstanceJavaClass superClass = this.superClass;
		if (superClass != null) {
			superClass.initialize();
		}
		// note: interfaces are *not* initialized here
		helper.initializeStaticFields(this);
		helper.setupHiddenFrames(this);
		JavaMethod clinit = getStaticMethod("<clinit>", "()V");
		try {
			if (clinit != null) {
				Locals locals = vm.getThreadStorage().newLocals(clinit);
				helper.invoke(clinit, locals);
			}
			this.state = State.COMPLETE;
		} catch (VMException ex) {
			markFailedInitialization(ex);
		} finally {
			signal.signalAll();
			lock.unlock();
		}
	}

	@Override
	public void link() {
		Lock lock = initializationLock;
		lock.lock();
		state = State.IN_PROGRESS;
		try {
			try {
				loadSuperClass();
				loadInterfaces();
			} catch (VMException ex) {
				markFailedInitialization(ex);
			}
			state = State.PENDING;
		} finally {
			signal.signalAll();
			lock.unlock();
		}
	}

	@Override
	public boolean isAssignableFrom(JavaClass other) {
		if (other == null) {
			VirtualMachine vm = this.vm;
			vm.getHelper().throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		if (other == this) {
			return true;
		}
		if (other.isPrimitive()) {
			return false;
		}
		VMSymbols symbols = vm.getSymbols();
		if (other.isArray()) {
			if (isInterface()) {
				return this == symbols.java_io_Serializable() || this == symbols.java_lang_Cloneable();
			} else {
				return this == symbols.java_lang_Object();
			}
		}
		if (this == symbols.java_lang_Object()) {
			return true;
		}
		if (other.isInterface()) {
			if (isInterface()) {
				Deque<InstanceJavaClass> toCheck = new ArrayDeque<>(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
			}
		} else {
			Deque<JavaClass> toCheck = new ArrayDeque<>();
			JavaClass superClass = other.getSuperClass();
			if (superClass != null) {
				toCheck.add(superClass);
			}
			if (isInterface()) {
				toCheck.addAll(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					superClass = popped.getSuperClass();
					if (superClass != null) {
						toCheck.add(superClass);
					}
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
			} else {
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					superClass = popped.getSuperClass();
					if (superClass != null) {
						toCheck.add(superClass);
					}
				}
			}
		}
		return false;
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
		FieldLayout vrtFieldLayout = this.vrtFieldLayout;
		// Build class layout
		// VM might've set it already, do not override.
		if (vrtFieldLayout == null) {
			return this.vrtFieldLayout = createVirtualFieldLayout();
		}
		return vrtFieldLayout;
	}

	@Override
	public FieldLayout getStaticFieldLayout() {
		FieldLayout staticLayout = this.staticFieldLayout;
		// Build class layout
		// VM might've set it already, do not override.
		if (staticLayout == null) {
			return this.staticFieldLayout = createStaticFieldLayout();
		}
		return staticLayout;
	}

	@Override
	public InstanceJavaClass getSuperClass() {
		return superClass;
	}

	@Override
	public InstanceJavaClass[] getInterfaces() {
		return interfaces;
	}

	@Override
	public ArrayJavaClass newArrayClass() {
		ArrayJavaClass arrayClass = this.arrayClass;
		if (arrayClass == null) {
			synchronized (this) {
				arrayClass = this.arrayClass;
				if (arrayClass == null) {
					VirtualMachine vm = this.vm;
					arrayClass = this.arrayClass = new ArrayJavaClass(vm, '[' + getDescriptor(), 1, this);
					vm.getHelper().setComponentType(arrayClass, this);
				}
			}
		}
		return arrayClass;
	}

	@Override
	public ArrayJavaClass getArrayClass() {
		return arrayClass;
	}

	@Override
	public VirtualMachine getVM() {
		return vm;
	}

	@Override
	public void setOop(JavaValue<InstanceJavaClass> oop) {
		this.oop = oop;
	}

	@Override
	public JavaMethod getVirtualMethodRecursively(String name, String desc) {
		InstanceJavaClass jc = this;
		JavaMethod method;
		do {
			method = jc.getVirtualMethod(name, desc);
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return method;
	}

	@Override
	public JavaMethod getInterfaceMethodRecursively(String name, String desc) {
		InstanceJavaClass jc = this;
		JavaMethod method;
		Deque<InstanceJavaClass> deque = new ArrayDeque<InstanceJavaClass>();
		do {
			method = jc.getVirtualMethod(name, desc);
			deque.push(jc);
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		if (method == null) {
			search:
			while ((jc = deque.poll()) != null) {
				method = jc.getVirtualMethod(name, desc);
				if (method != null) {
					break;
				}
				for (InstanceJavaClass iface : jc.getInterfaces()) {
					method = iface.getVirtualMethod(name, desc);
					if (method != null) {
						break search;
					}
					deque.addAll(Arrays.asList(iface.getInterfaces()));
				}
			}
		}
		return method;
	}

	@Override
	public JavaMethod getVirtualMethod(String name, String desc) {
		return lookupMethodIn(getVirtualMethodLayout(), name, desc);
	}

	@Override
	public JavaField getVirtualField(String name, String desc) {
		return getVirtualFieldLayout().getFields().get(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public JavaField getVirtualFieldRecursively(String name, String desc) {
		InstanceJavaClass jc = this;
		JavaField field;
		do {
			field = jc.getVirtualField(name, desc);
		} while (field == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return field;
	}

	@Override
	public JavaField getStaticField(String name, String desc) {
		return getStaticFieldLayout().getFields().get(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public JavaField getStaticFieldRecursively(String name, String desc) {
		InstanceJavaClass jc = this;
		JavaField field;
		do {
			field = jc.getStaticField(name, desc);
		} while (field == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return field;
	}

	@Override
	public JavaMethod getStaticMethodRecursively(String name, String desc) {
		InstanceJavaClass jc = this;
		JavaMethod method;
		do {
			method = jc.getStaticMethod(name, desc);
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		return method;
	}

	@Override
	public JavaMethod getStaticMethod(String name, String desc) {
		return lookupMethodIn(getStaticMethodLayout(), name, desc);
	}

	@Override
	public JavaMethod getMethod(String name, String desc) {
		MemberKey key = new SimpleMemberKey(this, name, desc);
		JavaMethod jm = lookupMethodIn(getVirtualMethodLayout(), key);
		if (jm == null) {
			jm = lookupMethodIn(getStaticMethodLayout(), key);
		}
		return jm;
	}

	@Override
	public long getStaticFieldOffset(MemberKey field) {
		initialize();
		return staticFieldLayout.getFieldOffset(field);
	}

	@Override
	public long getStaticFieldOffset(String name, String desc) {
		return getStaticFieldOffset(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public Value getStaticValue(MemberKey field) {
		initialize();

		long offset = staticFieldLayout.getFieldOffset(field);
		if (offset == -1L) {
			return null;
		}
		MemoryManager memoryManager = vm.getMemoryManager();
		long resultingOffset = memoryManager.getStaticOffset(this) + offset;
		return vm.getOperations().readGenericValue(oop, field.getDesc(), resultingOffset);
	}

	@Override
	public Value getStaticValue(String name, String desc) {
		return getStaticValue(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public boolean setStaticFieldValue(MemberKey field, Value value) {
		initialize();
		long offset = staticFieldLayout.getFieldOffset(field);
		if (offset == -1L) {
			return false;
		}
		MemoryManager memoryManager = vm.getMemoryManager();
		long resultingOffset = memoryManager.getStaticOffset(this) + offset;
		vm.getOperations().writeGenericValue(oop, field.getDesc(), value, resultingOffset);
		return true;
	}

	@Override
	public boolean setStaticFieldValue(String name, String desc, Value value) {
		return setStaticFieldValue(new SimpleMemberKey(this, name, desc), value);
	}

	@Override
	public long getVirtualFieldOffset(String name, String desc) {
		initialize();
		return vrtFieldLayout.getFieldOffset(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public long getVirtualFieldOffsetRecursively(String name, String desc) {
		initialize();
		FieldLayout layout = this.vrtFieldLayout;
		InstanceJavaClass jc = this;
		do {
			long offset = layout.getFieldOffset(new SimpleMemberKey(jc, name, desc));
			if (offset != -1L) {
				return offset;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return -1L;
	}

	@Override
	public long getVirtualFieldOffsetRecursively(String name) {
		initialize();
		FieldLayout layout = this.vrtFieldLayout;
		InstanceJavaClass jc = this;
		do {
			long offset = layout.getFieldOffset(jc, name);
			if (offset != -1L) {
				return offset;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return -1L;
	}

	@Override
	public boolean hasVirtualField(MemberKey info) {
		initialize();
		return vrtFieldLayout.getFields().containsKey(info);
	}

	@Override
	public boolean hasVirtualField(String name, String desc) {
		return hasVirtualField(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public ClassNode getNode() {
		return node;
	}

	@Override
	public ClassReader getClassReader() {
		return classReader;
	}

	@Override
	public void loadNoResolve() {
		Lock lock = this.initializationLock;
		lock.lock();
		VirtualMachine vm = this.vm;
		if (state == State.FAILED) {
			lock.unlock();
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError());
		}
		try {
			loadSuperClass();
			loadInterfaces();
			for (InstanceJavaClass ifc : interfaces) {
				ifc.loadNoResolve();
			}
		} catch (VMException ex) {
			state = State.FAILED;
			throw ex;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public JavaMethod getMethodBySlot(int slot) {
		JavaMethod[] methodArray = this.methodArray;
		if (methodArray == null) {
			Collection<JavaMethod> virtualMethods = getVirtualMethodLayout().getAll();
			Collection<JavaMethod> staticMethods = getStaticMethodLayout().getAll();
			methodArray = new JavaMethod[getTotalMethodCount()];
			for (JavaMethod m : virtualMethods) {
				methodArray[m.getSlot()] = m;
			}
			for (JavaMethod m : staticMethods) {
				methodArray[m.getSlot()] = m;
			}
			this.methodArray = methodArray;
		}
		return slot < 0 || slot >= methodArray.length ? null : methodArray[slot];
	}

	@Override
	public JavaField getFieldBySlot(int slot) {
		JavaField[] fieldArray = this.fieldArray;
		if (fieldArray == null) {
			Collection<JavaField> virtualFields = getVirtualFieldLayout().getAll();
			Collection<JavaField> staticFields = getStaticFieldLayout().getAll();
			fieldArray = new JavaField[getTotalFieldCount()];
			for (JavaField f : virtualFields) {
				fieldArray[f.getSlot()] = f;
			}
			for (JavaField f : staticFields) {
				fieldArray[f.getSlot()] = f;
			}
			this.fieldArray = fieldArray;
		}
		return slot < 0 || slot >= fieldArray.length ? null : fieldArray[slot];
	}

	@Override
	public List<JavaMethod> getDeclaredMethods(boolean publicOnly) {
		if (publicOnly) {
			List<JavaMethod> publicMethods = this.publicMethods;
			if (publicMethods == null) {
				return this.publicMethods = getDeclaredMethods0(true, false);
			}
			return publicMethods;
		}
		List<JavaMethod> declaredMethods = this.declaredMethods;
		if (declaredMethods == null) {
			return this.declaredMethods = getDeclaredMethods0(false, false);
		}
		return declaredMethods;
	}

	@Override
	public List<JavaMethod> getDeclaredConstructors(boolean publicOnly) {
		if (publicOnly) {
			List<JavaMethod> publicConstructors = this.publicConstructors;
			if (publicConstructors == null) {
				return this.publicConstructors = getDeclaredMethods0(true, true);
			}
			return publicConstructors;
		}
		List<JavaMethod> declaredConstructors = this.declaredConstructors;
		if (declaredConstructors == null) {
			return this.declaredConstructors = getDeclaredMethods0(false, true);
		}
		return declaredConstructors;
	}

	@Override
	public List<JavaField> getDeclaredFields(boolean publicOnly) {
		if (publicOnly) {
			List<JavaField> publicFields = this.publicFields;
			if (publicFields == null) {
				return this.publicFields = getDeclaredFields0(true);
			}
			return publicFields;
		}
		List<JavaField> declaredFields = this.declaredFields;
		if (declaredFields == null) {
			return this.declaredFields = getDeclaredFields0(false);
		}
		return declaredFields;
	}

	@Override
	public ClassFile getRawClassFile() {
		ClassFile rawClassFile = this.rawClassFile;
		if (rawClassFile == null) {
			try {
				ClassFileReader classFileReader = ThreadLocalStorage.get().getClassFileReader();
				return this.rawClassFile = classFileReader.read(classReader.b);
			} catch (InvalidClassException ex) {
				// Should not happen.
				// unless??
				throw new RuntimeException("Cafedude returned invalid class file", ex);
			}
		}
		return rawClassFile;
	}

	@Override
	public MethodLayout getVirtualMethodLayout() {
		MethodLayout vrtMethodLayout = this.vrtMethodLayout;
		if (vrtMethodLayout == null) {
			Map<MemberKey, JavaMethod> map = new HashMap<>();
			int slot = 0;
			List<MethodNode> methods = node.methods;
			for (MethodNode method : methods) {
				if ((method.access & Opcodes.ACC_STATIC) == 0) {
					map.put(new SimpleMemberKey(this, method.name, method.desc), new JavaMethod(this, method, slot++));
				}
			}
			vrtMethodLayout = new MethodLayout(Collections.unmodifiableMap(map));
			this.vrtMethodLayout = vrtMethodLayout;
		}
		return vrtMethodLayout;
	}

	@Override
	public MethodLayout getStaticMethodLayout() {
		MethodLayout staticMethodLayout = this.staticMethodLayout;
		if (staticMethodLayout == null) {
			Map<MemberKey, JavaMethod> map = new HashMap<>();
			int slot = getVirtualMethodCount();
			List<MethodNode> methods = node.methods;
			for (MethodNode method : methods) {
				if ((method.access & Opcodes.ACC_STATIC) != 0) {
					map.put(new SimpleMemberKey(this, method.name, method.desc), new JavaMethod(this, method, slot++));
				}
			}
			staticMethodLayout = new MethodLayout(Collections.unmodifiableMap(map));
			this.staticMethodLayout = staticMethodLayout;
		}
		return staticMethodLayout;
	}

	@Override
	public boolean shouldBeInitialized() {
		Lock lock = initializationLock;
		lock.lock();
		boolean isPending = state == State.PENDING;
		lock.unlock();
		return isPending;
	}

	@Override
	public InstanceJavaClass getSuperclassWithoutResolving() {
		String superName = node.superName;
		if (superName == null) {
			return null;
		}
		InstanceJavaClass superClass = this.superClass;
		if (superClass == null) {
			VirtualMachine vm = this.vm;
			return this.superClass = (InstanceJavaClass) vm.findClass(classLoader, superName, false);
		}
		return superClass;
	}

	@Override
	public synchronized void redefine(ClassReader reader, ClassNode node) {
		ClassNode current = this.node;
		verifyMembers(current.methods, node.methods, it -> it.name, it -> it.desc, it -> it.access);
		verifyMembers(current.fields, node.fields, it -> it.name, it -> it.desc, it -> it.access);
		classReader = reader;
		this.node = node;
		rawClassFile = null;
	}

	@Override
	public boolean canAllocateInstance() {
		// Its okay if this gets computed multiple times
		Boolean allocationStatus = this.allocationStatus;
		if (allocationStatus == null) {
			return this.allocationStatus = checkAllocationStatus();
		}
		return allocationStatus;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setVirtualFieldLayout(FieldLayout layout) {
		this.vrtFieldLayout = layout;
	}

	public void setStaticFieldLayout(FieldLayout layout) {
		this.staticFieldLayout = layout;
	}

	public FieldLayout createStaticFieldLayout() {
		Map<MemberKey, JavaField> map = new HashMap<>();
		long offset = 0L;
		int slot = getVirtualFieldCount();
		VMHelper helper = vm.getHelper();
		List<FieldNode> fields = node.fields;
		for (FieldNode field : fields) {
			if ((field.access & Opcodes.ACC_STATIC) != 0) {
				String desc = field.desc;
				map.put(new SimpleMemberKey(this, field.name, desc), new JavaField(this, field, slot++, offset));
				offset += helper.getDescriptorSize(desc);
			}
		}
		return new FieldLayout(Collections.unmodifiableMap(map), offset);
	}

	public FieldLayout createVirtualFieldLayout() {
		HashMap<MemberKey, JavaField> map = new HashMap<MemberKey, JavaField>();
		ArrayDeque<InstanceJavaClass> deque = new ArrayDeque<InstanceJavaClass>();
		long offset = 0L;
		InstanceJavaClass javaClass = this;
		while (javaClass != null) {
			deque.addFirst(javaClass);
			javaClass = javaClass.getSuperclassWithoutResolving();
		}
		VMHelper helper = vm.getHelper();
		int slot = 0;
		while ((javaClass = deque.pollFirst()) != null) {
			List<FieldNode> fields = javaClass.getNode().fields;
			for (FieldNode field : fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					String desc = field.desc;
					map.put(new SimpleMemberKey(javaClass, field.name, desc), new JavaField(javaClass, field, slot++, offset));
					offset += helper.getDescriptorSize(desc);
				}
			}
		}
		return new FieldLayout(Collections.unmodifiableMap(map), offset);
	}

	private boolean checkAllocationStatus() {
		int acc = getModifiers();
		if ((acc & Opcodes.ACC_ABSTRACT) == 0 && (acc & Opcodes.ACC_INTERFACE) == 0) {
			return this != vm.getSymbols().java_lang_Class();
		}
		return false;
	}

	private List<JavaMethod> getDeclaredMethods0(boolean publicOnly, boolean constructors) {
		Stream<JavaMethod> staticMethods = constructors ? Stream.<JavaMethod>empty() : getStaticMethods0(publicOnly);
		return Stream.concat(staticMethods, getVirtualMethodLayout()
				.getAll()
				.stream()
				.filter(x -> constructors == "<init>".equals(x.getName()))
				.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0))
			.filter(NON_HIDDEN_METHOD)
			.collect(Collectors.toList());
	}

	private Stream<JavaMethod> getStaticMethods0(boolean publicOnly) {
		return getStaticMethodLayout()
			.getAll()
			.stream()
			.filter(x -> !"<clinit>".equals(x.getName()))
			.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0);
	}

	private List<JavaField> getDeclaredFields0(boolean publicOnly) {
		Stream<JavaField> staticFields = getStaticFieldLayout()
			.getAll()
			.stream();
		return Stream.concat(staticFields, getVirtualFieldLayout()
				.getAll()
				.stream())
			.filter(x -> this == x.getOwner())
			.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0)
			.filter(NON_HIDDEN_FIELD)
			.collect(Collectors.toList());
	}

	private void loadSuperClass() {
		InstanceJavaClass superClass = this.superClass;
		if (superClass == null) {
			VirtualMachine vm = this.vm;
			String superName = node.superName;
			if (superName != null) {
				// Load parent class.
				superClass = (InstanceJavaClass) vm.findClass(classLoader, superName, false);
				this.superClass = superClass;
			}
		}
	}

	private void loadInterfaces() {
		InstanceJavaClass[] $interfaces = this.interfaces;
		if ($interfaces == null) {
			List<String> _interfaces = node.interfaces;
			$interfaces = new InstanceJavaClass[_interfaces.size()];
			VirtualMachine vm = this.vm;
			ObjectValue classLoader = this.classLoader;
			for (int i = 0, j = _interfaces.size(); i < j; i++) {
				$interfaces[i] = (SimpleInstanceJavaClass) vm.findClass(classLoader, _interfaces.get(i), false);
			}
			this.interfaces = $interfaces;
		}
	}

	private int getVirtualFieldCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			for (FieldNode field : jc.getNode().fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					count++;
				}
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private int getTotalFieldCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			count += jc.getNode().fields.size();
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private int getTotalMethodCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			count += jc.getNode().methods.size();
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private int getVirtualMethodCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			for (MethodNode field : jc.getNode().methods) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					count++;
				}
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private JavaMethod lookupMethodIn(MethodLayout layout, MemberKey key) {
		Map<MemberKey, JavaMethod> methods = layout.getMethods();
		JavaMethod jm = methods.get(key);
		return alternativeLookupMethodIn(jm, methods, key.getName(), key.getDesc());
	}

	private JavaMethod lookupMethodIn(MethodLayout layout, String name, String desc) {
		Map<MemberKey, JavaMethod> methods = layout.getMethods();
		JavaMethod jm = methods.get(new SimpleMemberKey(this, name, desc));
		return alternativeLookupMethodIn(jm, methods, name, desc);
	}

	private JavaMethod alternativeLookupMethodIn(JavaMethod jm, Map<MemberKey, JavaMethod> methods, String name, String desc) {
		if (jm == null) {
			jm = methods.get(new SimpleMemberKey(this, name, POLYMORPHIC_DESC));
			if (jm != null) {
				if (!jm.isPolymorphic()) {
					jm = null;
				} else {
					return linkPolymorphicCall(jm, name, desc);
				}
			}
		}
		return jm;
	}

	private JavaMethod linkPolymorphicCall(JavaMethod original, String name, String desc) {
		InstanceJavaClass owner = original.getOwner();
		return new JavaMethod(owner, original.getNode(), desc, original.getSlot());
	}

	private void markFailedInitialization(VMException ex) {
		state = State.FAILED;
		InstanceValue oop = ex.getOop();
		VirtualMachine vm = this.vm;
		VMSymbols symbols = vm.getSymbols();
		if (!symbols.java_lang_Error().isAssignableFrom(oop.getJavaClass())) {
			InstanceJavaClass jc = symbols.java_lang_ExceptionInInitializerError();
			jc.initialize();
			oop = vm.getMemoryManager().newInstance(jc);
			// Can't use newException here
			JavaMethod init = vm.getLinkResolver().resolveSpecialMethod(jc, "<init>", "(Ljava/lang/Throwable;)V");
			Locals locals = vm.getThreadStorage().newLocals(init);
			locals.set(0, oop);
			locals.set(1, oop);
			vm.getHelper().invoke(init, locals);
			throw new VMException(oop);
		}
		throw ex;
	}

	private static <T> Predicate<T> nonHidden(ToIntFunction<T> function) {
		return x -> !Modifier.isHiddenMember(function.applyAsInt(x));
	}

	private static <T> void verifyMembers(List<T> current, List<T> redefined, Function<T, String> name, Function<T, String> desc, ToIntFunction<T> access) {
		if (current.size() != redefined.size()) {
			throw new IllegalStateException("Size mismatch");
		}
		for (int i = 0; i < current.size(); i++) {
			T t1 = current.get(i);
			T t2 = redefined.get(i);
			if (!name.apply(t1).equals(name.apply(t2))) {
				throw new IllegalStateException("Member name changed");
			}
			if (!desc.apply(t1).equals(desc.apply(t2))) {
				throw new IllegalStateException("Member descriptor changed");
			}
			if ((access.applyAsInt(t1) & Opcodes.ACC_STATIC) != (access.applyAsInt(t2) & Opcodes.ACC_STATIC)) {
				throw new IllegalStateException("Static access changed");
			}
		}
	}
}
