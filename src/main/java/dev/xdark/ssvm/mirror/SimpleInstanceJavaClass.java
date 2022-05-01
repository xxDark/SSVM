package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import lombok.val;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleInstanceJavaClass implements InstanceJavaClass {

	private static final ClassFileReader READER = new ClassFileReader();
	private static final String POLYMORPHIC_DESC = "([Ljava/lang/Object;)Ljava/lang/Object;";

	private final VirtualMachine vm;
	private final ObjectValue classLoader;

	private final Lock initializationLock;
	private final Condition signal;

	private final ClassReader classReader;
	private final ClassNode node;
	private ClassFile rawClassFile;

	private InstanceValue oop;

	private FieldLayout vrtFieldLayout;
	private FieldLayout staticFieldLayout;

	private MethodLayout vrtMethodLayout;
	private MethodLayout staticMethodLayout;

	private SimpleInstanceJavaClass superClass;
	private SimpleInstanceJavaClass[] interfaces;
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
	 * This constructor must be invoked ONLY
	 * by the VM.
	 *
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
	public SimpleInstanceJavaClass(VirtualMachine vm, ObjectValue classLoader, ClassReader classReader, ClassNode node, InstanceValue oop) {
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
	public SimpleInstanceJavaClass(VirtualMachine vm, ObjectValue classLoader, ClassReader classReader, ClassNode node) {
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
	public ObjectValue getClassLoader() {
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
					signal.await();
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
		// Build class layout
		// VM might've set it already, do not override.
		if (vrtFieldLayout == null) {
			vrtFieldLayout = createVirtualFieldLayout();
		}
		helper.initializeStaticFields(this);
		helper.setupHiddenFrames(this);
		val clinit = getStaticMethod("<clinit>", "()V");
		try {
			if (clinit != null) {
				helper.invokeStatic(this, clinit, new Value[0], new Value[0]);
			}
			state = State.COMPLETE;
		} catch (VMException ex) {
			markFailedInitialization(ex);
		} finally {
			signal.signalAll();
			lock.unlock();
			initializer = null;
		}
	}

	@Override
	public void link() {
		val lock = initializationLock;
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
			val vm = this.vm;
			vm.getHelper().throwException(vm.getSymbols().java_lang_NullPointerException);
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
		}
		if (this == vm.getSymbols().java_lang_Object) {
			return true;
		}
		if (other.isInterface()) {
			if (isInterface()) {
				val toCheck = new ArrayDeque<>(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) return true;
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
			}
		} else {
			val toCheck = new ArrayDeque<JavaClass>();
			JavaClass superClass = other.getSuperClass();
			if (superClass != null)
				toCheck.add(superClass);
			if (isInterface()) {
				toCheck.addAll(Arrays.asList(other.getInterfaces()));
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) return true;
					superClass = popped.getSuperClass();
					if (superClass != null) toCheck.add(superClass);
					toCheck.addAll(Arrays.asList(popped.getInterfaces()));
				}
			} else {
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) return true;
					superClass = popped.getSuperClass();
					if (superClass != null) toCheck.add(superClass);
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
	public SimpleInstanceJavaClass getSuperClass() {
		initialize();
		return superClass;
	}

	@Override
	public SimpleInstanceJavaClass[] getInterfaces() {
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
		val deque = new ArrayDeque<InstanceJavaClass>();
		do {
			method = jc.getVirtualMethod(name, desc);
			deque.push(jc);
		} while (method == null && (jc = jc.getSuperclassWithoutResolving()) != null);
		if (method == null) {
			search:
			while ((jc = deque.poll()) != null) {
				method = jc.getVirtualMethod(name, desc);
				if (method != null) break;
				for (val iface : jc.getInterfaces()) {
					method = iface.getVirtualMethod(name, desc);
					if (method != null) break search;
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
		val key = (MemberKey) new SimpleMemberKey(this, name, desc);
		JavaMethod jm = getVirtualMethodLayout().getMethods().get(key);
		if (jm == null) {
			jm = getStaticMethodLayout().getMethods().get(key);
		}
		return jm;
	}

	@Override
	public Value getStaticValue(MemberKey field) {
		initialize();

		val offset = staticFieldLayout.getFieldOffset(field);
		if (offset == -1L) return null;
		val oop = this.oop;
		val memoryManager = vm.getMemoryManager();
		val resultingOffset = memoryManager.getStaticOffset(this) + offset;
		switch (field.getDesc()) {
			case "J":
				return LongValue.of(memoryManager.readLong(oop, resultingOffset));
			case "D":
				return new DoubleValue(memoryManager.readDouble(oop, resultingOffset));
			case "I":
				return IntValue.of(memoryManager.readInt(oop, resultingOffset));
			case "F":
				return new FloatValue(memoryManager.readFloat(oop, resultingOffset));
			case "C":
				return IntValue.of(memoryManager.readChar(oop, resultingOffset));
			case "S":
				return IntValue.of(memoryManager.readShort(oop, resultingOffset));
			case "B":
				return IntValue.of(memoryManager.readByte(oop, resultingOffset));
			case "Z":
				return memoryManager.readBoolean(oop, resultingOffset) ? IntValue.ONE : IntValue.ZERO;
			default:
				return memoryManager.readValue(oop, resultingOffset);
		}
	}

	@Override
	public Value getStaticValue(String name, String desc) {
		return getStaticValue(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public boolean setFieldValue(MemberKey field, Value value) {
		initialize();
		val offset = staticFieldLayout.getFieldOffset(field);
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

	@Override
	public boolean setFieldValue(String name, String desc, Value value) {
		return setFieldValue(new SimpleMemberKey(this, name, desc), value);
	}

	@Override
	public long getFieldOffset(String name, String desc) {
		initialize();
		return vrtFieldLayout.getFieldOffset(new SimpleMemberKey(this, name, desc));
	}

	@Override
	public long getFieldOffsetRecursively(String name, String desc) {
		initialize();
		val layout = this.vrtFieldLayout;
		InstanceJavaClass jc = this;
		do {
			val offset = layout.getFieldOffset(new SimpleMemberKey(jc, name, desc));
			if (offset != -1L) return offset;
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return -1L;
	}

	@Override
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

	@Override
	public boolean hasVirtualField(MemberKey info) {
		initialize();
		return vrtFieldLayout.getFields().containsKey(info);
	}

	@Override
	public boolean hasVirtualField(String name, String desc) {
		return hasVirtualField(new SimpleMemberKey(this, name, desc));
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
				map.put(new SimpleMemberKey(this, field.name, desc), new JavaField(this, field, slot++, offset));
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
			val fields = javaClass.getNode().fields;
			for (val field : fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					val desc = field.desc;
					map.put(new SimpleMemberKey(javaClass, field.name, desc), new JavaField(javaClass, field, slot++, offset));
					offset += UnsafeUtil.getSizeFor(desc);
				}
			}
		}
		return new FieldLayout(Collections.unmodifiableMap(map), offset);
	}

	@Override
	public ClassNode getNode() {
		return node;
	}

	@Override
	public ClassReader getClassReader() {
		return classReader;
	}

	/**
	 * Loads hierarchy of classes without marking them as resolved.
	 */
	public void loadNoResolve() {
		val lock = this.initializationLock;
		lock.lock();
		val vm = this.vm;
		if (state == State.FAILED) {
			lock.unlock();
			vm.getHelper().throwException(vm.getSymbols().java_lang_ExceptionInInitializerError);
		}
		try {
			loadSuperClass();
			loadInterfaces();
			for (val ifc : interfaces) {
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

	@Override
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

	@Override
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
	public ClassFile getRawClassFile() {
		ClassFile rawClassFile = this.rawClassFile;
		if (rawClassFile == null) {
			try {
				return this.rawClassFile = READER.read(classReader.b);
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
			val map = new HashMap<MemberKey, JavaMethod>();
			int slot = 0;
			val methods = node.methods;
			for (val method : methods) {
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
			val map = new HashMap<MemberKey, JavaMethod>();
			int slot = getVirtualMethodCount();
			val methods = node.methods;
			for (val method : methods) {
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
		val lock = initializationLock;
		lock.lock();
		val isPending = state == State.PENDING;
		lock.unlock();
		return isPending;
	}

	@Override
	public InstanceJavaClass getSuperclassWithoutResolving() {
		val superName = node.superName;
		if (superName == null) return null;
		val vm = this.vm;
		return (InstanceJavaClass) vm.findClass(classLoader, superName, false);
	}

	@Override
	public String toString() {
		return getName();
	}

	private List<JavaMethod> getDeclaredMethods0(boolean publicOnly, boolean constructors) {
		val staticMethods = constructors ? Stream.<JavaMethod>empty() : getStaticMethods0(publicOnly);
		return Stream.concat(staticMethods, getVirtualMethodLayout()
						.getAll()
						.stream()
						.filter(x -> constructors == "<init>".equals(x.getName()))
						.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0))
				.filter(nonHidden(JavaMethod::getAccess))
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
		val staticFields = getDeclaredStaticFields0(publicOnly);
		return Stream.concat(staticFields, getVirtualFieldLayout()
						.getAll()
						.stream()
						.filter(x -> this == x.getOwner())
						.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0))
				.filter(nonHidden(JavaField::getAccess))
				.collect(Collectors.toList());
	}

	private Stream<JavaField> getDeclaredStaticFields0(boolean publicOnly) {
		return getStaticFieldLayout()
				.getAll()
				.stream()
				.filter(x -> this == x.getOwner())
				.filter(x -> !publicOnly || (x.getAccess() & Opcodes.ACC_PUBLIC) != 0);
	}

	private void loadSuperClass() {
		SimpleInstanceJavaClass superClass = this.superClass;
		if (superClass == null) {
			val vm = this.vm;
			val superName = node.superName;
			if (superName != null) {
				// Load parent class.
				superClass = (SimpleInstanceJavaClass) vm.findClass(classLoader, superName, false);
				this.superClass = superClass;
			}
		}
	}

	private void loadInterfaces() {
		SimpleInstanceJavaClass[] $interfaces = this.interfaces;
		if ($interfaces == null) {
			val _interfaces = node.interfaces;
			$interfaces = new SimpleInstanceJavaClass[_interfaces.size()];
			val vm = this.vm;
			val classLoader = this.classLoader;
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
			for (val field : jc.getNode().fields) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private int getVirtualMethodCount() {
		int count = 0;
		InstanceJavaClass jc = this;
		do {
			for (val field : jc.getNode().methods) {
				if ((field.access & Opcodes.ACC_STATIC) == 0) count++;
			}
		} while ((jc = jc.getSuperclassWithoutResolving()) != null);
		return count;
	}

	private JavaMethod lookupMethodIn(MethodLayout layout, String name, String desc) {
		val methods = layout.getMethods();
		JavaMethod jm = methods.get(new SimpleMemberKey(this, name, desc));
		if (jm == null) {
			jm = methods.get(new SimpleMemberKey(this, name, POLYMORPHIC_DESC));
			if (jm != null) {
				if (!jm.isPolymorphic()) {
					jm = null;
				} else {
					jm = new JavaMethod(this, jm.getNode(), desc, jm.getSlot());
				}
			}
		}
		return jm;
	}

	private void markFailedInitialization(VMException ex) {
		state = State.FAILED;
		InstanceValue oop = ex.getOop();
		val vm = this.vm;
		val symbols = vm.getSymbols();
		if (!symbols.java_lang_Error.isAssignableFrom(oop.getJavaClass())) {
			val cause = oop;
			val jc = symbols.java_lang_ExceptionInInitializerError;
			jc.initialize();
			oop = vm.getMemoryManager().newInstance(jc);
			vm.getHelper().invokeExact(jc, "<init>", "(Ljava/lang/Throwable;)V", new Value[0], new Value[]{
					oop, cause
			});
			throw new VMException(oop);
		}
		throw ex;
	}

	private static <T> Predicate<T> nonHidden(ToIntFunction<T> function) {
		return x -> !Modifier.isHiddenMember(function.applyAsInt(x));
	}

	private enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
