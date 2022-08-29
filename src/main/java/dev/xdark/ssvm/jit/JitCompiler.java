package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Compiles method to Java bytecode.
 *
 * @author xDark
 */
public final class JitCompiler implements Opcodes {

	private static final AtomicInteger CLASS_ID = new AtomicInteger();
	private static final String INVOKER = Type.getInternalName(AbstractInvoker.class);
	private static final String INVOKE_NAME = "invoke";
	private static final String INVOKE_DESC = Type.getMethodDescriptor(Type.getType(Void.TYPE), Type.getType(ExecutionContext.class));
	private static final String CONSTANTS_FIELD = "constants";

	private static final String OBJECT_VALUE = Type.getInternalName(ObjectValue.class);
	private static final String INSTANCE_VALUE = Type.getInternalName(InstanceValue.class);

	private static final Access GET_LOCALS = interfaceCall()
		.owner(ExecutionContext.class)
		.name("getLocals")
		.rt(Locals.class)
		.build();

	private static Access localLoad(String name, Class<?> rt) {
		return interfaceCall()
			.owner(Locals.class)
			.name("load" + name)
			.rt(rt)
			.args("I")
			.build();
	}

	private static final Access LOCAL_LOAD_REFERENCE = localLoad("Reference", ObjectValue.class);
	private static final Access LOCAL_LOAD_LONG = localLoad("Long", long.class);
	private static final Access LOCAL_LOAD_DOUBLE = localLoad("Double", double.class);
	private static final Access LOCAL_LOAD_INT = localLoad("Int", int.class);
	private static final Access LOCAL_LOAD_FLOAT = localLoad("Float", float.class);

	private static final Access LOAD_NULL = staticCall()
		.owner(JitFunctions.class)
		.name("loadNull")
		.args(ExecutionContext.class)
		.rt(ObjectValue.class)
		.build();

	private static final Access MONITOR_ENTER = staticCall()
		.owner(JitFunctions.class)
		.name("monitorEnter")
		.args(ObjectValue.class, ExecutionContext.class)
		.rt("V")
		.build();
	private static final Access MONITOR_EXIT = staticCall()
		.owner(JitFunctions.class)
		.name("monitorExit")
		.args(ObjectValue.class, ExecutionContext.class)
		.rt("V")
		.build();

	private static final Access SET_LINE = staticCall()
		.owner(JitFunctions.class)
		.name("setLineNumber")
		.args("I", ExecutionContext.class)
		.rt("V")
		.build();

	private static Access arrayLoad(String name, Class<?> rt) {
		return staticCall()
			.owner(JitFunctions.class)
			.name("load" + name)
			.args(ObjectValue.class, Integer.TYPE, ExecutionContext.class)
			.rt(rt)
			.build();
	}

	private static final Access ARRAY_LOAD_REFERENCE = arrayLoad("Reference", ObjectValue.class);
	private static final Access ARRAY_LOAD_LONG = arrayLoad("Long", Long.TYPE);
	private static final Access ARRAY_LOAD_DOUBLE = arrayLoad("Double", Double.TYPE);
	private static final Access ARRAY_LOAD_INT = arrayLoad("Int", Integer.TYPE);
	private static final Access ARRAY_LOAD_FLOAT = arrayLoad("Float", Float.TYPE);
	private static final Access ARRAY_LOAD_CHAR = arrayLoad("Char", Character.TYPE);
	private static final Access ARRAY_LOAD_SHORT = arrayLoad("Short", Short.TYPE);
	private static final Access ARRAY_LOAD_BYTE = arrayLoad("Byte", Byte.TYPE);

	private static Access arrayStore(String name, Class<?> type) {
		return staticCall()
			.owner(JitFunctions.class)
			.name("store" + name)
			.args(ObjectValue.class, Integer.TYPE, type)
			.rt("V")
			.build();
	}

	private static final Access ARRAY_STORE_REFERENCE = arrayStore("Reference", ObjectValue.class);
	private static final Access ARRAY_STORE_LONG = arrayStore("Long", Long.TYPE);
	private static final Access ARRAY_STORE_DOUBLE = arrayStore("Double", Double.TYPE);
	private static final Access ARRAY_STORE_INT = arrayStore("Int", Integer.TYPE);
	private static final Access ARRAY_STORE_FLOAT = arrayStore("Float", Float.TYPE);
	private static final Access ARRAY_STORE_CHAR = arrayStore("Char", Character.TYPE);
	private static final Access ARRAY_STORE_SHORT = arrayStore("Short", Short.TYPE);
	private static final Access ARRAY_STORE_BYTE = arrayStore("Byte", Byte.TYPE);

	private static final String EXCEPTION_TYPE = Type.getInternalName(VMException.class);
	private static final Access EXCEPTION_CAUGHT = staticCall()
		.owner(JitFunctions.class)
		.name("exceptionCaught")
		.args(ObjectValue.class, Object.class /* ExceptionInfo */)
		.rt(ObjectValue.class)
		.build();

	private static final Access MAKE_METHOD_TYPE = staticCall()
		.owner(JitFunctions.class)
		.name("makeMethodType")
		.args(Object.class /* Type */, ExecutionContext.class)
		.rt(InstanceValue.class)
		.build();
	private static final Access LOAD_CLASS = staticCall()
		.owner(JitFunctions.class)
		.name("loadClass")
		.args(Object.class /* String */, ExecutionContext.class)
		.rt(InstanceValue.class)
		.build();
	private static final Access MAKE_METHOD_HANDLE = staticCall()
		.owner(JitFunctions.class)
		.name("makeMethodHandle")
		.args(Object.class /* Handle */, ExecutionContext.class)
		.rt(InstanceValue.class)
		.build();

	private static Access makeReturn(Class<?> type) {
		return staticCall()
			.owner(ExecutionContext.class)
			.name("setResult")
			.args(type)
			.rt("V")
			.build();
	}

	private static final Access RETURN_REFERENCE = makeReturn(ObjectValue.class);
	private static final Access RETURN_LONG = makeReturn(Long.TYPE);
	private static final Access RETURN_DOUBLE = makeReturn(Double.TYPE);
	private static final Access RETURN_FLOAT = makeReturn(Float.TYPE);
	private static final Access RETURN_INT = makeReturn(Integer.TYPE);

	private static final int CTX_SLOT = 0;
	private static final int VAR_OFFSET = 1;
	private final String className;
	private final JavaMethod method;
	private final ClassWriter writer;
	private final MethodVisitor result;
	private final Map<Object, Integer> constants;

	public static boolean isSupported(JavaMethod method) {
		return true;
	}

	public static CompiledData compile(JavaMethod method) {
		if (!isSupported(method)) {
			// TODO
		}
		String className = "dev/xdark/ssvm/jit/CompiledCode_" + CLASS_ID.incrementAndGet();
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, className, INVOKER, null, null);
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, INVOKE_NAME, INVOKE_DESC, null, null);
		mv.visitCode();
		JitCompiler compiler = new JitCompiler(className, method, writer, mv);
		compiler.doCompilation();
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
		List<Object> constants;
		Map<Object, Integer> constantMap = compiler.constants;
		if (constantMap.isEmpty()) {
			constants = Collections.emptyList();
		} else {
			constants = Arrays.asList(constantMap.keySet().toArray());
		}
		return new CompiledData(writer.toByteArray(), constants);
	}

	private void doCompilation() {
		MethodVisitor result = this.result;
		JavaMethod method = this.method;
		boolean vrt = !Modifier.isStatic(method.getAccess());
		JavaClass[] args = method.getArgumentTypes();
		if (vrt || args.length != 0) {
			loadContext();
			GET_LOCALS.emit(result);
			int index = 0;
			if (vrt) {
				if (args.length != 0) {
					result.visitInsn(DUP);
					pushInt(0);
					LOCAL_LOAD_REFERENCE.emit(result);
					result.visitVarInsn(ASTORE, 0 + VAR_OFFSET);
				}
				index = 1;
			}
			for (int i = 0, j = args.length; i < j; ) {
				JavaClass jc = args[i++];
				if (i != j) {
					result.visitInsn(DUP);
				}
				pushInt(index);
				Type type = jc.getType();
				switch (type.getSort()) {
					case Type.LONG:
						LOCAL_LOAD_LONG.emit(result);
						break;
					case Type.DOUBLE:
						LOCAL_LOAD_DOUBLE.emit(result);
						break;
					case Type.INT:
					case Type.CHAR:
					case Type.SHORT:
					case Type.BYTE:
					case Type.BOOLEAN:
						LOCAL_LOAD_INT.emit(result);
						break;
					case Type.FLOAT:
						LOCAL_LOAD_FLOAT.emit(result);
						break;
					default:
						LOCAL_LOAD_REFERENCE.emit(result);
				}
				result.visitVarInsn(type.getOpcode(ISTORE), index + VAR_OFFSET);
				index += type.getSize();
			}
		}
		MethodNode node = method.getNode();
		InsnList instructions = node.instructions;
		List<TryCatchBlockNode> tryCatchBlocks = node.tryCatchBlocks;
		Map<LabelNode, Label> labels = StreamSupport.stream(instructions.spliterator(), false)
			.filter(x -> x instanceof LabelNode)
			.collect(Collectors.toMap(x -> (LabelNode) x, __ -> new Label()));
		Map<LabelNode, Set<String>> handlers = tryCatchBlocks.stream()
			.collect(Collectors.groupingBy(
				x -> x.handler,
				Collectors.mapping(x -> x.type, Collectors.toSet())
			));
		for (int i = 0; i < tryCatchBlocks.size(); i++) {
			TryCatchBlockNode tcb = tryCatchBlocks.get(i);
			result.visitTryCatchBlock(
				labels.get(tcb.start),
				labels.get(tcb.end),
				labels.get(tcb.handler),
				EXCEPTION_TYPE
			);
		}
		for (int i = 0, j = instructions.size(); i < j; i++) {
			AbstractInsnNode insn = instructions.get(i);
			int opcode = insn.getOpcode();
			switch (opcode) {
				default:
					if (insn instanceof VarInsnNode) {
						VarInsnNode varInsnNode = (VarInsnNode) insn;
						result.visitVarInsn(opcode, varInsnNode.var + VAR_OFFSET);
					}
					break;
				case -1:
					if (insn instanceof LineNumberNode) {
						LineNumberNode lineNumberNode = (LineNumberNode) insn;
						pushInt(lineNumberNode.line);
						loadContext();
						SET_LINE.emit(result);
					} else if (insn instanceof LabelNode) {
						Label newLabel = labels.get(insn);
						result.visitLabel(newLabel);
						Set<String> types = handlers.remove(insn);
						if (types != null) {
							VirtualMachine vm = vm();
							ObjectValue loader = classLoader();
							Set<InstanceJavaClass> exceptionTypes = new HashSet<>((int) Math.ceil(types.size() / 0.75F));
							for (String type : types) {
								exceptionTypes.add((InstanceJavaClass) vm.findClass(loader, type, false));
							}
							loadCompilerConstant(new ExceptionInfo(exceptionTypes, vm.getMemoryManager().nullValue()));
							EXCEPTION_CAUGHT.emit(result);
						}
					}
					break;
				case ACONST_NULL:
					loadNull();
					break;
				case MONITORENTER:
					loadContext();
					MONITOR_ENTER.emit(result);
					break;
				case MONITOREXIT:
					loadContext();
					MONITOR_EXIT.emit(result);
					break;
				case IALOAD:
					ARRAY_LOAD_INT.emit(result);
					break;
				case LALOAD:
					ARRAY_LOAD_LONG.emit(result);
					break;
				case FALOAD:
					ARRAY_LOAD_FLOAT.emit(result);
					break;
				case DALOAD:
					ARRAY_LOAD_DOUBLE.emit(result);
					break;
				case AALOAD:
					ARRAY_LOAD_REFERENCE.emit(result);
					break;
				case BALOAD:
					ARRAY_LOAD_BYTE.emit(result);
					break;
				case CALOAD:
					ARRAY_LOAD_CHAR.emit(result);
					break;
				case SALOAD:
					ARRAY_LOAD_SHORT.emit(result);
					break;
				case IASTORE:
					ARRAY_STORE_INT.emit(result);
					break;
				case LASTORE:
					ARRAY_STORE_LONG.emit(result);
					break;
				case FASTORE:
					ARRAY_STORE_FLOAT.emit(result);
					break;
				case DASTORE:
					ARRAY_STORE_DOUBLE.emit(result);
					break;
				case AASTORE:
					ARRAY_STORE_REFERENCE.emit(result);
					break;
				case BASTORE:
					ARRAY_STORE_BYTE.emit(result);
					break;
				case CASTORE:
					ARRAY_STORE_CHAR.emit(result);
					break;
				case SASTORE:
					ARRAY_STORE_SHORT.emit(result);
					break;
				case LDC:
					LdcInsnNode ldcInsnNode = (LdcInsnNode) insn;
					pushLdc(ldcInsnNode.cst);
					break;
				case IRETURN:
					loadContext();
					result.visitInsn(SWAP);
					RETURN_INT.emit(result);
					result.visitInsn(RETURN);
					break;
				case LRETURN:
					loadContext();
					result.visitInsn(DUP_X2);
					result.visitInsn(POP);
					RETURN_LONG.emit(result);
					result.visitInsn(RETURN);
					break;
				case FRETURN:
					loadContext();
					result.visitInsn(SWAP);
					RETURN_FLOAT.emit(result);
					result.visitInsn(RETURN);
					break;
				case DRETURN:
					loadContext();
					result.visitInsn(DUP_X2);
					result.visitInsn(POP);
					RETURN_DOUBLE.emit(result);
					result.visitInsn(RETURN);
					break;
				case ARETURN:
					loadContext();
					result.visitInsn(SWAP);
					RETURN_REFERENCE.emit(result);
					result.visitInsn(RETURN);
					break;
			}
		}
	}

	private void loadContext() {
		result.visitVarInsn(ALOAD, CTX_SLOT);
	}

	private void loadNull() {
		loadContext();
		LOAD_NULL.emit(result);
	}

	private void loadCompilerConstant(Object constant) {
		int index = makeCompilerConstant(constant);
		MethodVisitor result = this.result;
		result.visitFieldInsn(GETSTATIC, className, CONSTANTS_FIELD, "[Ljava/lang/Object;");
		pushInt(index);
		result.visitInsn(AALOAD);
	}

	private int makeCompilerConstant(Object constant) {
		Map<Object, Integer> constants = this.constants;
		Integer index = constants.get(constant);
		if (index == null) {
			index = constants.size();
			constants.put(constant, index);
		}
		return index;
	}

	private void pushLong(long value) {
		MethodVisitor result = this.result;
		if (value == 0L) {
			result.visitInsn(LCONST_0);
		} else if (value == 1L) {
			result.visitInsn(LCONST_1);
		} else {
			result.visitLdcInsn(value);
		}
	}

	private void pushDouble(double value) {
		MethodVisitor result = this.result;
		if (value == 0.) {
			result.visitInsn(DCONST_0);
		} else if (value == 1.) {
			result.visitInsn(DCONST_1);
		} else {
			result.visitLdcInsn(value);
		}
	}

	private void pushInt(int value) {
		MethodVisitor result = this.result;
		if (value >= -1 && value <= 5) {
			result.visitInsn(ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			result.visitIntInsn(BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			result.visitIntInsn(SIPUSH, value);
		} else {
			result.visitLdcInsn(value);
		}
	}

	private void pushFloat(float value) {
		MethodVisitor result = this.result;
		if (value == 0.F) {
			result.visitInsn(FCONST_0);
		} else if (value == 1.F) {
			result.visitInsn(FCONST_1);
		} else if (value == 2.F) {
			result.visitInsn(FCONST_2);
		} else {
			result.visitLdcInsn(value);
		}
	}

	private void pushReference(ObjectValue ref) {
		loadCompilerConstant(ref);
		result.visitTypeInsn(CHECKCAST, OBJECT_VALUE);
	}

	private void pushInstance(InstanceValue ref) {
		loadCompilerConstant(ref);
		result.visitTypeInsn(CHECKCAST, INSTANCE_VALUE);
	}

	private void pushString(String value) {
		pushInstance((InstanceValue) stringPool().intern(value));
	}

	private void pushLdc(Object value) {
		if (value instanceof Long) {
			pushLong((Long) value);
		} else if (value instanceof Double) {
			pushDouble((Double) value);
		} else if (value instanceof Integer) {
			pushInt((Integer) value);
		} else if (value instanceof Float) {
			pushFloat((Float) value);
		} else if (value instanceof String) {
			pushString((String) value);
		} else if (value instanceof Type) {
			Type type = (Type) value;
			if (type.getSort() == Type.METHOD) {
				try {
					InstanceValue mt = helper().methodType(classLoader(), type);
					pushInstance(mt);
				} catch (VMException ex) {
					// Slow path, class loader failed to load classes
					loadCompilerConstant(type);
					loadContext();
					MAKE_METHOD_TYPE.emit(result);
				}
			} else {
				String name = type.getInternalName();
				try {
					InstanceValue klas = vm().findClass(classLoader(), name, false).getOop();
					pushInstance(klas);
				} catch (VMException ex) {
					// Slow path, class loader failed to load the class
					loadCompilerConstant(name);
					loadContext();
					LOAD_CLASS.emit(result);
				}
			}
		} else if (value instanceof Handle) {
			Handle handle = (Handle) value;
			try {
				InstanceValue mh = helper().linkMethodHandleConstant(owner(), handle);
				pushReference(mh);
			} catch (VMException ex) {
				// Slow path, class loader failed to load classes or find method handle
				loadCompilerConstant(handle);
				loadContext();
				MAKE_METHOD_HANDLE.emit(result);
			}
		}
	}

	private InstanceJavaClass owner() {
		return method.getOwner();
	}

	private ObjectValue classLoader() {
		return owner().getClassLoader();
	}

	private VirtualMachine vm() {
		return owner().getVM();
	}

	private VMHelper helper() {
		return vm().getHelper();
	}

	private StringPool stringPool() {
		return vm().getStringPool();
	}

	private VMSymbols symbols() {
		return vm().getSymbols();
	}

	private LinkResolver linkResolver() {
		return vm().getPublicLinkResolver();
	}

	private VMOperations operations() {
		return vm().getPublicOperations();
	}

	private JitCompiler withMethod(MethodVisitor mv) {
		return new JitCompiler(className, method, writer, mv, constants);
	}

	private JitCompiler(String className, JavaMethod method, ClassWriter writer, MethodVisitor result, Map<Object, Integer> constants) {
		this.className = className;
		this.method = method;
		this.writer = writer;
		this.result = result;
		this.constants = constants;
	}

	private JitCompiler(String className, JavaMethod method, ClassWriter writer, MethodVisitor result) {
		this(className, method, writer, result, new LinkedHashMap<>());
	}

	static AccessBuilder staticCall() {
		return new AccessBuilder(INVOKESTATIC);
	}

	static AccessBuilder virtualCall() {
		return new AccessBuilder(INVOKEVIRTUAL);
	}

	static AccessBuilder interfaceCall() {
		return new AccessBuilder(INVOKEINTERFACE);
	}

	static AccessBuilder specialCall() {
		return new AccessBuilder(INVOKESPECIAL);
	}

	static AccessBuilder getStatic() {
		return new AccessBuilder(GETSTATIC);
	}

	static AccessBuilder getField() {
		return new AccessBuilder(GETFIELD);
	}

	private static final class AccessBuilder {
		private final int opcode;
		private String returnType;
		private String[] argumentTypes;
		private String owner, name, desc;
		private boolean itf;

		private AccessBuilder(int opcode) {
			this.opcode = opcode;
		}

		AccessBuilder owner(String owner) {
			this.owner = owner;
			return this;
		}

		AccessBuilder owner(Type owner) {
			return owner(owner.getInternalName());
		}

		AccessBuilder owner(Class<?> owner) {
			return owner(Type.getInternalName(owner));
		}

		AccessBuilder name(String name) {
			this.name = name;
			return this;
		}

		AccessBuilder desc(String desc) {
			this.desc = desc;
			return this;
		}

		AccessBuilder rt(String returnType) {
			this.returnType = returnType;
			return this;
		}

		AccessBuilder rt(Type type) {
			return rt(type.getDescriptor());
		}

		AccessBuilder rt(Class<?> type) {
			return rt(Type.getDescriptor(type));
		}

		AccessBuilder args(String... argumentTypes) {
			this.argumentTypes = argumentTypes;
			return this;
		}

		AccessBuilder args(Type... argumentTypes) {
			return args(Stream.of(argumentTypes).map(Type::getDescriptor).toArray(String[]::new));
		}

		AccessBuilder args(Class<?>... argumentTypes) {
			return args(Stream.of(argumentTypes).map(Type::getDescriptor).toArray(String[]::new));
		}

		AccessBuilder args(Object... argumentTypes) {
			return args(Stream.of(argumentTypes).map(x -> {
				if (x instanceof String) {
					return (String) x;
				}
				if (x instanceof Type) {
					return ((Type) x).getDescriptor();
				}
				if (x instanceof Class) {
					return Type.getDescriptor((Class<?>) x);
				}
				throw new IllegalStateException(Objects.toString(x));
			}).toArray(String[]::new));
		}

		AccessBuilder itf() {
			itf = true;
			return this;
		}

		Access build() {
			String desc = this.desc;
			if (desc == null) {
				StringBuilder builder = new StringBuilder();
				builder.append('(');
				String[] argumentTypes = this.argumentTypes;
				if (argumentTypes != null) {
					for (String argumentType : argumentTypes) {
						builder.append(argumentType);
					}
				}
				builder.append(')').append(returnType);
				desc = builder.toString();
			}
			return new Access(opcode, owner, name, desc, itf);
		}
	}

	private static final class Access implements Opcodes {
		private final int opcode;
		private final String owner;
		private final String name;
		private final String desc;
		private final boolean itf;

		Access(int opcode, String owner, String name, String desc, boolean itf) {
			this.opcode = opcode;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.itf = itf;
		}

		void emit(MethodVisitor mv) {
			int opcode = this.opcode;
			String owner = this.owner;
			String name = this.name;
			String desc = this.desc;
			if (opcode >= INVOKEVIRTUAL) {
				mv.visitMethodInsn(opcode, owner, name, desc, itf);
			} else {
				mv.visitFieldInsn(opcode, owner, name, desc);
			}
		}
	}
}
