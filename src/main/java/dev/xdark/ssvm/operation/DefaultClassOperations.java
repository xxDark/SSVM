package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LanguageSpecification;
import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.classloading.BootClassFinder;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;
import dev.xdark.ssvm.mirror.member.area.ClassArea;
import dev.xdark.ssvm.mirror.member.area.SimpleClassArea;
import dev.xdark.ssvm.mirror.type.ClassLinkage;
import dev.xdark.ssvm.mirror.type.InitializationState;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.util.AutoCloseableLock;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultClassOperations implements ClassOperations {

	private final MirrorFactory mirrorFactory;
	private final MemoryManager memoryManager;
	private final ThreadManager threadManager;
	private final BootClassFinder bootClassFinder;
	private final LinkResolver linkResolver;
	private final Symbols symbols;
	private final ClassLoaders classLoaders;
	private final ClassDefiner classDefiner;
	private final ClassStorage classStorage;
	private final ExceptionOperations exceptionOperations;
	private final InvocationOperations invocationOperations;
	private final StringOperations stringOperations;
	private final VerificationOperations verificationOperations;
	private final ConstantOperations constantOperations;

	@Override
	public void link(InstanceClass instanceClass) {
		InitializationState state = instanceClass.state();
		state.lock();
		state.set(InstanceClass.State.IN_PROGRESS);
		try {
			ObjectValue cl = instanceClass.getClassLoader();
			ClassLinkage linkage = instanceClass.linkage();
			ClassNode node = instanceClass.getNode();
			String superName = node.superName;
			if (superName != null) {
				linkage.setSuperClass((InstanceClass) findClass(cl, superName, false));
			}
			List<String> interfaces = node.interfaces;
			if (!interfaces.isEmpty()) {
				InstanceClass[] classes = new InstanceClass[interfaces.size()];
				for (int i = 0; i < interfaces.size(); i++) {
					classes[i] = (InstanceClass) findClass(cl, interfaces.get(i), false);
				}
				linkage.setInterfaces(classes);
			}
			// Create method and field area
			List<JavaField> virtualFields = new ArrayList<>();
			InstanceClass jc = instanceClass.getSuperClass();
			JavaField lastField = null;
			while (jc != null) {
				ClassArea<JavaField> area = jc.virtualFieldArea();
				// May be java/lang/Class calling to java/lang/Object
				if (area == null) {
					Assertions.check(jc == symbols.java_lang_Object(), "null area is only allowed for java/lang/Object");
				} else {
					JavaField field = area.stream()
						.filter(x -> (x.getModifiers() & Opcodes.ACC_STATIC) == 0)
						.max(Comparator.comparingLong(JavaField::getOffset))
						.orElse(null);
					if (field != null && (lastField == null || field.getOffset() > lastField.getOffset())) {
						lastField = field;
					}
				}
				jc = jc.getSuperClass();
			}
			long offset;
			MemoryManager memoryManager = this.memoryManager;
			if (lastField != null) {
				offset = lastField.getOffset();
				offset += memoryManager.sizeOfType(lastField.getType()); // TODO calling getType may lead to exception
			} else {
				offset = memoryManager.valueBaseOffset(instanceClass);
			}

			List<FieldNode> fields = node.fields;
			MirrorFactory mf = this.mirrorFactory;
			int slot = 0;
			for (int i = 0, j = fields.size(); i < j; i++) {
				FieldNode fieldNode = fields.get(i);
				if ((fieldNode.access & Opcodes.ACC_STATIC) == 0) {
					JavaField field = mf.newField(instanceClass, fieldNode, slot++, offset);
					offset += memoryManager.sizeOfType(field.getType()); // TODO calling getType may lead to exception
					virtualFields.add(field);
				}
			}
			linkage.setVirtualFieldArea(new SimpleClassArea<>(virtualFields));
			linkage.setOccupiedInstanceSpace(offset - memoryManager.valueBaseOffset(instanceClass));
			int slotOffset = slot;
			// Static fields are stored right after java/lang/Class virtual fields
			// At this point of linkage java/lang/Class must already set its virtual
			// fields as we are doing it before (see above)
			JavaField maxVirtualField = symbols.java_lang_Class().virtualFieldArea()
				.stream()
				.max(Comparator.comparingLong(JavaField::getOffset))
				.orElseThrow(() -> new PanicException("No fields in java/lang/Class"));
			offset = maxVirtualField.getOffset() + memoryManager.sizeOfType(maxVirtualField.getType()); // TODO calling getType may lead to exception
			long baseStaticOffset = offset;
			List<JavaField> staticFields = new ArrayList<>(fields.size() - slot);
			for (int i = 0, j = fields.size(); i < j; i++) {
				FieldNode fieldNode = fields.get(i);
				if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) {
					JavaField field = mf.newField(instanceClass, fieldNode, slot++, offset);
					offset += memoryManager.sizeOfType(field.getType());
					staticFields.add(field);
				}
			}
			linkage.setStaticFieldArea(new SimpleClassArea<>(staticFields, slotOffset));
			linkage.setOccupiedStaticSpace(offset - baseStaticOffset);
			// Set methods
			List<MethodNode> methods = node.methods;
			List<JavaMethod> allMethods = new ArrayList<>(methods.size());
			for (int i = 0, j = methods.size(); i < j; i++) {
				allMethods.add(mf.newMethod(instanceClass, methods.get(i), i));
			}
			linkage.setMethodArea(new SimpleClassArea<>(allMethods));
		} catch (VMException ex) {
			state.set(InstanceClass.State.FAILED);
			throwClassException(ex);
		} finally {
			state.condition().signalAll();
			state.unlock();
		}

	}

	@Override
	public void initialize(InstanceClass instanceClass) {
		InitializationState state = instanceClass.state();
		state.lock();
		if (state.is(InstanceClass.State.COMPLETE) || state.is(InstanceClass.State.IN_PROGRESS)) {
			state.unlock();
			return;
		}
		if (state.is(InstanceClass.State.FAILED)) {
			state.unlock();
			exceptionOperations.throwException(symbols.java_lang_NoClassDefFoundError(), instanceClass.getInternalName());
		}
		state.set(InstanceClass.State.IN_PROGRESS);
		try {
			// Initialize hierarchy
			InstanceClass superClass = instanceClass.getSuperClass();
			if (superClass != null) {
				initialize(superClass);
			}
			// note: interfaces are *not* initialized here
			initializeStaticFields(instanceClass);
			JavaMethod clinit = instanceClass.getMethod("<clinit>", "()V");
			if (clinit != null) {
				Locals locals = threadManager.currentThreadStorage().newLocals(clinit);
				invocationOperations.invokeVoid(clinit, locals);
			}
		} catch (VMException ex) {
			state.set(InstanceClass.State.FAILED);
			throwClassException(ex);
		} finally {
			state.condition().signalAll();
			state.unlock();
		}
	}

	@Override
	public JavaClass findClass(ObjectValue classLoader, String internalName, boolean initialize) {
		int dimensions = 0;
		while (internalName.charAt(dimensions) == '[') {
			dimensions++;
		}
		if (dimensions >= LanguageSpecification.ARRAY_DIMENSION_LIMIT) {
			exceptionOperations.throwException(symbols.java_lang_ClassNotFoundException(), internalName);
		}
		String trueName = dimensions == 0 ? internalName : internalName.substring(dimensions + 1, internalName.length() - 1);
		ClassLoaderData data = classLoaders.getClassLoaderData(classLoader);
		try (AutoCloseableLock lock = data.lock()) {
			JavaClass klass = data.getClass(trueName);
			if (klass == null) {
				if (classLoader.isNull()) {
					ParsedClassData cdata = bootClassFinder.findBootClass(trueName);
					if (cdata != null) {
						klass = defineClass(classLoader, cdata, null, "JVM_DefineClass", true);
					}
				} else {
					// Ask Java world
					JavaMethod method = linkResolver.resolveVirtualMethod(classLoader, "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;");
					Locals locals = threadManager.currentThreadStorage().newLocals(method);
					locals.setReference(0, classLoader);
					locals.setReference(1, stringOperations.newUtf8(trueName.replace('/', '.')));
					locals.setInt(2, initialize ? 1 : 0);
					InstanceValue result = verificationOperations.checkNotNull(invocationOperations.invokeReference(method, locals));
					klass = classStorage.lookup(result);
				}
				if (klass == null) {
					exceptionOperations.throwException(symbols.java_lang_ClassNotFoundException(), internalName.replace('/', '.'));
				}
			}
			if (initialize) {
				if (klass instanceof InstanceClass) {
					initialize((InstanceClass) klass);
				}
			}
			return klass;
		}
	}

	@Override
	public InstanceClass defineClass(ObjectValue classLoader, ParsedClassData data, ObjectValue protectionDomain, String source, boolean shouldBeLinked) {
		ClassReader reader = data.getClassReader();
		InstanceClass jc = mirrorFactory.newInstanceClass(classLoader, reader, data.getNode());
		if (shouldBeLinked) {
			ClassLoaderData classLoaderData = classLoaders.getClassLoaderData(classLoader);
			if (!classLoaderData.linkClass(jc)) {
				exceptionOperations.throwException(symbols.java_lang_NoClassDefFoundError(), "Duplicate class: " + reader.getClassName());
			}
		}
		link(jc);
		classStorage.register(jc);
		return jc;
	}

	@Override
	public InstanceClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source, boolean shouldBeLinked) {
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			exceptionOperations.throwException(symbols.java_lang_ArrayIndexOutOfBoundsException());
		}
		ParsedClassData data = classDefiner.parseClass(name, b, off, len, source);
		if (data == null) {
			exceptionOperations.throwException(symbols.java_lang_NoClassDefFoundError(), name);
		}
		String classReaderName = data.getClassReader().getClassName();
		if (name == null) {
			name = classReaderName;
		} else if (!classReaderName.equals(name.replace('.', '/'))) {
			exceptionOperations.throwException(symbols.java_lang_ClassNotFoundException(), "Expected class name " + classReaderName.replace('/', '.') + " but received: " + name);
		}
		if (name.contains("[") || name.contains("(") || name.contains(")") || name.contains(";")) {
			exceptionOperations.throwException(symbols.java_lang_NoClassDefFoundError(), "Bad class name: " + classReaderName);
		}
		return defineClass(classLoader, data, protectionDomain, source, shouldBeLinked);
	}

	@Override
	public JavaClass findClass(ObjectValue classLoader, Type type, boolean initialize) {
		Assertions.check(type.getSort() > Type.DOUBLE, "not a reference type");
		return findClass(classLoader, type.getInternalName(), initialize);
	}


	private void initializeStaticFields(InstanceClass instanceClass) {
		InstanceValue oop = instanceClass.getOop();
		Assertions.notNull(oop, "oop not created");
		MemoryManager memoryManager = this.memoryManager;
		MemoryData data = oop.getData();
		for (JavaField field : instanceClass.staticFieldArea().list()) {
			MemberIdentifier identifier = field.getIdentifier();
			String desc = identifier.getDesc();
			FieldNode fn = field.getNode();
			Object cst = fn.value;
			if (cst == null) {
				cst = AsmUtil.getDefaultValue(desc);
			}
			long offset = field.getOffset();
			switch (desc.charAt(0)) {
				case 'J':
					data.writeLong(offset, (Long) cst);
					break;
				case 'D':
					data.writeLong(offset, Double.doubleToRawLongBits((Double) cst));
					break;
				case 'I':
					data.writeInt(offset, (Integer) cst);
					break;
				case 'F':
					data.writeInt(offset, Float.floatToRawIntBits((Float) cst));
					break;
				case 'C':
					data.writeChar(offset, (char) ((Integer) cst).intValue());
					break;
				case 'S':
					data.writeShort(offset, ((Integer) cst).shortValue());
					break;
				case 'B':
				case 'Z':
					data.writeByte(offset, ((Integer) cst).byteValue());
					break;
				default:
					memoryManager.writeValue(oop, offset, cst == null ? memoryManager.nullValue() : constantOperations.referenceValue(cst));
			}
		}
	}


	private void throwClassException(VMException ex) {
		InstanceValue oop = ex.getOop();
		Symbols symbols = this.symbols;
		if (!symbols.java_lang_Error().isAssignableFrom(oop.getJavaClass())) {
			InstanceClass jc = symbols.java_lang_ExceptionInInitializerError();
			initialize(jc);
			InstanceValue cause = oop;
			oop = memoryManager.newInstance(jc);
			// Can't use newException here
			JavaMethod init = jc.getMethod("<init>", "(Ljava/lang/Throwable;)V");
			Locals locals = threadManager.currentThreadStorage().newLocals(init);
			locals.setReference(0, oop);
			locals.setReference(1, cause);
			invocationOperations.invokeVoid(init, locals);
			throw new VMException(oop);
		}
		throw ex;
	}
}
