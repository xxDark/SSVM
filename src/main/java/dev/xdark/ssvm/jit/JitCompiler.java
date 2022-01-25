package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.Opcodes.*;

/**
 * "JIT" compiler.
 *
 * @author xDark
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class JitCompiler {

	private static final AtomicInteger CLASS_ID = new AtomicInteger();
	private static final Type J_VOID = Type.of(void.class);
	private static final Type J_LONG = Type.of(long.class);
	private static final Type J_INT = Type.of(int.class);
	private static final Type J_DOUBLE = Type.of(double.class);
	private static final Type J_FLOAT = Type.of(float.class);
	private static final Type J_CHAR = Type.of(char.class);
	private static final Type J_SHORT = Type.of(short.class);
	private static final Type J_BYTE = Type.of(byte.class);
	private static final Type J_BOOLEAN = Type.of(boolean.class);
	private static final Type J_OBJECT = Type.of(Object.class);
	private static final Type J_STRING = Type.of(String.class);

	private static final Type CTX = Type.of(ExecutionContext.class);
	private static final Type LOCALS = Type.of(Locals.class);
	private static final Type STACK = Type.of(Stack.class);
	private static final Type VALUE = Type.of(Value.class);
	private static final Type NULL = Type.of(NullValue.class);
	private static final Type INT = Type.of(IntValue.class);
	private static final Type LONG = Type.of(LongValue.class);
	private static final Type FLOAT = Type.of(FloatValue.class);
	private static final Type DOUBLE = Type.of(DoubleValue.class);
	private static final Type VM_HELPER = Type.of(VMHelper.class);
	private static final Type JIT_HELPER = Type.of(JitHelper.class);

	// ctx methods
	private static final Access GET_LOCALS = Access.virtualCall(CTX, "getLocals", LOCALS);
	private static final Access GET_STACK = Access.virtualCall(CTX, "getStack", STACK);
	private static final Access GET_HELPER = Access.virtualCall(CTX, "getHelper", VM_HELPER);
	private static final Access SET_RESULT = Access.virtualCall(CTX, "setResult", J_VOID, VALUE);

	// stack methods
	private static final Access PUSH = Access.virtualCall(STACK, "push", J_VOID, VALUE);
	private static final Access PUSH_WIDE = Access.virtualCall(STACK, "pushWide", J_VOID, VALUE);
	private static final Access PUSH_GENERIC = Access.virtualCall(STACK, "pushGeneric", J_VOID, VALUE);
	private static final Access POP = Access.virtualCall(STACK, "pop", VALUE);
	private static final Access POP_WIDE = Access.virtualCall(STACK, "popWide", VALUE);
	private static final Access DUP = Access.virtualCall(STACK, "dup", J_VOID);
	private static final Access DUP_X1 = Access.virtualCall(STACK, "dupx1", J_VOID);
	private static final Access DUP_X2 = Access.virtualCall(STACK, "dupx2", J_VOID);
	private static final Access DUP2 = Access.virtualCall(STACK, "dup2", J_VOID);
	private static final Access DUP2_X1 = Access.virtualCall(STACK, "dup2x1", J_VOID);
	private static final Access DUP2_X2 = Access.virtualCall(STACK, "dup2x2", J_VOID);
	private static final Access SWAP = Access.virtualCall(STACK, "swap", J_VOID);

	// locals methods
	private static final Access LOAD = Access.virtualCall(LOCALS, "load", VALUE, J_INT);
	private static final Access STORE = Access.virtualCall(LOCALS, "set", J_VOID, J_INT, VALUE);

	// value static methods
	private static final Access GET_NULL = Access.getStatic(NULL, "INSTANCE", NULL);
	private static final Access INT_OF = Access.staticCall(INT, "of", INT, J_INT);
	private static final Access LONG_OF = Access.staticCall(LONG, "of", LONG, J_LONG);
	private static final Access FLOAT_OF = Access.specialCall(FLOAT, "<init>", J_VOID, J_FLOAT);
	private static final Access DOUBLE_OF = Access.specialCall(DOUBLE, "<init>", J_VOID, J_DOUBLE);

	// value methods
	private static final Access AS_LONG = Access.interfaceCall(VALUE, "asLong", J_LONG);
	private static final Access AS_DOUBLE = Access.interfaceCall(VALUE, "asDouble", J_DOUBLE);
	private static final Access AS_INT = Access.interfaceCall(VALUE, "asInt", J_INT);
	private static final Access AS_FLOAT = Access.interfaceCall(VALUE, "asFloat", J_FLOAT);
	private static final Access AS_CHAR = Access.interfaceCall(VALUE, "asChar", J_CHAR);
	private static final Access AS_SHORT = Access.interfaceCall(VALUE, "asShort", J_SHORT);
	private static final Access AS_BYTE = Access.interfaceCall(VALUE, "asByte", J_BYTE);
	private static final Access IS_NULL = Access.interfaceCall(VALUE, "isNull", J_BOOLEAN);

	// helper methods
	private static final Access VALUE_FROM_LDC = Access.virtualCall(VM_HELPER, "valueFromLdc", VALUE, J_OBJECT);

	// jit methods
	private static final Access ARR_LOAD_LONG = Access.staticCall(JIT_HELPER, "arrayLoadLong", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_DOUBLE = Access.staticCall(JIT_HELPER, "arrayLoadDouble", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_INT = Access.staticCall(JIT_HELPER, "arrayLoadInt", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_FLOAT = Access.staticCall(JIT_HELPER, "arrayLoadFloat", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_CHAR = Access.staticCall(JIT_HELPER, "arrayLoadChar", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_SHORT = Access.staticCall(JIT_HELPER, "arrayLoadShort", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_BYTE = Access.staticCall(JIT_HELPER, "arrayLoadByte", VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_LOAD_VALUE = Access.staticCall(JIT_HELPER, "arrayLoadValue", VALUE, VALUE, VALUE, CTX);

	private static final Access ARR_STORE_LONG = Access.staticCall(JIT_HELPER, "arrayStoreLong", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_DOUBLE = Access.staticCall(JIT_HELPER, "arrayStoreDouble", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_INT = Access.staticCall(JIT_HELPER, "arrayStoreInt", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_FLOAT = Access.staticCall(JIT_HELPER, "arrayStoreFloat", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_CHAR = Access.staticCall(JIT_HELPER, "arrayStoreChar", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_SHORT = Access.staticCall(JIT_HELPER, "arrayStoreShort", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_BYTE = Access.staticCall(JIT_HELPER, "arrayStoreByte", J_VOID, VALUE, VALUE, VALUE, CTX);
	private static final Access ARR_STORE_VALUE = Access.staticCall(JIT_HELPER, "arrayStoreValue", J_VOID, VALUE, VALUE, VALUE, CTX);

	private static final Access ADD_INT = Access.staticCall(JIT_HELPER, "addInt", VALUE, VALUE, VALUE);
	private static final Access ADD_LONG = Access.staticCall(JIT_HELPER, "addLong", VALUE, VALUE, VALUE);
	private static final Access ADD_FLOAT = Access.staticCall(JIT_HELPER, "addFloat", VALUE, VALUE, VALUE);
	private static final Access ADD_DOUBLE = Access.staticCall(JIT_HELPER, "addDouble", VALUE, VALUE, VALUE);

	private static final Access SUB_INT = Access.staticCall(JIT_HELPER, "subInt", VALUE, VALUE, VALUE);
	private static final Access SUB_LONG = Access.staticCall(JIT_HELPER, "subLong", VALUE, VALUE, VALUE);
	private static final Access SUB_FLOAT = Access.staticCall(JIT_HELPER, "subFloat", VALUE, VALUE, VALUE);
	private static final Access SUB_DOUBLE = Access.staticCall(JIT_HELPER, "subDouble", VALUE, VALUE, VALUE);

	private static final Access MUL_INT = Access.staticCall(JIT_HELPER, "mulInt", VALUE, VALUE, VALUE);
	private static final Access MUL_LONG = Access.staticCall(JIT_HELPER, "mulLong", VALUE, VALUE, VALUE);
	private static final Access MUL_FLOAT = Access.staticCall(JIT_HELPER, "mulFloat", VALUE, VALUE, VALUE);
	private static final Access MUL_DOUBLE = Access.staticCall(JIT_HELPER, "mulDouble", VALUE, VALUE, VALUE);

	private static final Access DIV_INT = Access.staticCall(JIT_HELPER, "divInt", VALUE, VALUE, VALUE);
	private static final Access DIV_LONG = Access.staticCall(JIT_HELPER, "divLong", VALUE, VALUE, VALUE);
	private static final Access DIV_FLOAT = Access.staticCall(JIT_HELPER, "divFloat", VALUE, VALUE, VALUE);
	private static final Access DIV_DOUBLE = Access.staticCall(JIT_HELPER, "divDouble", VALUE, VALUE, VALUE);

	private static final Access REM_INT = Access.staticCall(JIT_HELPER, "remInt", VALUE, VALUE, VALUE);
	private static final Access REM_LONG = Access.staticCall(JIT_HELPER, "remLong", VALUE, VALUE, VALUE);
	private static final Access REM_FLOAT = Access.staticCall(JIT_HELPER, "remFloat", VALUE, VALUE, VALUE);
	private static final Access REM_DOUBLE = Access.staticCall(JIT_HELPER, "remDouble", VALUE, VALUE, VALUE);

	private static final Access SHL_INT = Access.staticCall(JIT_HELPER, "shlInt", VALUE, VALUE, VALUE);
	private static final Access SHL_LONG = Access.staticCall(JIT_HELPER, "shlLong", VALUE, VALUE, VALUE);
	private static final Access SHR_INT = Access.staticCall(JIT_HELPER, "shrInt", VALUE, VALUE, VALUE);
	private static final Access SHR_LONG = Access.staticCall(JIT_HELPER, "shrLong", VALUE, VALUE, VALUE);
	private static final Access USHR_INT = Access.staticCall(JIT_HELPER, "ushrInt", VALUE, VALUE, VALUE);
	private static final Access USHR_LONG = Access.staticCall(JIT_HELPER, "ushrLong", VALUE, VALUE, VALUE);
	private static final Access AND_INT = Access.staticCall(JIT_HELPER, "andInt", VALUE, VALUE, VALUE);
	private static final Access AND_LONG = Access.staticCall(JIT_HELPER, "andLong", VALUE, VALUE, VALUE);
	private static final Access OR_INT = Access.staticCall(JIT_HELPER, "orInt", VALUE, VALUE, VALUE);
	private static final Access OR_LONG = Access.staticCall(JIT_HELPER, "orLong", VALUE, VALUE, VALUE);
	private static final Access XOR_INT = Access.staticCall(JIT_HELPER, "xorInt", VALUE, VALUE, VALUE);
	private static final Access XOR_LONG = Access.staticCall(JIT_HELPER, "xorLong", VALUE, VALUE, VALUE);
	private static final Access LOCAL_INCREMENT = Access.staticCall(JIT_HELPER, "localIncrement", J_VOID, LOCALS, J_INT, J_INT);
	private static final Access COMPARE_LONG = Access.staticCall(JIT_HELPER, "compareLong", VALUE, VALUE, VALUE);
	private static final Access COMPARE_FLOAT = Access.staticCall(JIT_HELPER, "compareFloat", VALUE, VALUE, VALUE, J_INT);
	private static final Access COMPARE_DOUBLE = Access.staticCall(JIT_HELPER, "compareDouble", VALUE, VALUE, VALUE, J_INT);
	private static final Access GET_STATIC = Access.staticCall(JIT_HELPER, "getStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access PUT_STATIC = Access.staticCall(JIT_HELPER, "getStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access GET_FIELD = Access.staticCall(JIT_HELPER, "getField", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access PUT_FIELD = Access.staticCall(JIT_HELPER, "getField", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_VIRTUAL = Access.staticCall(JIT_HELPER, "invokeVirtual", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_SPECIAL = Access.staticCall(JIT_HELPER, "invokeSpecial", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_STATIC = Access.staticCall(JIT_HELPER, "invokeStatic", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_INTERFACE = Access.staticCall(JIT_HELPER, "invokeInterface", J_VOID, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access NEW_INSTANCE = Access.staticCall(JIT_HELPER, "allocateInstance", J_VOID, J_STRING, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY = Access.staticCall(JIT_HELPER, "allocatePrimitiveArray", J_VOID, J_INT, CTX);
	private static final Access NEW_INSTANCE_ARRAY = Access.staticCall(JIT_HELPER, "allocateValueArray", J_VOID, J_STRING, CTX);
	private static final Access GET_LENGTH = Access.staticCall(JIT_HELPER, "getArrayLength", J_VOID, CTX);
	private static final Access THROW_EXCEPTION = Access.staticCall(JIT_HELPER, "throwException", J_VOID, CTX);
	private static final Access CHECK_CAST = Access.staticCall(JIT_HELPER, "checkCast", J_VOID, CTX);
	private static final Access INSTANCEOF_RES = Access.staticCall(JIT_HELPER, "instanceofResult", J_VOID, CTX);
	private static final Access MONITOR_LOCK = Access.staticCall(JIT_HELPER, "monitorEnter", J_VOID, CTX);
	private static final Access MONITOR_UNLOCK = Access.staticCall(JIT_HELPER, "monitorExit", J_VOID, CTX);
	private static final Access NEW_MULTI_ARRAY = Access.staticCall(JIT_HELPER, "multiNewArray", J_VOID, J_STRING, J_INT, CTX);

	private static final int CTX_SLOT = 1;
	private static final int LOCALS_SLOT = 2;
	private static final int STACK_SLOT = 3;
	private static final int HELPER_SLOT = 4;

	private final MethodNode target;
	private final MethodVisitor jit;

	/**
	 * Compiles method.
	 *
	 * @param jm
	 * 		Method for compilation.
	 *
	 * @return class bytecode.
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
		new JitCompiler(target, jit).compileInner();
		jit.visitEnd();
		jit.visitMaxs(-1, -1);
		val bc = writer.toByteArray();
		return new JitClass(className, bc);
	}

	private void compileInner() {
		val jit = this.jit;
		// Setup locals.
		loadCtx();
		cast(CTX);
		jit.visitVarInsn(ASTORE, CTX_SLOT);
		// Load locals.
		loadCtx();
		GET_LOCALS.emmit(jit);
		jit.visitVarInsn(ASTORE, LOCALS_SLOT);
		// Load stack.
		loadCtx();
		GET_STACK.emmit(jit);
		jit.visitVarInsn(ASTORE, STACK_SLOT);
		// Load helper.
		loadCtx();
		GET_HELPER.emmit(jit);
		jit.visitVarInsn(ASTORE, HELPER_SLOT);

		val instructions = target.instructions;
		val copy = StreamSupport.stream(instructions.spliterator(), false)
				.filter(x -> x instanceof LabelNode)
				.collect(Collectors.toMap(x -> (LabelNode) x, __ -> new LabelNode()));
		val labels = copy.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getLabel()));

		// Process instructions.
		for (val insn : instructions) {
			int opcode = insn.getOpcode();
			switch (opcode) {
				case -1:
					if (insn instanceof LabelNode) {
						jit.visitLabel(labels.get((LabelNode) insn));
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
					push();
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
					push();
					break;
				case BIPUSH:
				case SIPUSH:
					intOf(((IntInsnNode) insn).operand);
					push();
					break;
				case LDC:
					val cst = ((LdcInsnNode)insn).cst;
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
					ARR_LOAD_INT.emmit(jit);
					push();
					break;
				case LALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_LONG.emmit(jit);
					pushWide();
					break;
				case FALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_FLOAT.emmit(jit);
					push();
					break;
				case DALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_DOUBLE.emmit(jit);
					pushWide();
					break;
				case AALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_VALUE.emmit(jit);
					push();
					break;
				case BALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_BYTE.emmit(jit);
					push();
					break;
				case CALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_CHAR.emmit(jit);
					push();
					break;
				case SALOAD:
					pop(); // index
					pop(); // index array
					jvm_swap();
					loadCtx();
					ARR_LOAD_SHORT.emmit(jit);
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
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_INT.emmit(jit);
					break;
				case LASTORE:
					popWide(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_LONG.emmit(jit);
					break;
				case FASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_FLOAT.emmit(jit);
					break;
				case DASTORE:
					popWide(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_DOUBLE.emmit(jit);
					break;
				case AASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_VALUE.emmit(jit);
					break;
				case BASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_BYTE.emmit(jit);
					break;
				case CASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_CHAR.emmit(jit);
					break;
				case SASTORE:
					pop(); // value
					pop(); // value idx
					jvm_swap(); // idx value
					pop(); // idx value array
					jit.visitInsn(Opcodes.DUP_X2); // array idx value array
					jit.visitInsn(Opcodes.POP); // array idx value
					loadCtx();
					ARR_STORE_SHORT.emmit(jit);
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
					ADD_INT.emmit(jit);
					push();
					break;
				case LADD:
					popWide();
					popWide();
					jvm_swap();
					ADD_LONG.emmit(jit);
					pushWide();
					break;
				case FADD:
					pop();
					pop();
					jvm_swap();
					ADD_FLOAT.emmit(jit);
					push();
					break;
				case DADD:
					popWide();
					popWide();
					jvm_swap();
					ADD_DOUBLE.emmit(jit);
					pushWide();
					break;
				case ISUB:
					pop();
					pop();
					jvm_swap();
					SUB_INT.emmit(jit);
					push();
					break;
				case LSUB:
					popWide();
					popWide();
					jvm_swap();
					SUB_LONG.emmit(jit);
					pushWide();
					break;
				case FSUB:
					pop();
					pop();
					jvm_swap();
					SUB_FLOAT.emmit(jit);
					push();
					break;
				case DSUB:
					popWide();
					popWide();
					jvm_swap();
					SUB_DOUBLE.emmit(jit);
					pushWide();
					break;
				case IMUL:
					pop();
					pop();
					jvm_swap();
					MUL_INT.emmit(jit);
					push();
					break;
				case LMUL:
					popWide();
					popWide();
					jvm_swap();
					MUL_LONG.emmit(jit);
					pushWide();
					break;
				case FMUL:
					pop();
					pop();
					jvm_swap();
					MUL_FLOAT.emmit(jit);
					push();
					break;
				case DMUL:
					popWide();
					popWide();
					jvm_swap();
					MUL_DOUBLE.emmit(jit);
					pushWide();
					break;
				case IDIV:
					pop();
					pop();
					jvm_swap();
					DIV_INT.emmit(jit);
					push();
					break;
				case LDIV:
					popWide();
					popWide();
					jvm_swap();
					DIV_LONG.emmit(jit);
					pushWide();
					break;
				case FDIV:
					pop();
					pop();
					jvm_swap();
					DIV_FLOAT.emmit(jit);
					push();
					break;
				case DDIV:
					popWide();
					popWide();
					jvm_swap();
					DIV_DOUBLE.emmit(jit);
					pushWide();
					break;
				case IREM:
					pop();
					pop();
					jvm_swap();
					REM_INT.emmit(jit);
					push();
					break;
				case LREM:
					popWide();
					popWide();
					jvm_swap();
					REM_LONG.emmit(jit);
					pushWide();
					break;
				case FREM:
					pop();
					pop();
					jvm_swap();
					REM_FLOAT.emmit(jit);
					push();
					break;
				case DREM:
					popWide();
					popWide();
					jvm_swap();
					REM_DOUBLE.emmit(jit);
					pushWide();
					break;
				case INEG:
					pop();
					AS_INT.emmit(jit);
					jit.visitInsn(Opcodes.INEG);
					intOf();
					push();
					break;
				case LNEG:
					pop();
					AS_LONG.emmit(jit);
					jit.visitInsn(Opcodes.LNEG);
					longOf();
					pushWide();
					break;
				case FNEG:
					pop();
					AS_FLOAT.emmit(jit);
					jit.visitInsn(Opcodes.FNEG);
					floatOf();
					push();
					break;
				case DNEG:
					popWide();
					AS_DOUBLE.emmit(jit);
					jit.visitInsn(Opcodes.DNEG);
					doubleOf();
					pushWide();
					break;
				case ISHL:
					pop();
					pop();
					jvm_swap();
					SHL_INT.emmit(jit);
					push();
					break;
				case LSHL:
					pop();
					popWide();
					jvm_swap();
					SHL_LONG.emmit(jit);
					pushWide();
					break;
				case ISHR:
					pop();
					pop();
					jvm_swap();
					SHR_INT.emmit(jit);
					push();
					break;
				case LSHR:
					pop();
					popWide();
					jvm_swap();
					SHR_LONG.emmit(jit);
					pushWide();
					break;
				case IUSHR:
					pop();
					pop();
					jvm_swap();
					USHR_INT.emmit(jit);
					push();
					break;
				case LUSHR:
					pop();
					popWide();
					jvm_swap();
					USHR_LONG.emmit(jit);
					pushWide();
					break;
				case IAND:
					pop();
					pop();
					jvm_swap();
					AND_INT.emmit(jit);
					push();
					break;
				case LAND:
					popWide();
					popWide();
					jvm_swap();
					AND_LONG.emmit(jit);
					pushWide();
					break;
				case IOR:
					pop();
					pop();
					jvm_swap();
					OR_INT.emmit(jit);
					push();
					break;
				case LOR:
					popWide();
					popWide();
					jvm_swap();
					OR_LONG.emmit(jit);
					pushWide();
					break;
				case IXOR:
					pop();
					pop();
					jvm_swap();
					XOR_INT.emmit(jit);
					push();
					break;
				case LXOR:
					popWide();
					popWide();
					jvm_swap();
					XOR_LONG.emmit(jit);
					pushWide();
					break;
				case IINC:
					loadLocals();
					jit.visitLdcInsn(((IincInsnNode) insn).var);
					jit.visitLdcInsn(((IincInsnNode) insn).incr);
					LOCAL_INCREMENT.emmit(jit);
					break;
				case I2L:
				case F2L:
					pop();
					AS_LONG.emmit(jit);
					longOf();
					pushWide();
					break;
				case I2F:
					pop();
					AS_FLOAT.emmit(jit);
					floatOf();
					push();
					break;
				case I2D:
				case F2D:
					pop();
					AS_DOUBLE.emmit(jit);
					doubleOf();
					pushWide();
					break;
				case L2I:
				case D2I:
					popWide();
					AS_INT.emmit(jit);
					intOf();
					push();
					break;
				case L2F:
				case D2F:
					popWide();
					AS_FLOAT.emmit(jit);
					floatOf();
					push();
					break;
				case L2D:
					popWide();
					AS_DOUBLE.emmit(jit);
					doubleOf();
					pushWide();
					break;
				case F2I:
					pop();
					AS_INT.emmit(jit);
					intOf();
					push();
					break;
				case D2L:
					popWide();
					AS_LONG.emmit(jit);
					longOf();
					pushWide();
					break;
				case I2B:
					pop();
					AS_BYTE.emmit(jit);
					intOf();
					push();
					break;
				case I2C:
					pop();
					AS_CHAR.emmit(jit);
					intOf();
					push();
					break;
				case I2S:
					pop();
					AS_SHORT.emmit(jit);
					intOf();
					push();
					break;
				case LCMP:
					popWide();
					popWide();
					jvm_swap();
					COMPARE_LONG.emmit(jit);
					push();
					break;
				case FCMPL:
				case FCMPG:
					pop();
					pop();
					jvm_swap();
					jit.visitLdcInsn(opcode == FCMPL ? -1 : 1);
					COMPARE_FLOAT.emmit(jit);
					push();
					break;
				case DCMPL:
				case DCMPG:
					popWide();
					popWide();
					jvm_swap();
					jit.visitLdcInsn(opcode == DCMPL ? -1 : 1);
					COMPARE_DOUBLE.emmit(jit);
					push();
					break;
				case IFEQ:
				case IFNE:
				case IFLT:
				case IFGE:
				case IFGT:
				case IFLE:
					pop();
					AS_INT.emmit(jit);
					jit.visitJumpInsn(opcode, labels.get(((JumpInsnNode) insn).label));
					break;
				case IF_ICMPEQ:
				case IF_ICMPNE:
				case IF_ICMPLT:
				case IF_ICMPGE:
				case IF_ICMPGT:
				case IF_ICMPLE:
					pop();
					AS_INT.emmit(jit);
					pop();
					AS_INT.emmit(jit);
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
					AS_INT.emmit(jit);
					insn.clone(copy).accept(jit);
					break;
				case IRETURN:
				case FRETURN:
				case ARETURN:
					loadCtx();
					pop();
					SET_RESULT.emmit(jit);
					jit.visitInsn(RETURN);
					break;
				case LRETURN:
				case DRETURN:
					loadCtx();
					popWide();
					SET_RESULT.emmit(jit);
					jit.visitInsn(RETURN);
					break;
				case RETURN:
					jit.visitInsn(RETURN);
					break;
				case GETSTATIC:
					pushField((FieldInsnNode) insn);
					loadCtx();
					GET_STATIC.emmit(jit);
					break;
				case PUTSTATIC:
					pushField((FieldInsnNode) insn);
					loadCtx();
					PUT_STATIC.emmit(jit);
					break;
				case GETFIELD:
					pushField((FieldInsnNode) insn);
					loadCtx();
					GET_FIELD.emmit(jit);
					break;
				case PUTFIELD:
					pushField((FieldInsnNode) insn);
					loadCtx();
					PUT_FIELD.emmit(jit);
					break;
				case INVOKEVIRTUAL:
					pushMethod((MethodInsnNode) insn);
					loadCtx();
					INVOKE_VIRTUAL.emmit(jit);
					break;
				case INVOKESPECIAL:
					pushMethod((MethodInsnNode) insn);
					loadCtx();
					INVOKE_SPECIAL.emmit(jit);
					break;
				case INVOKESTATIC:
					pushMethod((MethodInsnNode) insn);
					loadCtx();
					INVOKE_STATIC.emmit(jit);
					break;
				case INVOKEINTERFACE:
					pushMethod((MethodInsnNode) insn);
					loadCtx();
					INVOKE_INTERFACE.emmit(jit);
					break;
				case NEW:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					NEW_INSTANCE.emmit(jit);
					break;
				case NEWARRAY:
					jit.visitLdcInsn(((IntInsnNode) insn).operand);
					loadCtx();
					NEW_PRIMITIVE_ARRAY.emmit(jit);
					break;
				case ANEWARRAY:
					jit.visitLdcInsn(((TypeInsnNode) insn).desc);
					loadCtx();
					NEW_INSTANCE_ARRAY.emmit(jit);
					break;
				case ARRAYLENGTH:
					loadCtx();
					GET_LENGTH.emmit(jit);
					break;
				case ATHROW:
					loadCtx();
					THROW_EXCEPTION.emmit(jit);
					break;
				case CHECKCAST:
					loadCtx();
					CHECK_CAST.emmit(jit);
					break;
				case INSTANCEOF:
					loadCtx();
					INSTANCEOF_RES.emmit(jit);
					break;
				case MONITORENTER:
					loadCtx();
					MONITOR_LOCK.emmit(jit);
					break;
				case MONITOREXIT:
					loadCtx();
					MONITOR_UNLOCK.emmit(jit);
					break;
				case MULTIANEWARRAY:
					val array = (MultiANewArrayInsnNode) insn;
					jit.visitLdcInsn(array.desc);
					jit.visitLdcInsn(array.dims);
					loadCtx();
					NEW_MULTI_ARRAY.emmit(jit);
					break;
				case IFNULL:
					pop();
					IS_NULL.emmit(jit);
					jit.visitJumpInsn(IFNE, labels.get(((JumpInsnNode) insn).label));
					break;
				case IFNONNULL:
					pop();
					IS_NULL.emmit(jit);
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

	private void cast(Type type) {
		jit.visitTypeInsn(CHECKCAST, type.internalName);
	}

	private void newObj(Type type) {
		jit.visitTypeInsn(NEW, type.internalName);
	}

	private void pop(boolean discard) {
		loadStack();
		POP.emmit(jit);
		if (discard) {
			jit.visitInsn(Opcodes.POP);
		}
	}

	private void pop() {
		pop(false);
	}

	private void popWide(boolean discard) {
		loadStack();
		POP_WIDE.emmit(jit);
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
		PUSH.emmit(jit);
	}

	private void pushWide() {
		// value
		loadStack(); // value stack
		jit.visitInsn(Opcodes.SWAP);
		PUSH_WIDE.emmit(jit);
	}

	private void pushGeneric() {
		// value
		loadStack(); // value stack
		jit.visitInsn(Opcodes.SWAP);
		PUSH_GENERIC.emmit(jit);
	}

	private void dup() {
		loadStack();
		DUP.emmit(jit);
	}

	private void dupx1() {
		loadStack();
		DUP_X1.emmit(jit);
	}

	private void dupx2() {
		loadStack();
		DUP_X2.emmit(jit);
	}

	private void dup2() {
		loadStack();
		DUP2.emmit(jit);
	}

	private void dup2x1() {
		loadStack();
		DUP2_X1.emmit(jit);
	}

	private void dup2x2() {
		loadStack();
		DUP2_X2.emmit(jit);
	}

	private void swap() {
		loadStack();
		SWAP.emmit(jit);
	}

	private void loadLocal(int idx) {
		val jit = this.jit;
		loadLocals();
		jit.visitLdcInsn(idx);
		LOAD.emmit(jit);
	}

	private void setLocal(int idx) {
		val jit = this.jit;
		// value
		loadLocals(); // value locals
		jit.visitInsn(Opcodes.SWAP); // locals value
		jit.visitLdcInsn(idx);
		jit.visitInsn(Opcodes.SWAP); // locals idx value
		STORE.emmit(jit);
	}

	private void loadNull() {
		GET_NULL.emmit(jit);
	}

	private void intOf() {
		INT_OF.emmit(jit);
	}

	private void intOf(int v) {
		jit.visitLdcInsn(v);
		intOf();
	}

	private void longOf() {
		LONG_OF.emmit(jit);
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
		FLOAT_OF.emmit(jit);
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
		DOUBLE_OF.emmit(jit);
	}

	private void doubleOf(double v) {
		jit.visitLdcInsn(v);
		doubleOf();
	}

	private void ldcOf(Object value) {
		if (value instanceof Long) {
			longOf((Long) value);
		} else if (value instanceof Double) {
			doubleOf((Double) value);
		} else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
			intOf(((Number) value).intValue());
		} else if (value instanceof Character) {
			intOf((Character) value);
			jit.visitInsn(I2C);
		} else if (value instanceof Float) {
			floatOf((Float) value);
		} else {
			val jit = this.jit;
			loadHelper();
			jit.visitLdcInsn(value);
			VALUE_FROM_LDC.emmit(jit);
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

	private static boolean isWide(Object cst) {
		return cst instanceof Long || cst instanceof Double;
	}

	private static final class Type {

		final String internalName;
		final String desc;

		private Type(Class<?> klass) {
			internalName = org.objectweb.asm.Type.getInternalName(klass);
			desc = org.objectweb.asm.Type.getDescriptor(klass);
		}

		static Type of(Class<?> klass) {
			return new Type(klass);
		}
	}

	@RequiredArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	private static final class Access {

		int opcode;
		String owner;
		String name;
		String desc;

		Access(int opcode, JitCompiler.Type owner, String name, Type rt, Type... args) {
			this(opcode, owner.internalName, name, toDescriptor(opcode >= GETSTATIC && opcode <= PUTFIELD, rt, args));
		}

		void emmit(MethodVisitor visitor) {
			val opcode = this.opcode;
			if (opcode >= INVOKEVIRTUAL && opcode <= INVOKEINTERFACE) {
				visitor.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE);
			} else {
				visitor.visitFieldInsn(opcode, owner, name, desc);
			}
		}

		static Access staticCall(Type owner, String name, Type rt, Type... args) {
			return new Access(INVOKESTATIC, owner, name, rt, args);
		}

		static Access specialCall(Type owner, String name, Type rt, Type... args) {
			return new Access(INVOKESPECIAL, owner, name, rt, args);
		}

		static Access virtualCall(Type owner, String name, Type rt, Type... args) {
			return new Access(INVOKEVIRTUAL, owner, name, rt, args);
		}

		static Access interfaceCall(Type owner, String name, Type rt, Type... args) {
			return new Access(INVOKEINTERFACE, owner, name, rt, args);
		}

		static Access getStatic(Type owner, String name, Type rt) {
			return new Access(GETSTATIC, owner, name, rt);
		}

		private static String toDescriptor(boolean field, Type rt, Type[] args) {
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
