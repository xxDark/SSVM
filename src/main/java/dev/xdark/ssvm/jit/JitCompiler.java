package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.asm.DelegatingInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.Opcodes.*;

/**
 * "JIT" compiler.
 *
 * @author xDark
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class JitCompiler {

	private static final AtomicInteger CLASS_ID = new AtomicInteger();
	private static final ClassType J_VOID = ClassType.of(void.class);
	private static final ClassType J_LONG = ClassType.of(long.class);
	private static final ClassType J_INT = ClassType.of(int.class);
	private static final ClassType J_DOUBLE = ClassType.of(double.class);
	private static final ClassType J_FLOAT = ClassType.of(float.class);
	private static final ClassType J_BOOLEAN = ClassType.of(boolean.class);
	private static final ClassType J_OBJECT = ClassType.of(Object.class);
	private static final ClassType J_STRING = ClassType.of(String.class);

	private static final ClassType CTX = ClassType.of(ExecutionContext.class);
	private static final ClassType LOCALS = ClassType.of(Locals.class);
	private static final ClassType STACK = ClassType.of(Stack.class);
	private static final ClassType VALUE = ClassType.of(Value.class);
	private static final ClassType NULL = ClassType.of(NullValue.class);
	private static final ClassType INT = ClassType.of(IntValue.class);
	private static final ClassType LONG = ClassType.of(LongValue.class);
	private static final ClassType FLOAT = ClassType.of(FloatValue.class);
	private static final ClassType DOUBLE = ClassType.of(DoubleValue.class);
	private static final ClassType VM_HELPER = ClassType.of(VMHelper.class);
	private static final ClassType JIT_HELPER = ClassType.of(JitHelper.class);

	// ctx methods
	private static final Access GET_LOCALS = virtualCall(CTX, "getLocals", LOCALS);
	private static final Access GET_STACK = virtualCall(CTX, "getStack", STACK);
	private static final Access GET_HELPER = virtualCall(CTX, "getHelper", VM_HELPER);
	private static final Access SET_RESULT = virtualCall(CTX, "setResult", J_VOID, VALUE);
	private static final Access SET_LINE = virtualCall(CTX, "setLineNumber", J_VOID, J_INT);

	// stack methods
	private static final Access PUSH = virtualCall(STACK, "push", J_VOID, VALUE);
	private static final Access PUSH_WIDE = virtualCall(STACK, "pushWide", J_VOID, VALUE);
	private static final Access PUSH_GENERIC = virtualCall(STACK, "pushGeneric", J_VOID, VALUE);
	private static final Access POP = virtualCall(STACK, "pop", VALUE);
	private static final Access POP_WIDE = virtualCall(STACK, "popWide", VALUE);
	private static final Access DUP = virtualCall(STACK, "dup", J_VOID);
	private static final Access DUP_X1 = virtualCall(STACK, "dupx1", J_VOID);
	private static final Access DUP_X2 = virtualCall(STACK, "dupx2", J_VOID);
	private static final Access DUP2 = virtualCall(STACK, "dup2", J_VOID);
	private static final Access DUP2_X1 = virtualCall(STACK, "dup2x1", J_VOID);
	private static final Access DUP2_X2 = virtualCall(STACK, "dup2x2", J_VOID);
	private static final Access SWAP = virtualCall(STACK, "swap", J_VOID);

	// locals methods
	private static final Access LOAD = virtualCall(LOCALS, "load", VALUE, J_INT);
	private static final Access STORE = virtualCall(LOCALS, "set", J_VOID, J_INT, VALUE);

	// value static methods
	private static final Access GET_NULL = getStatic(NULL, "INSTANCE", NULL);
	private static final Access INT_OF = staticCall(INT, "of", INT, J_INT);
	private static final Access LONG_OF = staticCall(LONG, "of", LONG, J_LONG);
	private static final Access FLOAT_OF = specialCall(FLOAT, "<init>", J_VOID, J_FLOAT);
	private static final Access DOUBLE_OF = specialCall(DOUBLE, "<init>", J_VOID, J_DOUBLE);

	// value methods
	private static final Access AS_LONG = interfaceCall(VALUE, "asLong", J_LONG);
	private static final Access AS_DOUBLE = interfaceCall(VALUE, "asDouble", J_DOUBLE);
	private static final Access AS_INT = interfaceCall(VALUE, "asInt", J_INT);
	private static final Access AS_FLOAT = interfaceCall(VALUE, "asFloat", J_FLOAT);
	private static final Access IS_NULL = interfaceCall(VALUE, "isNull", J_BOOLEAN);

	// helper methods
	private static final Access VALUE_FROM_LDC = virtualCall(VM_HELPER, "valueFromLdc", VALUE, J_OBJECT);

	// jit methods
	private static final Access ARR_LOAD_LONG = staticCall(JIT_HELPER, "arrayLoadLong", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_DOUBLE = staticCall(JIT_HELPER, "arrayLoadDouble", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_INT = staticCall(JIT_HELPER, "arrayLoadInt", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_FLOAT = staticCall(JIT_HELPER, "arrayLoadFloat", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_CHAR = staticCall(JIT_HELPER, "arrayLoadChar", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_SHORT = staticCall(JIT_HELPER, "arrayLoadShort", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_BYTE = staticCall(JIT_HELPER, "arrayLoadByte", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_VALUE = staticCall(JIT_HELPER, "arrayLoadValue", VALUE, VALUE, VALUE, CTX);

	private static final Access ARR_STORE_LONG = staticCall(JIT_HELPER, "arrayStoreLong", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_DOUBLE = staticCall(JIT_HELPER, "arrayStoreDouble", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_INT = staticCall(JIT_HELPER, "arrayStoreInt", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_FLOAT = staticCall(JIT_HELPER, "arrayStoreFloat", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_CHAR = staticCall(JIT_HELPER, "arrayStoreChar", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_SHORT = staticCall(JIT_HELPER, "arrayStoreShort", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_BYTE = staticCall(JIT_HELPER, "arrayStoreByte", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_VALUE = staticCall(JIT_HELPER, "arrayStoreValue", J_VOID, VALUE, VALUE, VALUE, CTX);

	private static final Access ADD_INT = staticCall(JIT_HELPER, "addInt", VALUE, VALUE, VALUE);
	private static final Access ADD_LONG = staticCall(JIT_HELPER, "addLong", VALUE, VALUE, VALUE);
	private static final Access ADD_FLOAT = staticCall(JIT_HELPER, "addFloat", VALUE, VALUE, VALUE);
	private static final Access ADD_DOUBLE = staticCall(JIT_HELPER, "addDouble", VALUE, VALUE, VALUE);

	private static final Access SUB_INT = staticCall(JIT_HELPER, "subInt", VALUE, VALUE, VALUE);
	private static final Access SUB_LONG = staticCall(JIT_HELPER, "subLong", VALUE, VALUE, VALUE);
	private static final Access SUB_FLOAT = staticCall(JIT_HELPER, "subFloat", VALUE, VALUE, VALUE);
	private static final Access SUB_DOUBLE = staticCall(JIT_HELPER, "subDouble", VALUE, VALUE, VALUE);

	private static final Access MUL_INT = staticCall(JIT_HELPER, "mulInt", VALUE, VALUE, VALUE);
	private static final Access MUL_LONG = staticCall(JIT_HELPER, "mulLong", VALUE, VALUE, VALUE);
	private static final Access MUL_FLOAT = staticCall(JIT_HELPER, "mulFloat", VALUE, VALUE, VALUE);
	private static final Access MUL_DOUBLE = staticCall(JIT_HELPER, "mulDouble", VALUE, VALUE, VALUE);

	private static final Access DIV_INT = staticCall(JIT_HELPER, "divInt", VALUE, VALUE, VALUE);
	private static final Access DIV_LONG = staticCall(JIT_HELPER, "divLong", VALUE, VALUE, VALUE);
	private static final Access DIV_FLOAT = staticCall(JIT_HELPER, "divFloat", VALUE, VALUE, VALUE);
	private static final Access DIV_DOUBLE = staticCall(JIT_HELPER, "divDouble", VALUE, VALUE, VALUE);

	private static final Access REM_INT = staticCall(JIT_HELPER, "remInt", VALUE, VALUE, VALUE);
	private static final Access REM_LONG = staticCall(JIT_HELPER, "remLong", VALUE, VALUE, VALUE);
	private static final Access REM_FLOAT = staticCall(JIT_HELPER, "remFloat", VALUE, VALUE, VALUE);
	private static final Access REM_DOUBLE = staticCall(JIT_HELPER, "remDouble", VALUE, VALUE, VALUE);

	private static final Access SHL_INT = staticCall(JIT_HELPER, "shlInt", VALUE, VALUE, VALUE);
	private static final Access SHL_LONG = staticCall(JIT_HELPER, "shlLong", VALUE, VALUE, VALUE);
	private static final Access SHR_INT = staticCall(JIT_HELPER, "shrInt", VALUE, VALUE, VALUE);
	private static final Access SHR_LONG = staticCall(JIT_HELPER, "shrLong", VALUE, VALUE, VALUE);
	private static final Access USHR_INT = staticCall(JIT_HELPER, "ushrInt", VALUE, VALUE, VALUE);
	private static final Access USHR_LONG = staticCall(JIT_HELPER, "ushrLong", VALUE, VALUE, VALUE);
	private static final Access AND_INT = staticCall(JIT_HELPER, "andInt", VALUE, VALUE, VALUE);
	private static final Access AND_LONG = staticCall(JIT_HELPER, "andLong", VALUE, VALUE, VALUE);
	private static final Access OR_INT = staticCall(JIT_HELPER, "orInt", VALUE, VALUE, VALUE);
	private static final Access OR_LONG = staticCall(JIT_HELPER, "orLong", VALUE, VALUE, VALUE);
	private static final Access XOR_INT = staticCall(JIT_HELPER, "xorInt", VALUE, VALUE, VALUE);
	private static final Access XOR_LONG = staticCall(JIT_HELPER, "xorLong", VALUE, VALUE, VALUE);
	private static final Access LOCAL_INCREMENT = staticCall(JIT_HELPER, "localIncrement", J_VOID, LOCALS, J_INT, J_INT);
	private static final Access COMPARE_LONG = staticCall(JIT_HELPER, "compareLong", VALUE, VALUE, VALUE);
	private static final Access COMPARE_FLOAT = staticCall(JIT_HELPER, "compareFloat", VALUE, VALUE, VALUE, J_INT);
	private static final Access COMPARE_DOUBLE = staticCall(JIT_HELPER, "compareDouble", VALUE, VALUE, VALUE, J_INT);
	private static final Access PUT_STATIC = staticCall(JIT_HELPER, "putStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access GET_FIELD = staticCall(JIT_HELPER, "getField", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access PUT_FIELD = staticCall(JIT_HELPER, "putField", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_INTERFACE = staticCall(JIT_HELPER, "invokeInterface", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access NEW_INSTANCE = staticCall(JIT_HELPER, "allocateInstance", J_VOID, J_STRING, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY = staticCall(JIT_HELPER, "allocatePrimitiveArray", J_VOID, J_INT, CTX);
	private static final Access NEW_INSTANCE_ARRAY = staticCall(JIT_HELPER, "allocateValueArray", J_VOID, J_STRING, CTX);
	private static final Access GET_LENGTH = staticCall(JIT_HELPER, "getArrayLength", J_VOID, CTX);
	private static final Access THROW_EXCEPTION = staticCall(JIT_HELPER, "throwException", J_VOID, CTX);
	private static final Access CHECK_CAST = staticCall(JIT_HELPER, "checkCast", J_VOID, J_STRING, CTX);
	private static final Access INSTANCEOF_RES = staticCall(JIT_HELPER, "instanceofResult", J_VOID, J_STRING, CTX);
	private static final Access MONITOR_LOCK = staticCall(JIT_HELPER, "monitorEnter", J_VOID, CTX);
	private static final Access MONITOR_UNLOCK = staticCall(JIT_HELPER, "monitorExit", J_VOID, CTX);
	private static final Access NEW_MULTI_ARRAY = staticCall(JIT_HELPER, "multiNewArray", J_VOID, J_STRING, J_INT, CTX);
	private static final Access CLASS_LDC = staticCall(JIT_HELPER, "classLdc", VALUE, J_STRING, CTX);
	private static final Access METHOD_LDC = staticCall(JIT_HELPER, "methodLdc", VALUE, J_STRING, CTX);
	private static final Access INT_TO_BYTE = staticCall(JIT_HELPER, "intToByte", J_VOID, CTX);
	private static final Access INT_TO_CHAR = staticCall(JIT_HELPER, "intToChar", J_VOID, CTX);
	private static final Access INT_TO_SHORT = staticCall(JIT_HELPER, "intToShort", J_VOID, CTX);

	private static final Access GET_STATIC_LONG = staticCall(JIT_HELPER, "getStaticIntrinsicJ", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_DOUBLE = staticCall(JIT_HELPER, "getStaticIntrinsicD", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_INT = staticCall(JIT_HELPER, "getStaticIntrinsicI", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_FLOAT = staticCall(JIT_HELPER, "getStaticIntrinsicF", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_CHAR = staticCall(JIT_HELPER, "getStaticIntrinsicC", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_SHORT = staticCall(JIT_HELPER, "getStaticIntrinsicS", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_BYTE = staticCall(JIT_HELPER, "getStaticIntrinsicB", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_VALUE = staticCall(JIT_HELPER, "getStaticIntrinsicA", J_VOID, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_FAIL = staticCall(JIT_HELPER, "getStaticIntrinsicFail", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access GET_STATIC_SLOW = staticCall(JIT_HELPER, "getStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);

	private static final Access INVOKE_FAIL = staticCall(JIT_HELPER, "invokeFail", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access INVOKE_STATIC_INTRINSIC = staticCall(JIT_HELPER, "invokeStaticIntrinsic", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access INVOKE_SPECIAL_INTRINSIC = staticCall(JIT_HELPER, "invokeSpecialIntrinsic", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access INVOKE_VIRTUAL_INTRINSIC = staticCall(JIT_HELPER, "invokeVirtualIntrinsic", J_VOID, J_OBJECT, J_OBJECT, J_OBJECT, CTX);
	private static final Access INVOKE_STATIC_SLOW = staticCall(JIT_HELPER, "invokeStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_SPECIAL_SLOW = staticCall(JIT_HELPER, "invokeSpecial", J_VOID, J_STRING, J_STRING, J_STRING, CTX);

	private static final int CTX_SLOT = 1;
	private static final int LOCALS_SLOT = 2;
	private static final int STACK_SLOT = 3;
	private static final int HELPER_SLOT = 4;

	private final String className;
	private final JavaMethod target;
	private final MethodVisitor jit;
	private final Map<Object, Integer> constants = new LinkedHashMap<>();

	/**
	 * @param jm
	 * 		Method to check.
	 *
	 * @return {@code true} if method is compilable,
	 * {@code false} otherwise.
	 */
	public static boolean isCompilable(JavaMethod jm) {
		val node = jm.getNode();
		if (!node.tryCatchBlocks.isEmpty()) return false;
		val list = node.instructions;
		if (list.size() == 0) return false;
		for (AbstractInsnNode insn : list) {
			insn = unmask(insn);
			if (insn instanceof InvokeDynamicInsnNode) return false;
			int opc = insn.getOpcode();
			if (opc == JSR || opc == RET) return false;
		}
		return true;
	}

	/**
	 * Compiles method.
	 *
	 * @param jm
	 * 		Method for compilation.
	 *
	 * @return jit class info.
	 */
	public static JitClass compile(JavaMethod jm) {
		val target = jm.getNode();
		if (!target.tryCatchBlocks.isEmpty()) {
			throw new IllegalStateException("JIT does not support try/catch blocks yet");
		}
		val writer = new ClassWriter(3);
		val className = "dev/xdark/ssvm/jit/JitCode" + CLASS_ID.getAndIncrement();
		writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, className, null, "java/lang/Object", new String[]{"java/util/function/Consumer"});
		val init = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitVarInsn(ALOAD, 0);
		init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(RETURN);
		init.visitEnd();
		init.visitMaxs(1, 1);
		val jit = writer.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null);
		jit.visitCode();
		val compiler = new JitCompiler(className, jm, jit);
		compiler.compileInner();
		jit.visitEnd();
		jit.visitMaxs(-1, -1);
		// Leave some debug info.
		val owner = jm.getOwner();
		int infoAcc = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
		writer.visitField(infoAcc, "CLASS", "Ljava/lang/String;", null, owner.getInternalName());
		writer.visitField(infoAcc, "METHOD_NAME", "Ljava/lang/String;", null, jm.getName());
		writer.visitField(infoAcc, "METHOD_DESC", "Ljava/lang/String;", null, jm.getDesc());
		writer.visitSource(jm.toString(), null);
		val constants = compiler.constants.keySet();
		if (!constants.isEmpty()) {
			writer.visitField(ACC_PRIVATE | ACC_STATIC, "constants", "[Ljava/lang/Object;", null, null);
		}

		val bc = writer.toByteArray();
		return new JitClass(className, bc, constants.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(constants)));
	}

	private void compileInner() {
		val jit = this.jit;
		// Setup locals.
		loadCtx();
		cast(CTX);
		jit.visitVarInsn(ASTORE, CTX_SLOT);
		// Load locals.
		loadCtx();
		GET_LOCALS.emit(jit);
		jit.visitVarInsn(ASTORE, LOCALS_SLOT);
		// Load stack.
		loadCtx();
		GET_STACK.emit(jit);
		jit.visitVarInsn(ASTORE, STACK_SLOT);
		// Load helper.
		loadCtx();
		GET_HELPER.emit(jit);
		jit.visitVarInsn(ASTORE, HELPER_SLOT);

		val target = this.target;
		val node = target.getNode();
		val instructions = node.instructions;
		val copy = StreamSupport.stream(instructions.spliterator(), false)
				.filter(x -> x instanceof LabelNode)
				.collect(Collectors.toMap(x -> (LabelNode) x, __ -> new LabelNode()));
		val labels = copy.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getLabel()));

		// Process instructions.
		Label last = new Label();
		jit.visitLabel(last);
		int x = 0;
		for (AbstractInsnNode insn : instructions) {
			insn = unmask(insn);
			int opcode = insn.getOpcode();
			jit.visitLineNumber(x++, last);
			switch (opcode) {
				case -1:
					if (insn instanceof LabelNode) {
						jit.visitLabel(last = labels.get((LabelNode) insn));
					} else if (insn instanceof LineNumberNode) {
						loadCtx();
						jit.visitLdcInsn(((LineNumberNode) insn).line);
						SET_LINE.emit(jit);
					}
					break;
				case NOP:
					break;
				case ACONST_NULL:
					loadNull();
					push();
					break;
				case ICONST_M1:
				case ICONST_0:
				case ICONST_1:
				case ICONST_2:
				case ICONST_3:
				case ICONST_4:
				case ICONST_5:
					intOf(opcode - ICONST_0);
					push();
					break;
				case LCONST_0:
				case LCONST_1:
					longOf(opcode - LCONST_0);
					pushWide();
					break;
				case FCONST_0:
				case FCONST_1:
				case FCONST_2:
					floatOf(opcode - FCONST_0);
					push();
					break;
				case DCONST_0:
				case DCONST_1:
					doubleOf(opcode - DCONST_0);
					pushWide();
					break;
				case BIPUSH:
				case SIPUSH:
					intOf(((IntInsnNode) insn).operand);
					push();
					break;
				case LDC:
					val cst = ((LdcInsnNode) insn).cst;
					ldcOf(cst);
					if (isWide(cst)) {
						pushWide();
					} else {
						push();
					}
					break;
				case ILOAD:
				case FLOAD:
				case ALOAD:
					loadLocal(((VarInsnNode) insn).var);
					push();
					break;
				case LLOAD:
				case DLOAD:
					loadLocal(((VarInsnNode) insn).var);
					pushWide();
					break;
				case IALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_INT.emit(jit);
					push();
					break;
				case LALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_LONG.emit(jit);
					pushWide();
					break;
				case FALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_FLOAT.emit(jit);
					push();
					break;
				case DALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_DOUBLE.emit(jit);
					pushWide();
					break;
				case AALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_VALUE.emit(jit);
					push();
					break;
				case BALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_BYTE.emit(jit);
					push();
					break;
				case CALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_CHAR.emit(jit);
					push();
					break;
				case SALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_SHORT.emit(jit);
					push();
					break;
				case ISTORE:
				case FSTORE:
				case ASTORE:
					pop();
					setLocal(((VarInsnNode) insn).var);
					break;
				case LSTORE:
				case DSTORE:
					popWide();
					setLocal(((VarInsnNode) insn).var);
					break;
				case IASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_INT.emit(jit);
					break;
				case LASTORE:
					popWide(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_LONG.emit(jit);
					break;
				case FASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_FLOAT.emit(jit);
					break;
				case DASTORE:
					popWide(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_DOUBLE.emit(jit);
					break;
				case AASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_VALUE.emit(jit);
					break;
				case BASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_BYTE.emit(jit);
					break;
				case CASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_CHAR.emit(jit);
					break;
				case SASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jvm_swap(1, 2);
					loadCtx();
					ARR_STORE_SHORT.emit(jit);
					break;
				case Opcodes.POP:
					pop(true);
					break;
				case POP2:
					pop(true);
					pop(true);
					break;
				case Opcodes.DUP:
					dup();
					break;
				case Opcodes.DUP_X1:
					dupx1();
					break;
				case Opcodes.DUP_X2:
					dupx2();
					break;
				case Opcodes.DUP2:
					dup2();
					break;
				case Opcodes.DUP2_X1:
					dup2x1();
					break;
				case Opcodes.DUP2_X2:
					dup2x2();
					break;
				case Opcodes.SWAP:
					swap();
					break;
				case IADD:
					pop();
					pop();
					jvm_swap();
					ADD_INT.emit(jit);
					push();
					break;
				case LADD:
					popWide();
					popWide();
					jvm_swap();
					ADD_LONG.emit(jit);
					pushWide();
					break;
				case FADD:
					pop();
					pop();
					jvm_swap();
					ADD_FLOAT.emit(jit);
					push();
					break;
				case DADD:
					popWide();
					popWide();
					jvm_swap();
					ADD_DOUBLE.emit(jit);
					pushWide();
					break;
				case ISUB:
					pop();
					pop();
					jvm_swap();
					SUB_INT.emit(jit);
					push();
					break;
				case LSUB:
					popWide();
					popWide();
					jvm_swap();
					SUB_LONG.emit(jit);
					pushWide();
					break;
				case FSUB:
					pop();
					pop();
					jvm_swap();
					SUB_FLOAT.emit(jit);
					push();
					break;
				case DSUB:
					popWide();
					popWide();
					jvm_swap();
					SUB_DOUBLE.emit(jit);
					pushWide();
					break;
				case IMUL:
					pop();
					pop();
					jvm_swap();
					MUL_INT.emit(jit);
					push();
					break;
				case LMUL:
					popWide();
					popWide();
					jvm_swap();
					MUL_LONG.emit(jit);
					pushWide();
					break;
				case FMUL:
					pop();
					pop();
					jvm_swap();
					MUL_FLOAT.emit(jit);
					push();
					break;
				case DMUL:
					popWide();
					popWide();
					jvm_swap();
					MUL_DOUBLE.emit(jit);
					pushWide();
					break;
				case IDIV:
					pop();
					pop();
					jvm_swap();
					DIV_INT.emit(jit);
					push();
					break;
				case LDIV:
					popWide();
					popWide();
					jvm_swap();
					DIV_LONG.emit(jit);
					pushWide();
					break;
				case FDIV:
					pop();
					pop();
					jvm_swap();
					DIV_FLOAT.emit(jit);
					push();
					break;
				case DDIV:
					popWide();
					popWide();
					jvm_swap();
					DIV_DOUBLE.emit(jit);
					pushWide();
					break;
				case IREM:
					pop();
					pop();
					jvm_swap();
					REM_INT.emit(jit);
					push();
					break;
				case LREM:
					popWide();
					popWide();
					jvm_swap();
					REM_LONG.emit(jit);
					pushWide();
					break;
				case FREM:
					pop();
					pop();
					jvm_swap();
					REM_FLOAT.emit(jit);
					push();
					break;
				case DREM:
					popWide();
					popWide();
					jvm_swap();
					REM_DOUBLE.emit(jit);
					pushWide();
					break;
				case INEG:
					pop();
					AS_INT.emit(jit);
					jit.visitInsn(Opcodes.INEG);
					intOf();
					push();
					break;
				case LNEG:
					popWide();
					AS_LONG.emit(jit);
					jit.visitInsn(Opcodes.LNEG);
					longOf();
					pushWide();
					break;
				case FNEG:
					pop();
					AS_FLOAT.emit(jit);
					jit.visitInsn(Opcodes.FNEG);
					floatOf();
					push();
					break;
				case DNEG:
					popWide();
					AS_DOUBLE.emit(jit);
					jit.visitInsn(Opcodes.DNEG);
					doubleOf();
					pushWide();
					break;
				case ISHL:
					pop();
					pop();
					jvm_swap();
					SHL_INT.emit(jit);
					push();
					break;
				case LSHL:
					pop();
					popWide();
					jvm_swap();
					SHL_LONG.emit(jit);
					pushWide();
					break;
				case ISHR:
					pop();
					pop();
					jvm_swap();
					SHR_INT.emit(jit);
					push();
					break;
				case LSHR:
					pop();
					popWide();
					jvm_swap();
					SHR_LONG.emit(jit);
					pushWide();
					break;
				case IUSHR:
					pop();
					pop();
					jvm_swap();
					USHR_INT.emit(jit);
					push();
					break;
				case LUSHR:
					pop();
					popWide();
					jvm_swap();
					USHR_LONG.emit(jit);
					pushWide();
					break;
				case IAND:
					pop();
					pop();
					jvm_swap();
					AND_INT.emit(jit);
					push();
					break;
				case LAND:
					popWide();
					popWide();
					jvm_swap();
					AND_LONG.emit(jit);
					pushWide();
					break;
				case IOR:
					pop();
					pop();
					jvm_swap();
					OR_INT.emit(jit);
					push();
					break;
				case LOR:
					popWide();
					popWide();
					jvm_swap();
					OR_LONG.emit(jit);
					pushWide();
					break;
				case IXOR:
					pop();
					pop();
					jvm_swap();
					XOR_INT.emit(jit);
					push();
					break;
				case LXOR:
					popWide();
					popWide();
					jvm_swap();
					XOR_LONG.emit(jit);
					pushWide();
					break;
				case IINC:
					loadLocals();
					jit.visitLdcInsn(((IincInsnNode) insn).var);
					jit.visitLdcInsn(((IincInsnNode) insn).incr);
					LOCAL_INCREMENT.emit(jit);
					break;
				case I2L:
				case F2L:
					pop();
					AS_LONG.emit(jit);
					longOf();
					pushWide();
					break;
				case I2F:
					pop();
					AS_FLOAT.emit(jit);
					floatOf();
					push();
					break;
				case I2D:
				case F2D:
					pop();
					AS_DOUBLE.emit(jit);
					doubleOf();
					pushWide();
					break;
				case L2I:
				case D2I:
					popWide();
					AS_INT.emit(jit);
					intOf();
					push();
					break;
				case L2F:
				case D2F:
					popWide();
					AS_FLOAT.emit(jit);
					floatOf();
					push();
					break;
				case L2D:
					popWide();
					AS_DOUBLE.emit(jit);
					doubleOf();
					pushWide();
					break;
				case F2I:
					pop();
					AS_INT.emit(jit);
					intOf();
					push();
					break;
				case D2L:
					popWide();
					AS_LONG.emit(jit);
					longOf();
					pushWide();
					break;
				case I2B:
					loadCtx();
					INT_TO_BYTE.emit(jit);
					break;
				case I2C:
					loadCtx();
					INT_TO_CHAR.emit(jit);
					break;
				case I2S:
					loadCtx();
					INT_TO_SHORT.emit(jit);
					break;
				case LCMP:
					popWide();
					popWide();
					jvm_swap();
					COMPARE_LONG.emit(jit);
					push();
					break;
				case FCMPL:
				case FCMPG:
					pop();
					pop();
					jvm_swap();
					jit.visitLdcInsn(opcode == FCMPL ? -1 : 1);
					COMPARE_FLOAT.emit(jit);
					push();
					break;
				case DCMPL:
				case DCMPG:
					popWide();
					popWide();
					jvm_swap();
					jit.visitLdcInsn(opcode == DCMPL ? -1 : 1);
					COMPARE_DOUBLE.emit(jit);
					push();
					break;
				case IFEQ:
				case IFNE:
				case IFLT:
				case IFGE:
				case IFGT:
				case IFLE:
					pop();
					AS_INT.emit(jit);
					jit.visitJumpInsn(opcode, labels.get(((JumpInsnNode) insn).label));
					break;
				case IF_ICMPEQ:
				case IF_ICMPNE:
				case IF_ICMPLT:
				case IF_ICMPGE:
				case IF_ICMPGT:
				case IF_ICMPLE:
					pop();
					AS_INT.emit(jit);
					pop();
					AS_INT.emit(jit);
					jvm_swap();
					jit.visitJumpInsn(opcode, labels.get(((JumpInsnNode) insn).label));
					break;
				case IF_ACMPEQ:
				case IF_ACMPNE:
					pop();
					pop();
					jit.visitJumpInsn(opcode, labels.get(((JumpInsnNode) insn).label));
					break;
				case GOTO:
					jit.visitJumpInsn(GOTO, labels.get(((JumpInsnNode) insn).label));
					break;
				case JSR:
				case RET:
					throw new IllegalStateException("TODO");
				case TABLESWITCH:
				case LOOKUPSWITCH:
					pop();
					AS_INT.emit(jit);
					insn.clone(copy).accept(jit);
					break;
				case IRETURN:
				case FRETURN:
				case ARETURN:
					loadCtx();
					pop();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case LRETURN:
				case DRETURN:
					loadCtx();
					popWide();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case RETURN:
					jit.visitInsn(RETURN);
					break;
				case GETSTATIC:
					getStatic((FieldInsnNode) insn);
					break;
				case PUTSTATIC:
					pushField((FieldInsnNode) insn);
					loadCtx();
					PUT_STATIC.emit(jit);
					break;
				case GETFIELD:
					pushField((FieldInsnNode) insn);
					loadCtx();
					GET_FIELD.emit(jit);
					break;
				case PUTFIELD:
					pushField((FieldInsnNode) insn);
					loadCtx();
					PUT_FIELD.emit(jit);
					break;
				case INVOKEVIRTUAL:
					invokeVirtual((MethodInsnNode) insn);
					break;
				case INVOKESPECIAL:
					invokeSpecial((MethodInsnNode) insn);
					break;
				case INVOKESTATIC:
					invokeStatic((MethodInsnNode) insn);
					break;
				case INVOKEINTERFACE:
					pushMethod((MethodInsnNode) insn);
					loadCtx();
					INVOKE_INTERFACE.emit(jit);
					break;
				case NEW:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					NEW_INSTANCE.emit(jit);
					break;
				case NEWARRAY:
					jit.visitLdcInsn(((IntInsnNode) insn).operand);
					loadCtx();
					NEW_PRIMITIVE_ARRAY.emit(jit);
					break;
				case ANEWARRAY:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					NEW_INSTANCE_ARRAY.emit(jit);
					break;
				case ARRAYLENGTH:
					loadCtx();
					GET_LENGTH.emit(jit);
					break;
				case ATHROW:
					loadCtx();
					THROW_EXCEPTION.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case CHECKCAST:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					CHECK_CAST.emit(jit);
					break;
				case INSTANCEOF:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					INSTANCEOF_RES.emit(jit);
					break;
				case MONITORENTER:
					loadCtx();
					MONITOR_LOCK.emit(jit);
					break;
				case MONITOREXIT:
					loadCtx();
					MONITOR_UNLOCK.emit(jit);
					break;
				case MULTIANEWARRAY:
					val array = (MultiANewArrayInsnNode) insn;
					jit.visitLdcInsn(array.desc);
					jit.visitLdcInsn(array.dims);
					loadCtx();
					NEW_MULTI_ARRAY.emit(jit);
					break;
				case IFNULL:
					pop();
					IS_NULL.emit(jit);
					jit.visitJumpInsn(IFNE, labels.get(((JumpInsnNode) insn).label));
					break;
				case IFNONNULL:
					pop();
					IS_NULL.emit(jit);
					jit.visitJumpInsn(IFEQ, labels.get(((JumpInsnNode) insn).label));
					break;
				case INVOKEDYNAMIC:
					throw new IllegalStateException("JIT does not support InvokeDynamic");
			}
		}
	}

	private void loadCtx() {
		jit.visitVarInsn(ALOAD, CTX_SLOT);
	}

	private void loadStack() {
		jit.visitVarInsn(ALOAD, STACK_SLOT);
	}

	private void loadLocals() {
		jit.visitVarInsn(ALOAD, LOCALS_SLOT);
	}

	private void loadHelper() {
		jit.visitVarInsn(ALOAD, HELPER_SLOT);
	}

	private void cast(ClassType type) {
		jit.visitTypeInsn(CHECKCAST, type.internalName);
	}

	private void newObj(ClassType type) {
		jit.visitTypeInsn(NEW, type.internalName);
	}

	private void pop(boolean discard) {
		loadStack();
		POP.emit(jit);
		if (discard) {
			jit.visitInsn(Opcodes.POP);
		}
	}

	private void pop() {
		pop(false);
	}

	private void popWide(boolean discard) {
		loadStack();
		POP_WIDE.emit(jit);
		if (discard) {
			jit.visitInsn(Opcodes.POP);
		}
	}

	private void popWide() {
		popWide(false);
	}

	private void push() {
		// value
		loadStack(); // value stack
		jit.visitInsn(Opcodes.SWAP);
		PUSH.emit(jit);
	}

	private void pushWide() {
		// value
		loadStack(); // value stack
		jit.visitInsn(Opcodes.SWAP);
		PUSH_WIDE.emit(jit);
	}

	private void pushGeneric() {
		// value
		loadStack(); // value stack
		jit.visitInsn(Opcodes.SWAP);
		PUSH_GENERIC.emit(jit);
	}

	private void dup() {
		loadStack();
		DUP.emit(jit);
	}

	private void dupx1() {
		loadStack();
		DUP_X1.emit(jit);
	}

	private void dupx2() {
		loadStack();
		DUP_X2.emit(jit);
	}

	private void dup2() {
		loadStack();
		DUP2.emit(jit);
	}

	private void dup2x1() {
		loadStack();
		DUP2_X1.emit(jit);
	}

	private void dup2x2() {
		loadStack();
		DUP2_X2.emit(jit);
	}

	private void swap() {
		loadStack();
		SWAP.emit(jit);
	}

	private void loadLocal(int idx) {
		val jit = this.jit;
		loadLocals();
		jit.visitLdcInsn(idx);
		LOAD.emit(jit);
	}

	private void setLocal(int idx) {
		val jit = this.jit;
		// value
		loadLocals(); // value locals
		jit.visitInsn(Opcodes.SWAP); // locals value
		jit.visitLdcInsn(idx);
		jit.visitInsn(Opcodes.SWAP); // locals idx value
		STORE.emit(jit);
	}

	private void loadNull() {
		GET_NULL.emit(jit);
	}

	private void intOf() {
		INT_OF.emit(jit);
	}

	private void intOf(int v) {
		jit.visitLdcInsn(v);
		intOf();
	}

	private void longOf() {
		LONG_OF.emit(jit);
	}

	private void longOf(long v) {
		jit.visitLdcInsn(v);
		longOf();
	}

	private void floatOf() {
		val jit = this.jit;
		// value
		newObj(FLOAT); // value wrapper
		jit.visitInsn(Opcodes.DUP_X1);
		jit.visitInsn(Opcodes.SWAP); // wrapper wrapper value
		FLOAT_OF.emit(jit);
	}

	private void floatOf(float v) {
		jit.visitLdcInsn(v);
		floatOf();
	}

	private void doubleOf() {
		val jit = this.jit;
		// value
		newObj(DOUBLE); // value wrapper
		jit.visitInsn(Opcodes.DUP); // value wrwapper wrapper
		jvm_swap(2, 2); // wrwapper wrapper value
		DOUBLE_OF.emit(jit);
	}

	private void doubleOf(double v) {
		jit.visitLdcInsn(v);
		doubleOf();
	}

	private void ldcOf(Object value) {
		if (value instanceof Long) {
			loadCompilerConstant(LongValue.of((Long) value));
			cast(VALUE);
		} else if (value instanceof Double) {
			loadCompilerConstant(new DoubleValue((Double) value));
			cast(VALUE);
		} else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
			loadCompilerConstant(IntValue.of(((Number) value).intValue()));
			cast(VALUE);
		} else if (value instanceof Character) {
			loadCompilerConstant(IntValue.of((Character) value));
			cast(VALUE);
		} else if (value instanceof Float) {
			loadCompilerConstant(new FloatValue((Float) value));
			cast(VALUE);
		} else if (value instanceof Type) {
			val jit = this.jit;
			val type = (Type) value;
			if (type.getSort() == Type.METHOD) {
				jit.visitLdcInsn(type.getDescriptor());
				loadCtx();
				METHOD_LDC.emit(jit);
			} else {
				jit.visitLdcInsn(type.getInternalName());
				loadCtx();
				CLASS_LDC.emit(jit);
			}
		} else {
			val jit = this.jit;
			loadHelper();
			jit.visitLdcInsn(value);
			VALUE_FROM_LDC.emit(jit);
		}
	}

	private void pushField(FieldInsnNode field) {
		val jit = this.jit;
		jit.visitLdcInsn(field.owner);
		jit.visitLdcInsn(field.name);
		jit.visitLdcInsn(field.desc);
	}

	private void pushMethod(MethodInsnNode method) {
		val jit = this.jit;
		jit.visitLdcInsn(method.owner);
		jit.visitLdcInsn(method.name);
		jit.visitLdcInsn(method.desc);
	}

	private void jvm_swap(int right, int left) {
		val jit = this.jit;
		if (right == 1) {
			if (left == 1) {
				jit.visitInsn(Opcodes.SWAP);
			} else if (left == 2) {
				jit.visitInsn(Opcodes.DUP_X2);
				jit.visitInsn(Opcodes.POP);
			} else {
				throw new IllegalStateException("Not implemented for stackTop=" + right + ", belowTop=" + left);
			}
		} else if (right == 2) {
			if (left == 1) {
				jit.visitInsn(Opcodes.DUP2_X1);
				jit.visitInsn(Opcodes.POP2);
			} else if (left == 2) {
				jit.visitInsn(Opcodes.DUP2_X2);
				jit.visitInsn(Opcodes.POP2);
			} else {
				throw new IllegalStateException("Not implemented for stackTop=" + right + ", belowTop=" + left);
			}
		} else {
			throw new IllegalStateException("Not implemented for stackTop=" + right);
		}
	}

	private void jvm_swap() {
		jit.visitInsn(Opcodes.SWAP);
	}

	private void loadCompilerConstant(Object value) {
		val constants = this.constants;
		Integer constant = constants.get(value);
		val jit = this.jit;
		if (constant == null) {
			constant = constants.size();
			constants.put(value, constant);
			jit.visitLdcInsn("const " + constant + " = " + (value instanceof Object[] ? Arrays.toString((Object[]) value) : value));
			jit.visitInsn(Opcodes.POP);
		}
		jit.visitFieldInsn(GETSTATIC, className, "constants", "[Ljava/lang/Object;");
		jit.visitLdcInsn(constant);
		jit.visitInsn(AALOAD);
	}

	private void getStatic(FieldInsnNode node) {
		val jit = this.jit;
		Access access;
		lookup:
		{
			try {
				val target = this.target;
				val owner = target.getOwner();
				val vm = owner.getVM();
				InstanceJavaClass jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
				val name = node.name;
				val desc = node.desc;
				while (jc != null) {
					val field = jc.getStaticField(name, desc);
					if (field != null) {
						long offset = vm.getMemoryManager().getStaticOffset(jc) + field.getOffset();
						loadCompilerConstant(jc);
						jit.visitLdcInsn(offset);
						switch (field.getType().getSort()) {
							case Type.LONG:
								access = GET_STATIC_LONG;
								break;
							case Type.DOUBLE:
								access = GET_STATIC_DOUBLE;
								break;
							case Type.INT:
								access = GET_STATIC_INT;
								break;
							case Type.FLOAT:
								access = GET_STATIC_FLOAT;
								break;
							case Type.CHAR:
								access = GET_STATIC_CHAR;
								break;
							case Type.SHORT:
								access = GET_STATIC_SHORT;
								break;
							case Type.BYTE:
							case Type.BOOLEAN:
								access = GET_STATIC_BYTE;
								break;
							default:
								access = GET_STATIC_VALUE;
								break;
						}
						break lookup;
					}
					jc = jc.getSuperclassWithoutResolving();
				}
				// Field was not found.
				jit.visitInsn(ACONST_NULL);
				jit.visitLdcInsn(node.name);
				access = GET_STATIC_FAIL;
			} catch (VMException ex) {
				// Class was probably not found.
				// We need to use fallback path
				// because the may be defined right in the code
				// we are JITting.
				getStaticSlow(node);
				return;
			}
		}
		loadCtx();
		access.emit(jit);
	}

	private void getStaticSlow(FieldInsnNode node) {
		pushField(node);
		loadCtx();
		GET_STATIC_SLOW.emit(jit);
	}

	private void invokeStatic(MethodInsnNode node) {
		val jit = this.jit;
		Access access;
		try {
			val target = this.target;
			val owner = target.getOwner();
			val vm = owner.getVM();
			val jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
			val name = node.name;
			val desc = node.desc;
			val mn = jc.getStaticMethodRecursively(name, desc);
			if (mn == null) {
				jit.visitInsn(ACONST_NULL);
				jit.visitLdcInsn(node.owner + name + desc);
				access = INVOKE_FAIL;
			} else {
				loadCompilerConstant(jc);
				loadCompilerConstant(mn);
				access = INVOKE_STATIC_INTRINSIC;
			}
		} catch (VMException ex) {
			// Class was probably not found.
			// We need to use fallback path
			// because the may be defined right in the code
			// we are JITting.
			invokeStaticSlow(node);
			return;
		}
		loadCtx();
		access.emit(jit);
	}

	private void invokeStaticSlow(MethodInsnNode node) {
		pushMethod(node);
		loadCtx();
		INVOKE_STATIC_SLOW.emit(jit);
	}

	private void invokeSpecial(MethodInsnNode node) {
		val jit = this.jit;
		Access access;
		try {
			val target = this.target;
			val owner = target.getOwner();
			val vm = owner.getVM();
			val jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
			val name = node.name;
			val desc = node.desc;
			JavaMethod mn = jc.getVirtualMethodRecursively(name, desc);
			if (mn == null && jc.isInterface()) {
				mn = jc.getInterfaceMethodRecursively(name, desc);
			}
			if (mn == null) {
				jit.visitInsn(ACONST_NULL);
				jit.visitLdcInsn(node.owner + name + desc);
				access = INVOKE_FAIL;
			} else {
				loadCompilerConstant(jc);
				loadCompilerConstant(mn);
				access = INVOKE_SPECIAL_INTRINSIC;
			}
		} catch (VMException ex) {
			// Class was probably not found.
			// We need to use fallback path
			// because the may be defined right in the code
			// we are JITting.
			invokeSpecialSlow(node);
			return;
		}
		loadCtx();
		access.emit(jit);
	}

	private void invokeSpecialSlow(MethodInsnNode node) {
		pushMethod(node);
		loadCtx();
		INVOKE_SPECIAL_SLOW.emit(jit);
	}

	private void invokeVirtual(MethodInsnNode node) {
		loadCompilerConstant(node.name);
		val desc = node.desc;
		loadCompilerConstant(desc);
		loadCompilerConstant(Type.getArgumentTypes(desc));
		loadCtx();
		INVOKE_VIRTUAL_INTRINSIC.emit(jit);
	}

	private static boolean isWide(Object cst) {
		return cst instanceof Long || cst instanceof Double;
	}

	private static AbstractInsnNode unmask(AbstractInsnNode insnNode) {
		if (insnNode instanceof DelegatingInsnNode) {
			return ((DelegatingInsnNode<?>) insnNode).getDelegate();
		}
		return insnNode;
	}

	private static Access staticCall(ClassType owner, String name, ClassType rt, ClassType... args) {
		return new Access(INVOKESTATIC, owner, name, rt, args);
	}

	private static Access specialCall(ClassType owner, String name, ClassType rt, ClassType... args) {
		return new Access(INVOKESPECIAL, owner, name, rt, args);
	}

	private static Access virtualCall(ClassType owner, String name, ClassType rt, ClassType... args) {
		return new Access(INVOKEVIRTUAL, owner, name, rt, args);
	}

	private static Access interfaceCall(ClassType owner, String name, ClassType rt, ClassType... args) {
		return new Access(INVOKEINTERFACE, owner, name, rt, args);
	}

	private static Access getStatic(ClassType owner, String name, ClassType rt) {
		return new Access(GETSTATIC, owner, name, rt);
	}

	private static final class ClassType {

		final String internalName;
		final String desc;

		private ClassType(Class<?> klass) {
			internalName = Type.getInternalName(klass);
			desc = Type.getDescriptor(klass);
		}

		static ClassType of(Class<?> klass) {
			return new ClassType(klass);
		}
	}

	@RequiredArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	private static final class Access {

		int opcode;
		String owner;
		String name;
		String desc;

		Access(int opcode, ClassType owner, String name, ClassType rt, ClassType... args) {
			this(opcode, owner.internalName, name, toDescriptor(opcode >= GETSTATIC && opcode <= PUTFIELD, rt, args));
		}

		void emit(MethodVisitor visitor) {
			val opcode = this.opcode;
			if (opcode >= INVOKEVIRTUAL && opcode <= INVOKEINTERFACE) {
				visitor.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE);
			} else {
				visitor.visitFieldInsn(opcode, owner, name, desc);
			}
		}

		private static String toDescriptor(boolean field, ClassType rt, ClassType[] args) {
			if (field) {
				// assert args.length == 0;
				return rt.desc;
			}
			val b = new StringBuilder();
			b.append('(');
			for (val arg : args) b.append(arg.desc);
			return b.append(')').append(rt.desc).toString();
		}
	}
}
