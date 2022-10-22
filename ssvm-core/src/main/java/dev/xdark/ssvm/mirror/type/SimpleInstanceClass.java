package dev.xdark.ssvm.mirror.type;

import dev.xdark.jlinker.ClassInfo;
import dev.xdark.jlinker.MemberInfo;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;
import dev.xdark.ssvm.mirror.member.area.ClassArea;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.threadlocal.ThreadLocalStorage;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleInstanceClass implements InstanceClass {

	private static final String POLYMORPHIC_DESC = "([Ljava/lang/Object;)Ljava/lang/Object;";
	private static final Predicate<JavaField> NON_HIDDEN_FIELD = nonHidden(JavaField::getModifiers);
	private static final Predicate<JavaMethod> NON_HIDDEN_METHOD = nonHidden(JavaMethod::getModifiers);


	private final VirtualMachine vm;

	private final ObjectValue classLoader;
	private final InitializationState state = new InitializationState();
	private final AtomicBoolean linked = new AtomicBoolean();
	private ClassInfo<JavaClass> linkerInfo;

	private ClassReader classReader;
	private ClassNode node;
	private ClassFile rawClassFile;

	private InstanceValue oop;
	private int id = -1;

	private InstanceClass superClass;
	private List<InstanceClass> interfaces;
	private volatile ArrayClass arrayClass;
	private ClassArea<JavaMethod> methodArea;
	private ClassArea<JavaField> virtualFieldArea;
	private ClassArea<JavaField> staticFieldArea;
	private long occupiedInstanceSpace;
	private long occupiedStaticSpace;

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
	private Type type;

	/**
	 * This constructor must be invoked only
	 * by the VM.
	 *
	 * @param vm          VM in which this class is being created.
	 * @param classLoader Loader of the class.
	 * @param classReader Source of the class.
	 * @param node        ASM class data.
	 */
	public SimpleInstanceClass(VirtualMachine vm, ObjectValue classLoader, ClassReader classReader, ClassNode node) {
		this.vm = vm;
		this.classLoader = classLoader;
		this.classReader = classReader;
		this.node = node;
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
	public int getId() {
		return id;
	}

	@Override
	public boolean isAssignableFrom(JavaClass other) {
		if (other == this) {
			return true;
		}
		if (other.isPrimitive()) {
			return false;
		}
		Symbols symbols = vm.getSymbols();
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
				Deque<InstanceClass> toCheck = new ArrayDeque<>(other.getInterfaces());
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					toCheck.addAll(popped.getInterfaces());
				}
			}
		} else {
			Deque<JavaClass> toCheck = new ArrayDeque<>();
			JavaClass superClass = other.getSuperClass();
			if (superClass != null) {
				toCheck.add(superClass);
			}
			if (isInterface()) {
				toCheck.addAll(other.getInterfaces());
				JavaClass popped;
				while ((popped = toCheck.poll()) != null) {
					if (popped == this) {
						return true;
					}
					superClass = popped.getSuperClass();
					if (superClass != null) {
						toCheck.add(superClass);
					}
					toCheck.addAll(popped.getInterfaces());
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
	public InstanceClass getSuperClass() {
		return superClass;
	}

	@Override
	public List<InstanceClass> getInterfaces() {
		return interfaces;
	}

	@Override
	public ArrayClass newArrayClass() {
		ArrayClass arrayClass = this.arrayClass;
		if (arrayClass == null) {
			synchronized (this) {
				arrayClass = this.arrayClass;
				if (arrayClass == null) {
					arrayClass = vm.getMirrorFactory().newArrayClass(this);
					this.arrayClass = arrayClass;
				}
			}
		}
		return arrayClass;
	}

	@Override
	public ArrayClass getArrayClass() {
		return arrayClass;
	}

	@Override
	public void setOop(InstanceValue oop) {
		Assertions.notNull(oop, "class oop");
		Assertions.isNull(this.oop, "cannot re-assign class oop");
		this.oop = oop;
	}

	@Override
	public void setId(int id) {
		Assertions.check(this.id == -1 , "id already set");
		this.id = id;
	}
	@Override
	public VirtualMachine getVM() {
		return vm;
	}

	@Override
	public JavaField getField(String name, String desc) {
		MemberIdentifier identifier = MemberIdentifier.of(name, desc);
		JavaField field = staticFieldArea.get(identifier);
		if (field == null) {
			field = virtualFieldArea.get(identifier);
		}
		return field;
	}

	@Override
	public JavaMethod getMethod(String name, String desc) {
		ClassArea<JavaMethod> methodArea = this.methodArea;
		JavaMethod method = methodArea.get(name, desc);
		if (method == null) {
			// Polymorphic?
			method = methodArea.get(name, POLYMORPHIC_DESC);
			if (method != null) {
				if (method.isPolymorphic()) {
					method = vm.getMirrorFactory().newPolymorphicMethod(method, desc);
				} else {
					method = null;
				}
			}
		}
		return method;
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
	public JavaMethod getMethodBySlot(int slot) {
		return methodArea.get(slot);
	}

	@Override
	public JavaField getFieldBySlot(int slot) {
		JavaField field = virtualFieldArea.get(slot);
		if (field == null) {
			field = staticFieldArea.get(slot);
		}
		return field;
	}

	@Override
	public ClassArea<JavaMethod> methodArea() {
		return methodArea;
	}

	@Override
	public ClassArea<JavaField> virtualFieldArea() {
		return virtualFieldArea;
	}

	@Override
	public ClassArea<JavaField> staticFieldArea() {
		return staticFieldArea;
	}

	@Override
	public long getOccupiedInstanceSpace() {
		return occupiedInstanceSpace;
	}

	@Override
	public long getOccupiedStaticSpace() {
		return occupiedStaticSpace;
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
	public boolean shouldBeInitialized() {
		InitializationState state = this.state;
		state.lock();
		boolean pending = state.is(State.PENDING);
		state.unlock();
		return pending;
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
	public Type getType() {
		Type type = this.type;
		if (type == null) {
			type = Type.getType(getDescriptor());
			this.type = type;
		}
		return type;
	}

	@Override
	public int getSort() {
		return Type.OBJECT;
	}

	@Override
	public ClassInfo<JavaClass> linkerInfo() {
		Assertions.check(linked.get(), "class is not linked");
		ClassInfo<JavaClass> linkerInfo = this.linkerInfo;
		if (linkerInfo == null) {
			linkerInfo = makeLinkerInfo(this);
			this.linkerInfo = linkerInfo;
		}
		return linkerInfo;
	}

	@Override
	public InitializationState state() {
		return state;
	}

	@Override
	public ClassLinkage linkage() {
		Assertions.check(linked.compareAndSet(false, true), "class linkage occurred twice");
		return new ClassLinkage() {
			@Override
			public void setSuperClass(InstanceClass superClass) {
				SimpleInstanceClass.this.superClass = superClass;
			}

			@Override
			public void setInterfaces(List<InstanceClass> interfaces) {
				SimpleInstanceClass.this.interfaces = interfaces.isEmpty() ? interfaces : Collections.unmodifiableList(interfaces);
			}

			@Override
			public void setVirtualFieldArea(ClassArea<JavaField> fieldArea) {
				SimpleInstanceClass.this.virtualFieldArea = fieldArea;
			}

			@Override
			public void setStaticFieldArea(ClassArea<JavaField> fieldArea) {
				SimpleInstanceClass.this.staticFieldArea = fieldArea;
			}

			@Override
			public void setMethodArea(ClassArea<JavaMethod> methodArea) {
				SimpleInstanceClass.this.methodArea = methodArea;
			}

			@Override
			public void setOccupiedInstanceSpace(long occupiedInstanceSpace) {
				SimpleInstanceClass.this.occupiedInstanceSpace = occupiedInstanceSpace;
			}

			@Override
			public void setOccupiedStaticSpace(long occupiedStaticSpace) {
				SimpleInstanceClass.this.occupiedStaticSpace = occupiedStaticSpace;
			}
		};
	}

	@Override
	public String toString() {
		return getName();
	}

	private boolean checkAllocationStatus() {
		int acc = getModifiers();
		if ((acc & Opcodes.ACC_ABSTRACT) == 0 && (acc & Opcodes.ACC_INTERFACE) == 0) {
			return this != vm.getSymbols().java_lang_Class();
		}
		return false;
	}

	private List<JavaMethod> getDeclaredMethods0(boolean publicOnly, boolean constructors) {
		return methodArea.stream()
			.filter(x -> {
				String name = x.getName();
				if ("<clinit>".equals(name)) {
					return false;
				}
				return constructors == "<init>".equals(name);
			})
			.filter(x -> !publicOnly || (x.getModifiers() & Opcodes.ACC_PUBLIC) != 0)
			.filter(NON_HIDDEN_METHOD)
			.collect(Collectors.toList());
	}

	private List<JavaField> getDeclaredFields0(boolean publicOnly) {
		return Stream.concat(virtualFieldArea.stream(), staticFieldArea.stream())
			.filter(x -> !publicOnly || (x.getModifiers() & Opcodes.ACC_PUBLIC) != 0)
			.filter(NON_HIDDEN_FIELD)
			.collect(Collectors.toList());
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

	private static ClassInfo<JavaClass> makeLinkerInfo(InstanceClass instanceClass) {
		return new ClassInfo<JavaClass>() {
			@Override
			public JavaClass innerValue() {
				return instanceClass;
			}

			@Override
			public int accessFlags() {
				return Modifier.eraseClass(instanceClass.getModifiers());
			}

			@Override
			public ClassInfo<JavaClass> superClass() {
				InstanceClass superClass = instanceClass.getSuperClass();
				return superClass == null ? null : superClass.linkerInfo();
			}

			@Override
			public List<ClassInfo<JavaClass>> interfaces() {
				return instanceClass.getInterfaces().stream().map(JavaClass::linkerInfo).collect(Collectors.toList());
			}

			@Override
			public MemberInfo<?> getMethod(String name, String descriptor) {
				JavaMethod method = instanceClass.getMethod(name, descriptor);
				return method == null ? null : method.linkerInfo();
			}

			@Override
			public MemberInfo<?> getField(String name, String descriptor) {
				JavaField field = instanceClass.getField(name, descriptor);
				return field == null ? null : field.linkerInfo();
			}
		};
	}
}
