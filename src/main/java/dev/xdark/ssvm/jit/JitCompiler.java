package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.DelegatingInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

	static final boolean CAN_USE_STATIC_FINAL;
	private static final AtomicInteger CLASS_ID = new AtomicInteger();
	private static final ClassType JIT_INVOKER = ClassType.of(JitInvoker.class);
	private static final ClassType CTX = ClassType.of(ExecutionContext.class);
	private static final String INVOKE_SIGNATURE = "(" + CTX.desc + ")V";

	private static final ClassType J_VOID = ClassType.of(void.class);
	private static final ClassType J_LONG = ClassType.of(long.class);
	private static final ClassType J_INT = ClassType.of(int.class);
	private static final ClassType J_DOUBLE = ClassType.of(double.class);
	private static final ClassType J_FLOAT = ClassType.of(float.class);
	private static final ClassType J_CHAR = ClassType.of(char.class);
	private static final ClassType J_SHORT = ClassType.of(short.class);
	private static final ClassType J_BYTE = ClassType.of(byte.class);
	private static final ClassType J_BOOLEAN = ClassType.of(boolean.class);
	private static final ClassType J_OBJECT = ClassType.of(Object.class);
	private static final ClassType J_STRING = ClassType.of(String.class);
	private static final ClassType J_INT_ARRAY = ClassType.of(int[].class);

	private static final ClassType LOCALS = ClassType.of(Locals.class);
	private static final ClassType VALUE = ClassType.of(Value.class);
	private static final ClassType NULL = ClassType.of(NullValue.class);
	private static final ClassType INT = ClassType.of(IntValue.class);
	private static final ClassType LONG = ClassType.of(LongValue.class);
	private static final ClassType FLOAT = ClassType.of(FloatValue.class);
	private static final ClassType DOUBLE = ClassType.of(DoubleValue.class);
	private static final ClassType VM_HELPER = ClassType.of(VMHelper.class);
	private static final ClassType JIT_HELPER = ClassType.of(JitHelper.class);
	private static final ClassType VALUES = ClassType.of(Value[].class);
	private static final ClassType VM_EXCEPTION = ClassType.of(VMException.class);
	private static final ClassType INSTANCE = ClassType.of(InstanceValue.class);
	private static final ClassType TOP = ClassType.of(TopValue.class);

	// ctx methods
	private static final Access GET_LOCALS = interfaceCall(CTX, "getLocals", LOCALS);
	private static final Access GET_HELPER = interfaceCall(CTX, "getHelper", VM_HELPER);
	private static final Access SET_RESULT = interfaceCall(CTX, "setResult", J_VOID, VALUE);
	private static final Access SET_LINE = interfaceCall(CTX, "setLineNumber", J_VOID, J_INT);

	// locals methods
	private static final Access LOAD = interfaceCall(LOCALS, "load", VALUE, J_INT);

	// value static methods
	private static final Access GET_NULL = staticCall(JIT_HELPER, "loadNull", VALUE, CTX);
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
	private static final Access ARR_LOAD_LONG = staticCall(JIT_HELPER, "arrayLoadLong", J_LONG, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_DOUBLE = staticCall(JIT_HELPER, "arrayLoadDouble", J_DOUBLE, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_INT = staticCall(JIT_HELPER, "arrayLoadInt", J_INT, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_FLOAT = staticCall(JIT_HELPER, "arrayLoadFloat", J_FLOAT, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_CHAR = staticCall(JIT_HELPER, "arrayLoadChar", J_CHAR, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_SHORT = staticCall(JIT_HELPER, "arrayLoadShort", J_SHORT, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_BYTE = staticCall(JIT_HELPER, "arrayLoadByte", J_BYTE, VALUE, J_INT, CTX);
	private static final Access ARR_LOAD_VALUE = staticCall(JIT_HELPER, "arrayLoadValue", VALUE, VALUE, J_INT, CTX);

	private static final Access ARR_STORE_LONG = staticCall(JIT_HELPER, "arrayStoreLong", J_VOID, VALUE, J_INT, J_LONG, CTX);
	private static final Access ARR_STORE_DOUBLE = staticCall(JIT_HELPER, "arrayStoreDouble", J_VOID, VALUE, J_INT, J_DOUBLE, CTX);
	private static final Access ARR_STORE_INT = staticCall(JIT_HELPER, "arrayStoreInt", J_VOID, VALUE, J_INT, J_INT, CTX);
	private static final Access ARR_STORE_FLOAT = staticCall(JIT_HELPER, "arrayStoreFloat", J_VOID, VALUE, J_INT, J_FLOAT, CTX);
	private static final Access ARR_STORE_CHAR = staticCall(JIT_HELPER, "arrayStoreChar", J_VOID, VALUE, J_INT, J_CHAR, CTX);
	private static final Access ARR_STORE_SHORT = staticCall(JIT_HELPER, "arrayStoreShort", J_VOID, VALUE, J_INT, J_SHORT, CTX);
	private static final Access ARR_STORE_BYTE = staticCall(JIT_HELPER, "arrayStoreByte", J_VOID, VALUE, J_INT, J_BYTE, CTX);
	private static final Access ARR_STORE_VALUE = staticCall(JIT_HELPER, "arrayStoreValue", J_VOID, VALUE, J_INT, VALUE, CTX);

	private static final Access INVOKE_INTERFACE = staticCall(JIT_HELPER, "invokeInterface", VALUE, VALUES, J_STRING, J_STRING, J_STRING, CTX);

	private static final Access NEW_INSTANCE = staticCall(JIT_HELPER, "allocateInstance", VALUE, J_OBJECT, CTX);
	private static final Access NEW_INSTANCE_SLOW = staticCall(JIT_HELPER, "allocateInstance", VALUE, J_STRING, CTX);

	private static final Access NEW_PRIMITIVE_ARRAY_LONG = staticCall(JIT_HELPER, "allocateLongArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_DOUBLE = staticCall(JIT_HELPER, "allocateDoubleArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_INT = staticCall(JIT_HELPER, "allocateIntArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_FLOAT = staticCall(JIT_HELPER, "allocateFloatArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_CHAR = staticCall(JIT_HELPER, "allocateCharArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_SHORT = staticCall(JIT_HELPER, "allocateShortArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_BYTE = staticCall(JIT_HELPER, "allocateByteArray", VALUE, J_INT, CTX);
	private static final Access NEW_PRIMITIVE_ARRAY_BOOLEAN = staticCall(JIT_HELPER, "allocateBooleanArray", VALUE, J_INT, CTX);

	private static final Access NEW_INSTANCE_ARRAY = staticCall(JIT_HELPER, "allocateValueArray", VALUE, J_INT, J_OBJECT, CTX);
	private static final Access NEW_INSTANCE_ARRAY_SLOW = staticCall(JIT_HELPER, "allocateValueArray", VALUE, J_INT, J_STRING, CTX);

	private static final Access GET_LENGTH = staticCall(JIT_HELPER, "getArrayLength", J_INT, VALUE, CTX);
	private static final Access THROW_EXCEPTION = staticCall(JIT_HELPER, "throwException", J_VOID, VALUE, CTX);
	private static final Access CHECK_CAST = staticCall(JIT_HELPER, "checkCast", VALUE, VALUE, J_OBJECT, CTX);
	private static final Access CHECK_CAST_SLOW = staticCall(JIT_HELPER, "checkCast", VALUE, VALUE, J_STRING, CTX);

	private static final Access INSTANCEOF = staticCall(JIT_HELPER, "instanceofResult", J_BOOLEAN, VALUE, J_OBJECT, CTX);
	private static final Access INSTANCEOF_SLOW = staticCall(JIT_HELPER, "instanceofResult", J_BOOLEAN, VALUE, J_STRING, CTX);

	private static final Access CLASS_LDC = staticCall(JIT_HELPER, "classLdc", VALUE, J_STRING, CTX);
	private static final Access METHOD_LDC = staticCall(JIT_HELPER, "methodLdc", VALUE, J_STRING, CTX);

	private static final Access PUT_STATIC_LONG = staticCall(JIT_HELPER, "putStaticJ", J_VOID, J_LONG, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_DOUBLE = staticCall(JIT_HELPER, "putStaticD", J_VOID, J_DOUBLE, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_INT = staticCall(JIT_HELPER, "putStaticI", J_VOID, J_INT, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_FLOAT = staticCall(JIT_HELPER, "putStaticF", J_VOID, J_FLOAT, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_CHAR = staticCall(JIT_HELPER, "putStaticC", J_VOID, J_CHAR, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_SHORT = staticCall(JIT_HELPER, "putStaticS", J_VOID, J_SHORT, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_BYTE = staticCall(JIT_HELPER, "putStaticB", J_VOID, J_BYTE, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_VALUE = staticCall(JIT_HELPER, "putStaticA", J_VOID, VALUE, J_OBJECT, J_LONG, CTX);
	private static final Access PUT_STATIC_FAIL = staticCall(JIT_HELPER, "putStaticFail", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access PUT_STATIC_SLOW = staticCall(JIT_HELPER, "putStaticA", J_VOID, VALUE, J_STRING, J_STRING, J_STRING, CTX);

	private static final Access GET_STATIC_LONG = staticCall(JIT_HELPER, "getStaticJ", J_LONG, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_DOUBLE = staticCall(JIT_HELPER, "getStaticD", J_DOUBLE, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_INT = staticCall(JIT_HELPER, "getStaticI", J_INT, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_FLOAT = staticCall(JIT_HELPER, "getStaticF", J_FLOAT, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_CHAR = staticCall(JIT_HELPER, "getStaticC", J_CHAR, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_SHORT = staticCall(JIT_HELPER, "getStaticS", J_SHORT, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_BYTE = staticCall(JIT_HELPER, "getStaticB", J_BYTE, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_VALUE = staticCall(JIT_HELPER, "getStaticA", VALUE, J_OBJECT, J_LONG, CTX);
	private static final Access GET_STATIC_FAIL = staticCall(JIT_HELPER, "getStaticFail", J_VOID, J_OBJECT, J_OBJECT, CTX);
	private static final Access GET_STATIC_SLOW = staticCall(JIT_HELPER, "getStaticA", VALUE, J_STRING, J_STRING, J_STRING, CTX);

	private static final Access GET_FIELD_LONG = staticCall(JIT_HELPER, "getFieldJ", J_LONG, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_DOUBLE = staticCall(JIT_HELPER, "getFieldD", J_DOUBLE, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_INT = staticCall(JIT_HELPER, "getFieldI", J_INT, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_FLOAT = staticCall(JIT_HELPER, "getFieldF", J_FLOAT, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_CHAR = staticCall(JIT_HELPER, "getFieldC", J_CHAR, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_SHORT = staticCall(JIT_HELPER, "getFieldS", J_SHORT, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_BYTE = staticCall(JIT_HELPER, "getFieldB", J_BYTE, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_BOOLEAN = staticCall(JIT_HELPER, "getFieldZ", J_BOOLEAN, VALUE, J_OBJECT, J_STRING, CTX);
	private static final Access GET_FIELD_VALUE = staticCall(JIT_HELPER, "getFieldA", VALUE, VALUE, J_OBJECT, J_STRING, J_STRING, CTX);

	private static final Access GET_FIELD_LONG_DIRECT = staticCall(JIT_HELPER, "getFieldJ", J_LONG, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_DOUBLE_DIRECT = staticCall(JIT_HELPER, "getFieldD", J_DOUBLE, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_INT_DIRECT = staticCall(JIT_HELPER, "getFieldI", J_INT, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_FLOAT_DIRECT = staticCall(JIT_HELPER, "getFieldF", J_FLOAT, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_CHAR_DIRECT = staticCall(JIT_HELPER, "getFieldC", J_CHAR, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_SHORT_DIRECT = staticCall(JIT_HELPER, "getFieldS", J_SHORT, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_BYTE_DIRECT = staticCall(JIT_HELPER, "getFieldB", J_BYTE, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_BOOLEAN_DIRECT = staticCall(JIT_HELPER, "getFieldZ", J_BOOLEAN, VALUE, J_LONG, CTX);
	private static final Access GET_FIELD_VALUE_DIRECT = staticCall(JIT_HELPER, "getFieldA", VALUE, VALUE, J_LONG, CTX);

	private static final Access PUT_FIELD_LONG = staticCall(JIT_HELPER, "putFieldJ", J_VOID, VALUE, J_LONG, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_DOUBLE = staticCall(JIT_HELPER, "putFieldD", J_VOID, VALUE, J_DOUBLE, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_INT = staticCall(JIT_HELPER, "putFieldI", J_VOID, VALUE, J_INT, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_FLOAT = staticCall(JIT_HELPER, "putFieldF", J_VOID, VALUE, J_FLOAT, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_CHAR = staticCall(JIT_HELPER, "putFieldC", J_VOID, VALUE, J_CHAR, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_SHORT = staticCall(JIT_HELPER, "putFieldS", J_VOID, VALUE, J_SHORT, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_BYTE = staticCall(JIT_HELPER, "putFieldB", J_VOID, VALUE, J_BYTE, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_BOOLEAN = staticCall(JIT_HELPER, "putFieldZ", J_VOID, VALUE, J_BOOLEAN, J_OBJECT, J_STRING, CTX);
	private static final Access PUT_FIELD_VALUE = staticCall(JIT_HELPER, "putFieldA", J_VOID, VALUE, VALUE, J_OBJECT, J_STRING, J_STRING, CTX);


	private static final Access PUT_FIELD_LONG_DIRECT = staticCall(JIT_HELPER, "putFieldJ", J_VOID, VALUE, J_LONG, J_LONG, CTX);
	private static final Access PUT_FIELD_DOUBLE_DIRECT = staticCall(JIT_HELPER, "putFieldD", J_VOID, VALUE, J_DOUBLE, J_LONG, CTX);
	private static final Access PUT_FIELD_INT_DIRECT = staticCall(JIT_HELPER, "putFieldI", J_VOID, VALUE, J_INT, J_LONG, CTX);
	private static final Access PUT_FIELD_FLOAT_DIRECT = staticCall(JIT_HELPER, "putFieldF", J_VOID, VALUE, J_FLOAT, J_LONG, CTX);
	private static final Access PUT_FIELD_CHAR_DIRECT = staticCall(JIT_HELPER, "putFieldC", J_VOID, VALUE, J_CHAR, J_LONG, CTX);
	private static final Access PUT_FIELD_SHORT_DIRECT = staticCall(JIT_HELPER, "putFieldS", J_VOID, VALUE, J_SHORT, J_LONG, CTX);
	private static final Access PUT_FIELD_BYTE_DIRECT = staticCall(JIT_HELPER, "putFieldB", J_VOID, VALUE, J_BYTE, J_LONG, CTX);
	private static final Access PUT_FIELD_BOOLEAN_DIRECT = staticCall(JIT_HELPER, "putFieldZ", J_VOID, VALUE, J_BOOLEAN, J_LONG, CTX);
	private static final Access PUT_FIELD_VALUE_DIRECT = staticCall(JIT_HELPER, "putFieldA", J_VOID, VALUE, VALUE, J_LONG, CTX);

	private static final Access INVOKE_STATIC_INTRINSIC = staticCall(JIT_HELPER, "invokeStatic", VALUE, VALUES, J_OBJECT, CTX);
	private static final Access INVOKE_SPECIAL_INTRINSIC = staticCall(JIT_HELPER, "invokeSpecial", VALUE, VALUES, J_OBJECT, CTX);
	private static final Access INVOKE_VIRTUAL_INTRINSIC = staticCall(JIT_HELPER, "invokeVirtual", VALUE, VALUES, J_OBJECT, J_OBJECT, CTX);
	private static final Access INVOKE_STATIC_SLOW = staticCall(JIT_HELPER, "invokeStatic", VALUE, VALUES, J_STRING, J_STRING, J_STRING, CTX);
	private static final Access INVOKE_SPECIAL_SLOW = staticCall(JIT_HELPER, "invokeSpecial", VALUE, VALUES, J_STRING, J_STRING, J_STRING, CTX);

	private static final Access EXCEPTION_CAUGHT = staticCall(JIT_HELPER, "exceptionCaught", VM_EXCEPTION, VM_EXCEPTION, J_OBJECT, CTX);
	private static final Access GET_EXCEPTION_OOP = virtualCall(VM_EXCEPTION, "getOop", INSTANCE);

	private static final Access DYNAMIC_CALL = staticCall(JIT_HELPER, "invokeDynamic", VALUE, VALUES, J_OBJECT, J_INT, CTX);

	private static final Access MONITOR_ENTER = staticCall(JIT_HELPER, "monitorEnter", J_VOID, VALUE, CTX);
	private static final Access MONITOR_EXIT = staticCall(JIT_HELPER, "monitorExit", J_VOID, VALUE, CTX);

	private static final Access NEW_MULTI_ARRAY_INTRINSIC = staticCall(JIT_HELPER, "multiNewArray", VALUE, J_OBJECT, J_INT_ARRAY, CTX);
	private static final Access NEW_MULTI_ARRAY = staticCall(JIT_HELPER, "multiNewArray", VALUE, J_STRING, J_INT_ARRAY, CTX);

	private static final Access GET_TOP = getStatic(TOP, "INSTANCE", TOP);

	private static final int CTX_SLOT = 1;
	private static final int LOCALS_SLOT = 2;
	private static final int HELPER_SLOT = 3;
	private static final int SLOT_OFFSET = 4;

	String className;
	JavaMethod target;
	ClassWriter writer;
	MethodVisitor jit;
	Map<Object, Integer> constants;
	int ctxIndex;
	Map<DynamicCallInfo, MethodInsnNode> invokeDynamicCalls = new HashMap<>();
	Map<MethodCallInfo, MethodInsnNode> methodCalls = new HashMap<>();
	Map<NewMultiArrayInfo, MethodInsnNode> multiArrays = new HashMap<>();

	/**
	 * @param jm Method to check.
	 * @return {@code true} if method is compilable,
	 * {@code false} otherwise.
	 */
	public static boolean isCompilable(JavaMethod jm) {
		return jm.getNode().instructions.size() != 0;
	}

	/**
	 * Compiles method.
	 *
	 * @param jm    Method for compilation.
	 * @param flags {@link ClassWriter} flags.
	 * @return jit class info.
	 */
	public static JitClass compile(JavaMethod jm, int flags) {
		ClassWriter writer = new ClassWriter(flags);
		String className = "dev/xdark/ssvm/jit/JitCode" + CLASS_ID.getAndIncrement();
		writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, className, null, JIT_INVOKER.internalName, null);
		MethodVisitor init = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitVarInsn(ALOAD, 0);
		init.visitMethodInsn(INVOKESPECIAL, JIT_INVOKER.internalName, "<init>", "()V", false);
		init.visitInsn(RETURN);
		init.visitEnd();
		init.visitMaxs(1, 1);
		MethodVisitor jit = writer.visitMethod(ACC_PUBLIC, "invoke", INVOKE_SIGNATURE, null, null);
		jit.visitCode();
		JitCompiler compiler = new JitCompiler(className, jm, writer, jit, new LinkedHashMap<>(), CTX_SLOT);
		compiler.compileInner();
		jit.visitEnd();
		jit.visitMaxs(-1, -1);
		// Leave some debug info.
		InstanceJavaClass owner = jm.getOwner();
		int infoAcc = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
		writer.visitField(infoAcc, "CLASS", "Ljava/lang/String;", null, owner.getInternalName());
		writer.visitField(infoAcc, "METHOD_NAME", "Ljava/lang/String;", null, jm.getName());
		writer.visitField(infoAcc, "METHOD_DESC", "Ljava/lang/String;", null, jm.getDesc());
		writer.visitSource(jm.toString(), null);
		Set<Object> constants = compiler.constants.keySet();
		if (!constants.isEmpty()) {
			int constantsAcc = ACC_PRIVATE | ACC_STATIC;
			if (CAN_USE_STATIC_FINAL) {
				constantsAcc |= ACC_FINAL;
			}
			writer.visitField(constantsAcc, "constants", "[Ljava/lang/Object;", null, null);
		}

		byte[] bc = writer.toByteArray();
		return new JitClass(className, bc, constants.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(constants)));
	}

	private void compileInner() {
		MethodVisitor jit = this.jit;
		// Load locals.
		loadCtx();
		GET_LOCALS.emit(jit);
		jit.visitVarInsn(ASTORE, LOCALS_SLOT);
		// Load helper.
		loadCtx();
		GET_HELPER.emit(jit);
		jit.visitVarInsn(ASTORE, HELPER_SLOT);

		JavaMethod target = this.target;
		MethodNode node = target.getNode();
		// Load method locals.
		Type[] args = target.getArgumentTypes();
		int local = 0;
		if ((target.getAccess() & ACC_STATIC) == 0) {
			// Load 'this'.
			loadLocal(0);
			jvm_var(ASTORE, 0);
			local++;
		}
		for (Type arg : args) {
			int x = local++;
			loadLocal(x);
			if (arg.getSize() == 2) {
				local++;
			}
			switch (arg.getSort()) {
				case Type.LONG:
					AS_LONG.emit(jit);
					jvm_var(LSTORE, x);
					break;
				case Type.DOUBLE:
					AS_DOUBLE.emit(jit);
					jvm_var(DSTORE, x);
					break;
				case Type.INT:
				case Type.CHAR:
				case Type.SHORT:
				case Type.BYTE:
				case Type.BOOLEAN:
					AS_INT.emit(jit);
					jvm_var(ISTORE, x);
					break;
				case Type.FLOAT:
					AS_FLOAT.emit(jit);
					jvm_var(FSTORE, x);
					break;
				default:
					jvm_var(ASTORE, x);
			}
		}

		InsnList instructions = node.instructions;
		Map<LabelNode, LabelNode> copy = StreamSupport.stream(instructions.spliterator(), false)
			.filter(x -> x instanceof LabelNode)
			.collect(Collectors.toMap(x -> (LabelNode) x, __ -> new LabelNode()));
		Map<LabelNode, Label> labels = copy.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getLabel()));
		List<TryCatchBlockNode> tryCatchBlocks = node.tryCatchBlocks;
		Map<Label, List<TryCatchBlockNode>> handlers = tryCatchBlocks.stream()
			.collect(Collectors.groupingBy(x -> labels.get(x.handler),
				Collectors.mapping(Function.identity(), Collectors.toList())));
		for (TryCatchBlockNode block : tryCatchBlocks) {
			jit.visitTryCatchBlock(
				labels.get(block.start),
				labels.get(block.end),
				labels.get(block.handler),
				VM_EXCEPTION.internalName
			);
		}
		// Process instructions.
		for (AbstractInsnNode insn : instructions) {
			insn = unmask(insn);
			int opcode = insn.getOpcode();

			switch (opcode) {
				case -1:
					if (insn instanceof LabelNode) {
						Label label = labels.get(insn);
						jit.visitLabel(label);
						List<TryCatchBlockNode> blocks = handlers.get(label);
						if (blocks != null) {
							jit.visitInsn(DUP);
							jit.visitTypeInsn(Opcodes.INSTANCEOF, VALUE.internalName);
							Label overlap = new Label();
							jit.visitJumpInsn(IFNE, overlap);
							cast(VM_EXCEPTION);
							int count = blocks.size();
							Object[] classes = new Object[count];
							for (int i = 0; i < count; i++) {
								TryCatchBlockNode block = blocks.get(i);
								String type = block.type;
								if (type == null) {
									type = "java/lang/Throwable";
								}
								classes[i] = tryLoadClass(type);
							}
							loadCompilerConstant(classes); // ex infos
							loadCtx(); // ex infos ctx
							EXCEPTION_CAUGHT.emit(jit); // ex
							GET_EXCEPTION_OOP.emit(jit);
							jit.visitLabel(overlap);
						}
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
					break;
				case ICONST_M1:
				case ICONST_0:
				case ICONST_1:
				case ICONST_2:
				case ICONST_3:
				case ICONST_4:
				case ICONST_5:
				case LCONST_0:
				case LCONST_1:
				case FCONST_0:
				case FCONST_1:
				case FCONST_2:
				case DCONST_0:
				case DCONST_1:
				case POP:
				case POP2:
				case DUP:
				case DUP_X1:
				case DUP_X2:
				case DUP2:
				case DUP2_X1:
				case DUP2_X2:
				case SWAP:
				case LXOR:
				case IADD:
				case LADD:
				case FADD:
				case DADD:
				case ISUB:
				case LSUB:
				case FSUB:
				case DSUB:
				case IMUL:
				case LMUL:
				case FMUL:
				case DMUL:
				case FDIV:
				case DDIV:
				case IREM:
				case LREM:
				case FREM:
				case DREM:
				case INEG:
				case LNEG:
				case FNEG:
				case DNEG:
				case ISHL:
				case LSHL:
				case ISHR:
				case LSHR:
				case IUSHR:
				case LUSHR:
				case IAND:
				case LAND:
				case IOR:
				case LOR:
				case IXOR:
				case I2L:
				case F2L:
				case I2F:
				case I2D:
				case F2D:
				case L2I:
				case D2I:
				case L2F:
				case D2F:
				case L2D:
				case F2I:
				case D2L:
				case I2B:
				case I2C:
				case I2S:
				case LCMP:
				case FCMPL:
				case FCMPG:
				case DCMPL:
				case DCMPG:
				case IDIV:
				case LDIV:
					jit.visitInsn(opcode);
					break;
				case BIPUSH:
				case SIPUSH:
					insn.accept(jit);
					break;
				case LDC:
					ldcOf(((LdcInsnNode) insn).cst);
					break;
				case ILOAD:
				case FLOAD:
				case ALOAD:
				case LLOAD:
				case DLOAD:
				case ISTORE:
				case FSTORE:
				case ASTORE:
				case LSTORE:
				case DSTORE:
					jvm_var(opcode, ((VarInsnNode) insn).var);
					break;
				case IALOAD:
					loadCtx();
					ARR_LOAD_INT.emit(jit);
					break;
				case LALOAD:
					loadCtx();
					ARR_LOAD_LONG.emit(jit);
					break;
				case FALOAD:
					loadCtx();
					ARR_LOAD_FLOAT.emit(jit);
					break;
				case DALOAD:
					loadCtx();
					ARR_LOAD_DOUBLE.emit(jit);
					break;
				case AALOAD:
					loadCtx();
					ARR_LOAD_VALUE.emit(jit);
					break;
				case BALOAD:
					loadCtx();
					ARR_LOAD_BYTE.emit(jit);
					break;
				case CALOAD:
					loadCtx();
					ARR_LOAD_CHAR.emit(jit);
					break;
				case SALOAD:
					loadCtx();
					ARR_LOAD_SHORT.emit(jit);
					break;
				case IASTORE:
					loadCtx();
					ARR_STORE_INT.emit(jit);
					break;
				case LASTORE:
					loadCtx();
					ARR_STORE_LONG.emit(jit);
					break;
				case FASTORE:
					loadCtx();
					ARR_STORE_FLOAT.emit(jit);
					break;
				case DASTORE:
					loadCtx();
					ARR_STORE_DOUBLE.emit(jit);
					break;
				case AASTORE:
					loadCtx();
					ARR_STORE_VALUE.emit(jit);
					break;
				case BASTORE:
					loadCtx();
					ARR_STORE_BYTE.emit(jit);
					break;
				case CASTORE:
					loadCtx();
					ARR_STORE_CHAR.emit(jit);
					break;
				case SASTORE:
					loadCtx();
					ARR_STORE_SHORT.emit(jit);
					break;
				case IINC:
					IincInsnNode iinc = (IincInsnNode) insn;
					jit.visitIincInsn(iinc.var + SLOT_OFFSET, iinc.incr);
					break;
				case IFEQ:
				case IFNE:
				case IFLT:
				case IFGE:
				case IFGT:
				case IFLE:
				case IF_ICMPEQ:
				case IF_ICMPNE:
				case IF_ICMPLT:
				case IF_ICMPGE:
				case IF_ICMPGT:
				case IF_ICMPLE:
				case IF_ACMPEQ:
				case IF_ACMPNE:
				case GOTO:
					jit.visitJumpInsn(opcode, labels.get(((JumpInsnNode) insn).label));
					break;
				case JSR:
				case RET:
				case TABLESWITCH:
				case LOOKUPSWITCH:
					insn.clone(copy).accept(jit);
					break;
				case IRETURN:
					intOf();
					loadCtx();
					jvm_swap();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case FRETURN:
					floatOf();
					loadCtx();
					jvm_swap();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case ARETURN:
					loadCtx();
					jvm_swap();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case LRETURN:
					longOf();
					loadCtx();
					jvm_swap();
					SET_RESULT.emit(jit);
					jit.visitInsn(RETURN);
					break;
				case DRETURN:
					doubleOf();
					loadCtx();
					jvm_swap();
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
					putStatic((FieldInsnNode) insn);
					break;
				case GETFIELD:
					getField((FieldInsnNode) insn);
					break;
				case PUTFIELD:
					putField((FieldInsnNode) insn);
					break;
				case INVOKEVIRTUAL:
				case INVOKESPECIAL:
				case INVOKESTATIC:
				case INVOKEINTERFACE:
					doCall((MethodInsnNode) insn);
					break;
				case NEW:
					newInstance(((TypeInsnNode) insn).desc);
					break;
				case NEWARRAY:
					loadCtx();
					newPrimitiveArray(((IntInsnNode) insn).operand);
					break;
				case ANEWARRAY:
					newArray(((TypeInsnNode) insn).desc);
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
					checkCast(((TypeInsnNode) insn).desc);
					break;
				case Opcodes.INSTANCEOF:
					instanceofCheck(((TypeInsnNode) insn).desc);
					break;
				case MONITORENTER:
					loadCtx();
					MONITOR_ENTER.emit(jit);
					break;
				case MONITOREXIT:
					loadCtx();
					MONITOR_EXIT.emit(jit);
					break;
				case MULTIANEWARRAY:
					multiNewArray((MultiANewArrayInsnNode) insn);
					break;
				case IFNULL:
					IS_NULL.emit(jit);
					jit.visitJumpInsn(IFNE, labels.get(((JumpInsnNode) insn).label));
					break;
				case IFNONNULL:
					IS_NULL.emit(jit);
					jit.visitJumpInsn(IFEQ, labels.get(((JumpInsnNode) insn).label));
					break;
				case INVOKEDYNAMIC:
					invokeDynamic((InvokeDynamicInsnNode) insn);
					break;
			}
		}
	}

	private void loadCtx() {
		loadCtx(jit);
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

	private void loadLocal(int idx) {
		MethodVisitor jit = this.jit;
		loadLocals();
		jit.visitLdcInsn(idx);
		LOAD.emit(jit);
	}

	private void loadNull() {
		loadCtx();
		GET_NULL.emit(jit);
	}

	private void intOf() {
		INT_OF.emit(jit);
	}

	private void longOf() {
		LONG_OF.emit(jit);
	}

	private void floatOf() {
		MethodVisitor jit = this.jit;
		// value
		newObj(FLOAT); // value wrapper
		jit.visitInsn(DUP_X1);
		jit.visitInsn(SWAP); // wrapper wrapper value
		FLOAT_OF.emit(jit);
	}

	private void doubleOf() {
		MethodVisitor jit = this.jit;
		// value
		newObj(DOUBLE); // value wrapper
		jit.visitInsn(DUP); // value wrwapper wrapper
		jvm_swap(2, 2); // wrwapper wrapper value
		DOUBLE_OF.emit(jit);
	}

	private void ldcOf(Object value) {
		MethodVisitor jit = this.jit;
		if (value instanceof String) {
			loadCompilerConstant(target.getOwner().getVM().getStringPool().intern((String) value));
		} else if (value instanceof Long
			|| value instanceof Double
			|| value instanceof Integer
			|| value instanceof Short
			|| value instanceof Byte
			|| value instanceof Character
			|| value instanceof Float) {
			jit.visitLdcInsn(value);
		} else if (value instanceof Type) {
			Type type = (Type) value;
			if (type.getSort() == Type.METHOD) {
				Object mt = tryMethodType(type);
				if (mt instanceof Type) {
					jit.visitLdcInsn(type.getDescriptor());
					loadCtx();
					METHOD_LDC.emit(jit);
				} else {
					loadCompilerConstant(mt);
				}
			} else {
				Object klass = tryLoadClass(type.getInternalName());
				if (klass instanceof JavaClass) {
					loadCompilerConstant(((JavaClass) klass).getOop());
				} else {
					jit.visitLdcInsn(type.getInternalName());
					loadCtx();
					CLASS_LDC.emit(jit);
				}
			}
		} else if (value instanceof Value) {
			loadCompilerConstant(value);
		} else {
			loadHelper();
			loadCompilerConstant(value);
			VALUE_FROM_LDC.emit(jit);
		}
	}

	private void pushField(FieldInsnNode field, boolean withDesc) {
		MethodVisitor jit = this.jit;
		Object owner = tryLoadClass(field.owner);
		if (owner instanceof InstanceJavaClass) {
			loadCompilerConstant(owner);
		} else {
			jit.visitLdcInsn(owner);
		}
		jit.visitLdcInsn(field.name);
		if (withDesc) {
			jit.visitLdcInsn(field.desc);
		}
	}

	private void pushField(FieldInsnNode field) {
		pushField(field, true);
	}

	private void pushMethod(MethodInsnNode method) {
		MethodVisitor jit = this.jit;
		jit.visitLdcInsn(method.owner);
		jit.visitLdcInsn(method.name);
		jit.visitLdcInsn(method.desc);
	}

	private void jvm_swap(int right, int left) {
		MethodVisitor jit = this.jit;
		if (right == 1) {
			if (left == 1) {
				jit.visitInsn(SWAP);
			} else if (left == 2) {
				jit.visitInsn(DUP_X2);
				jit.visitInsn(POP);
			} else {
				throw new IllegalStateException("Not implemented for stackTop=" + right + ", belowTop=" + left);
			}
		} else if (right == 2) {
			if (left == 1) {
				jit.visitInsn(DUP2_X1);
				jit.visitInsn(POP2);
			} else if (left == 2) {
				jit.visitInsn(DUP2_X2);
				jit.visitInsn(POP2);
			} else {
				throw new IllegalStateException("Not implemented for stackTop=" + right + ", belowTop=" + left);
			}
		} else {
			throw new IllegalStateException("Not implemented for stackTop=" + right);
		}
	}

	private void jvm_swap() {
		jit.visitInsn(SWAP);
	}

	private void jvm_var(int opcode, int index) {
		jit.visitVarInsn(opcode, index + SLOT_OFFSET);
	}

	private void loadConstants() {
		loadConstants(jit);
	}

	private Integer makeConstant(Object value) {
		Map<Object, Integer> constants = this.constants;
		Integer constant = constants.get(value);
		if (constant == null) {
			constant = constants.size();
			constants.put(value, constant);
		}
		return constant;
	}

	private void loadCompilerConstant(Object value) {
		Integer constant = makeConstant(value);
		loadConstants();
		MethodVisitor jit = this.jit;
		jit.visitLdcInsn(constant);
		jit.visitInsn(AALOAD);
	}

	private void getStatic(FieldInsnNode node) {
		MethodVisitor jit = this.jit;
		Access access;
		lookup:
		{
			try {
				JavaMethod target = this.target;
				InstanceJavaClass owner = target.getOwner();
				VirtualMachine vm = owner.getVM();
				InstanceJavaClass jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
				String name = node.name;
				String desc = node.desc;
				JavaField field = jc.getStaticFieldRecursively(name, desc);
				if (field != null) {
					// We will be reading from a field directly at this point,
					// force class initialization
					jc = field.getOwner();
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
				// Field was not found.
				jit.visitInsn(ACONST_NULL);
				jit.visitLdcInsn(node.name);
				access = GET_STATIC_FAIL;
			} catch (VMException ex) {
				// Class was probably not found.
				// We need to use fallback path
				// because the class may be defined right in the code
				// we are JITting.
				getStaticSlow(node);
				return;
			}
		}
		loadCtx();
		access.emit(jit);
		if (access == GET_STATIC_FAIL) {
			dummyValue(Type.getType(node.desc));
		}
	}

	private void getStaticSlow(FieldInsnNode node) {
		pushField(node);
		loadCtx();
		GET_STATIC_SLOW.emit(jit);
		toJava(Type.getType(node.desc));
	}

	private void putStaticSlow(FieldInsnNode node) {
		toVM(Type.getType(node.desc));
		pushField(node);
		loadCtx();
		PUT_STATIC_SLOW.emit(jit);
	}

	private void putStatic(FieldInsnNode node) {
		MethodVisitor jit = this.jit;
		Access access;
		lookup:
		{
			try {
				JavaMethod target = this.target;
				InstanceJavaClass owner = target.getOwner();
				VirtualMachine vm = owner.getVM();
				InstanceJavaClass jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
				String name = node.name;
				String desc = node.desc;
				JavaField field = jc.getStaticFieldRecursively(name, desc);
				if (field != null) {
					jc = field.getOwner();
					long offset = vm.getMemoryManager().getStaticOffset(jc) + field.getOffset();
					loadCompilerConstant(jc);
					jit.visitLdcInsn(offset);
					switch (field.getType().getSort()) {
						case Type.LONG:
							access = PUT_STATIC_LONG;
							break;
						case Type.DOUBLE:
							access = PUT_STATIC_DOUBLE;
							break;
						case Type.INT:
							access = PUT_STATIC_INT;
							break;
						case Type.FLOAT:
							access = PUT_STATIC_FLOAT;
							break;
						case Type.CHAR:
							access = PUT_STATIC_CHAR;
							break;
						case Type.SHORT:
							access = PUT_STATIC_SHORT;
							break;
						case Type.BYTE:
						case Type.BOOLEAN:
							access = PUT_STATIC_BYTE;
							break;
						default:
							access = PUT_STATIC_VALUE;
							break;
					}
					break lookup;
				}
				// Field was not found.
				jit.visitInsn(ACONST_NULL);
				jit.visitLdcInsn(node.name);
				access = PUT_STATIC_FAIL;
			} catch (VMException ex) {
				// Class was probably not found.
				// We need to use fallback path
				// because the class may be defined right in the code
				// we are JITting.
				putStaticSlow(node);
				return;
			}
		}
		loadCtx();
		access.emit(jit);
		if (access == PUT_STATIC_FAIL) {
			dummyValue(Type.getType(node.desc));
		}
	}

	private void getField(FieldInsnNode node) {
		int sort = Type.getType(node.desc).getSort();
		Object jc = tryLoadClass(node.owner);
		if (jc instanceof InstanceJavaClass) {
			InstanceJavaClass klass = (InstanceJavaClass) jc;
			JavaField field;
			if ((klass.getModifiers() & ACC_FINAL) != 0) {
				field = klass.getVirtualFieldRecursively(node.name, node.desc);
			} else {
				field = klass.getVirtualField(node.name, node.desc);
			}
			if (field != null) {
				long offset = getMemoryManager().valueBaseOffset(klass) + field.getOffset();
				MethodVisitor jit = this.jit;
				jit.visitLdcInsn(offset);
				loadCtx();
				switch (sort) {
					case Type.LONG:
						GET_FIELD_LONG_DIRECT.emit(jit);
						break;
					case Type.DOUBLE:
						GET_FIELD_DOUBLE_DIRECT.emit(jit);
						break;
					case Type.INT:
						GET_FIELD_INT_DIRECT.emit(jit);
						break;
					case Type.FLOAT:
						GET_FIELD_FLOAT_DIRECT.emit(jit);
						break;
					case Type.CHAR:
						GET_FIELD_CHAR_DIRECT.emit(jit);
						break;
					case Type.SHORT:
						GET_FIELD_SHORT_DIRECT.emit(jit);
						break;
					case Type.BYTE:
						GET_FIELD_BYTE_DIRECT.emit(jit);
						break;
					case Type.BOOLEAN:
						GET_FIELD_BOOLEAN_DIRECT.emit(jit);
						break;
					default:
						GET_FIELD_VALUE_DIRECT.emit(jit);
				}
				return;
			}
		}
		pushField(node, sort >= Type.ARRAY);
		loadCtx();
		MethodVisitor jit = this.jit;
		switch (sort) {
			case Type.LONG:
				GET_FIELD_LONG.emit(jit);
				break;
			case Type.DOUBLE:
				GET_FIELD_DOUBLE.emit(jit);
				break;
			case Type.INT:
				GET_FIELD_INT.emit(jit);
				break;
			case Type.FLOAT:
				GET_FIELD_FLOAT.emit(jit);
				break;
			case Type.CHAR:
				GET_FIELD_CHAR.emit(jit);
				break;
			case Type.SHORT:
				GET_FIELD_SHORT.emit(jit);
				break;
			case Type.BYTE:
				GET_FIELD_BYTE.emit(jit);
				break;
			case Type.BOOLEAN:
				GET_FIELD_BOOLEAN.emit(jit);
				break;
			default:
				GET_FIELD_VALUE.emit(jit);
		}
	}

	private void putField(FieldInsnNode node) {
		int sort = Type.getType(node.desc).getSort();
		Object jc = tryLoadClass(node.owner);
		if (jc instanceof InstanceJavaClass) {
			InstanceJavaClass klass = (InstanceJavaClass) jc;
			JavaField field;
			if ((klass.getModifiers() & ACC_FINAL) != 0) {
				field = klass.getVirtualFieldRecursively(node.name, node.desc);
			} else {
				field = klass.getVirtualField(node.name, node.desc);
			}
			if (field != null) {
				long offset = getMemoryManager().valueBaseOffset(klass) + field.getOffset();
				MethodVisitor jit = this.jit;
				jit.visitLdcInsn(offset);
				loadCtx();
				switch (sort) {
					case Type.LONG:
						PUT_FIELD_LONG_DIRECT.emit(jit);
						break;
					case Type.DOUBLE:
						PUT_FIELD_DOUBLE_DIRECT.emit(jit);
						break;
					case Type.INT:
						PUT_FIELD_INT_DIRECT.emit(jit);
						break;
					case Type.FLOAT:
						PUT_FIELD_FLOAT_DIRECT.emit(jit);
						break;
					case Type.CHAR:
						PUT_FIELD_CHAR_DIRECT.emit(jit);
						break;
					case Type.SHORT:
						PUT_FIELD_SHORT_DIRECT.emit(jit);
						break;
					case Type.BYTE:
						PUT_FIELD_BYTE_DIRECT.emit(jit);
						break;
					case Type.BOOLEAN:
						PUT_FIELD_BOOLEAN_DIRECT.emit(jit);
						break;
					default:
						PUT_FIELD_VALUE_DIRECT.emit(jit);
				}
				return;
			}
		}
		pushField(node, sort >= Type.ARRAY);
		loadCtx();
		MethodVisitor jit = this.jit;
		switch (sort) {
			case Type.LONG:
				PUT_FIELD_LONG.emit(jit);
				break;
			case Type.DOUBLE:
				PUT_FIELD_DOUBLE.emit(jit);
				break;
			case Type.INT:
				PUT_FIELD_INT.emit(jit);
				break;
			case Type.FLOAT:
				PUT_FIELD_FLOAT.emit(jit);
				break;
			case Type.CHAR:
				PUT_FIELD_CHAR.emit(jit);
				break;
			case Type.SHORT:
				PUT_FIELD_SHORT.emit(jit);
				break;
			case Type.BYTE:
				PUT_FIELD_BYTE.emit(jit);
				break;
			case Type.BOOLEAN:
				PUT_FIELD_BOOLEAN.emit(jit);
				break;
			default:
				PUT_FIELD_VALUE.emit(jit);
		}
	}

	private void invokeStatic(MethodInsnNode node) {
		MethodVisitor jit = this.jit;
		String desc = node.desc;
		Type rt = Type.getReturnType(desc);
		Access access;
		try {
			JavaMethod target = this.target;
			InstanceJavaClass owner = target.getOwner();
			VirtualMachine vm = owner.getVM();
			InstanceJavaClass jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
			String name = node.name;
			JavaMethod mn = vm.getLinkResolver().resolveStaticMethod(jc, name, desc);
			collectStaticCallArgs(desc);
			loadCompilerConstant(mn);
			loadCtx();
			INVOKE_STATIC_INTRINSIC.emit(jit);
		} catch (VMException ex) {
			// Class was probably not found.
			// We need to use fallback path
			// because the class may be defined right in the code
			// we are JITting.
			invokeStaticSlow(node);
		}
		toJava(rt);
	}

	private void invokeStaticSlow(MethodInsnNode node) {
		String desc = node.desc;
		collectStaticCallArgs(desc);
		pushMethod(node);
		loadCtx();
		INVOKE_STATIC_SLOW.emit(jit);
	}

	private void invokeSpecial(MethodInsnNode node) {
		MethodVisitor jit = this.jit;
		String desc = node.desc;
		Type rt = Type.getReturnType(desc);
		try {
			JavaMethod target = this.target;
			InstanceJavaClass owner = target.getOwner();
			VirtualMachine vm = owner.getVM();
			InstanceJavaClass jc = (InstanceJavaClass) vm.getHelper().findClass(owner.getClassLoader(), node.owner, false);
			String name = node.name;
			JavaMethod mn = vm.getLinkResolver().resolveSpecialMethod(jc, name, desc);
			collectVirtualCallArgs(desc);
			loadCompilerConstant(mn);
			loadCtx();
			INVOKE_SPECIAL_INTRINSIC.emit(jit);
		} catch (VMException ex) {
			// Class was probably not found.
			// We need to use fallback path
			// because the class may be defined right in the code
			// we are JITting.
			invokeSpecialSlow(node);
		}
		toJava(rt);
	}

	private void invokeSpecialSlow(MethodInsnNode node) {
		String desc = node.desc;
		collectVirtualCallArgs(desc);
		pushMethod(node);
		loadCtx();
		INVOKE_SPECIAL_SLOW.emit(jit);
	}

	private void invokeVirtual(MethodInsnNode node) {
		MethodVisitor jit = this.jit;
		String desc = node.desc;
		collectVirtualCallArgs(desc);
		jit.visitLdcInsn(node.name);
		jit.visitLdcInsn(desc);
		loadCtx();
		INVOKE_VIRTUAL_INTRINSIC.emit(jit);
		toJava(Type.getReturnType(desc));
	}

	private void invokeInterface(MethodInsnNode node) {
		String desc = node.desc;
		collectVirtualCallArgs(desc);
		pushMethod(node);
		loadCtx();
		INVOKE_INTERFACE.emit(jit);
		toJava(Type.getReturnType(desc));
	}

	private void doCall(MethodInsnNode node) {
		MethodInsnNode method = methodCalls.computeIfAbsent(new MethodCallInfo(node), __ -> {
			String methodName = "methodCall" + methodCalls.size();
			String desc = node.desc;
			Type[] args = Type.getArgumentTypes(desc);
			int opcode = node.getOpcode();
			boolean vrt = opcode != INVOKESTATIC;
			// We need to rewrite java types to VM value type
			ensureVMValues(args);
			Type[] newArgs = Arrays.copyOf(args, args.length + (vrt ? 2 : 1));
			if (vrt) {
				// We need to insert `this`, so shift array to the right by one
				System.arraycopy(newArgs, 0, newArgs, 1, args.length);
				newArgs[0] = VALUE.type;
			}
			// Inject current context as new argument
			newArgs[newArgs.length - 1] = CTX.type;
			// Same as above
			Type returnType = ensureVMValue(Type.getReturnType(desc));
			String newDesc = Type.getMethodDescriptor(returnType, newArgs);
			MethodVisitor split = writer.visitMethod(ACC_PRIVATE | ACC_STATIC, methodName, newDesc, null, null);
			split.visitCode();
			int loadIndex = 0;
			if (vrt) {
				split.visitVarInsn(ALOAD, 0);
				loadIndex++;
			}
			for (Type arg : args) {
				split.visitVarInsn(arg.getOpcode(ILOAD), loadIndex);
				loadIndex += arg.getSize();
			}
			// Need to create new jit compiler for temporary usage
			// because calling invokeXX uses method that is being compiled
			JitCompiler jit = new JitCompiler(className, target, writer, split, constants, loadIndex);
			switch (opcode) {
				case INVOKEVIRTUAL:
					jit.invokeVirtual(node);
					break;
				case INVOKESPECIAL:
					jit.invokeSpecial(node);
					break;
				case INVOKESTATIC:
					jit.invokeStatic(node);
					break;
				case INVOKEINTERFACE:
					jit.invokeInterface(node);
					break;
				default:
					throw new IllegalStateException(Integer.toString(node.getOpcode()));
			}
			split.visitInsn(returnType.getOpcode(IRETURN));
			split.visitEnd();
			split.visitMaxs(-1, -1);
			return new MethodInsnNode(INVOKESTATIC, className, methodName, newDesc);
		});
		MethodVisitor jit = this.jit;
		// Pass context
		loadCtx();
		method.accept(jit);
	}

	private void invokeDynamic(InvokeDynamicInsnNode node) {
		MethodInsnNode method = invokeDynamicCalls.computeIfAbsent(new DynamicCallInfo(node.desc, node.bsm), __ -> {
			String methodName = "dynCall" + invokeDynamicCalls.size();
			String desc = node.desc;
			Type[] args = Type.getArgumentTypes(desc);
			// We need to rewrite java types to VM value type
			ensureVMValues(args);
			Type[] newArgs = Arrays.copyOf(args, args.length + 2);
			// Inject node index & current context as new arguments
			newArgs[args.length] = Type.INT_TYPE;
			newArgs[args.length + 1] = CTX.type;
			// Same as above
			Type returnType = ensureVMValue(Type.getReturnType(desc));
			String newDesc = Type.getMethodDescriptor(returnType, newArgs);
			MethodVisitor split = writer.visitMethod(ACC_PRIVATE | ACC_STATIC, methodName, newDesc, null, null);
			split.visitCode();
			int loadIndex = 0;
			for (Type arg : args) {
				split.visitVarInsn(arg.getOpcode(ILOAD), loadIndex);
				loadIndex += arg.getSize();
			}
			// Need to create new jit compiler for temporary usage
			// because calling collectArgs uses method that is being compiled
			new JitCompiler(className, target, writer, split, constants, loadIndex + 1).collectArgs(1, desc);
			loadConstants(split);
			split.visitVarInsn(ILOAD, loadIndex);
			split.visitVarInsn(Opcodes.ALOAD, loadIndex + 1);
			DYNAMIC_CALL.emit(split);
			toJava(returnType, split);
			split.visitInsn(returnType.getOpcode(IRETURN));
			split.visitEnd();
			split.visitMaxs(-1, -1);
			return new MethodInsnNode(INVOKESTATIC, className, methodName, newDesc);
		});
		MethodVisitor jit = this.jit;
		// Pass context, node index for rewriting
		emitInt(makeConstant(node), jit);
		loadCtx();
		method.accept(jit);
	}

	private void newInstance(String type) {
		Object jc = tryLoadClass(type);
		MethodVisitor jit = this.jit;
		if (jc instanceof InstanceJavaClass) {
			loadCompilerConstant(jc);
			loadCtx();
			NEW_INSTANCE.emit(jit);
		} else {
			// Slow path.
			jit.visitLdcInsn(type);
			loadCtx();
			NEW_INSTANCE_SLOW.emit(jit);
		}
	}

	private void newPrimitiveArray(int type) {
		MethodVisitor jit = this.jit;
		switch (type) {
			case T_LONG:
				NEW_PRIMITIVE_ARRAY_LONG.emit(jit);
				break;
			case T_DOUBLE:
				NEW_PRIMITIVE_ARRAY_DOUBLE.emit(jit);
				break;
			case T_INT:
				NEW_PRIMITIVE_ARRAY_INT.emit(jit);
				break;
			case T_FLOAT:
				NEW_PRIMITIVE_ARRAY_FLOAT.emit(jit);
				break;
			case T_CHAR:
				NEW_PRIMITIVE_ARRAY_CHAR.emit(jit);
				break;
			case T_SHORT:
				NEW_PRIMITIVE_ARRAY_SHORT.emit(jit);
				break;
			case T_BYTE:
				NEW_PRIMITIVE_ARRAY_BYTE.emit(jit);
				break;
			case T_BOOLEAN:
				NEW_PRIMITIVE_ARRAY_BOOLEAN.emit(jit);
				break;
			default:
				throw new IllegalStateException("Unknown array type " + type);
		}
	}

	private void newArray(String type) {
		Object jc = tryLoadClass(type);
		MethodVisitor jit = this.jit;
		if (jc instanceof JavaClass) {
			loadCompilerConstant(jc);
			loadCtx();
			NEW_INSTANCE_ARRAY.emit(jit);
		} else {
			// Slow path.
			jit.visitLdcInsn(type);
			loadCtx();
			NEW_INSTANCE_ARRAY_SLOW.emit(jit);
		}
	}

	private void multiNewArray(MultiANewArrayInsnNode node) {
		MethodInsnNode call = multiArrays.computeIfAbsent(new NewMultiArrayInfo(node.desc, node.dims), k -> {
			int dimensions = k.dimensions;
			Object klass = tryLoadClass(k.desc);
			String methodName = "multiNewArray" + multiArrays.size();
			StringBuilder callDesc = new StringBuilder();
			callDesc.append('(');
			for (int i = 0; i < dimensions; i++) {
				callDesc.append('I');
			}
			callDesc.append(CTX.desc);
			String desc = callDesc.append(')').append(VALUE.desc).toString();
			int loadIndex = dimensions;
			MethodVisitor split = writer.visitMethod(ACC_PRIVATE | ACC_STATIC, methodName, desc, null, null);
			split.visitCode();
			JitCompiler jit = new JitCompiler(className, target, writer, split, constants, loadIndex);
			if (klass instanceof JavaClass) {
				jit.loadCompilerConstant(klass);
			} else {
				split.visitLdcInsn(k.desc);
			}
			emitInt(dimensions, split);
			split.visitIntInsn(NEWARRAY, T_INT);
			for (int i = 0; i < dimensions; i++) {
				split.visitInsn(DUP);
				emitInt(i, split);
				split.visitVarInsn(ILOAD, i);
				split.visitInsn(IASTORE);
			}
			jit.loadCtx();
			if (klass instanceof JavaClass) {
				NEW_MULTI_ARRAY_INTRINSIC.emit(split);
			} else {
				NEW_MULTI_ARRAY.emit(split);
			}
			split.visitInsn(ARETURN);
			split.visitEnd();
			split.visitMaxs(-1, -1);
			return new MethodInsnNode(INVOKESTATIC, className, methodName, desc);
		});
		loadCtx();
		call.accept(jit);
	}

	private void checkCast(String type) {
		Object jc = tryLoadClass(type);
		MethodVisitor jit = this.jit;
		if (jc instanceof JavaClass) {
			loadCompilerConstant(jc);
			loadCtx();
			CHECK_CAST.emit(jit);
		} else {
			// Slow path.
			jit.visitLdcInsn(type);
			loadCtx();
			CHECK_CAST_SLOW.emit(jit);
		}
	}

	private void instanceofCheck(String type) {
		Object jc = tryLoadClass(type);
		MethodVisitor jit = this.jit;
		if (jc instanceof JavaClass) {
			loadCompilerConstant(jc);
			loadCtx();
			INSTANCEOF.emit(jit);
		} else {
			// Slow path.
			jit.visitLdcInsn(type);
			loadCtx();
			INSTANCEOF_SLOW.emit(jit);
		}
	}

	private Object tryLoadClass(String type) {
		InstanceJavaClass owner = target.getOwner();
		VMHelper helper = owner.getVM().getHelper();
		try {
			return helper.findClass(owner.getClassLoader(), type, false);
		} catch (VMException ex) {
			return type;
		}
	}

	private Object tryMethodType(Type type) {
		InstanceJavaClass owner = target.getOwner();
		VMHelper helper = owner.getVM().getHelper();
		try {
			return helper.methodType(owner.getClassLoader(), type);
		} catch (VMException ex) {
			return type;
		}
	}

	private MemoryManager getMemoryManager() {
		return target.getOwner().getVM().getMemoryManager();
	}

	private VMHelper getHelper() {
		return target.getOwner().getVM().getHelper();
	}

	private void collectArgs(int extra, String desc) {
		Type[] args = Type.getArgumentTypes(desc);
		int count = totalSize(args) + extra;
		MethodVisitor jit = this.jit;
		jit.visitLdcInsn(count);
		jit.visitTypeInsn(ANEWARRAY, VALUE.internalName);
		int idx = args.length;
		count--;
		while (idx-- != 0) {
			Type arg = args[idx];
			if (arg.getSize() == 2) {
				loadTopTo(count--);
			}
			loadArgTo(count--, arg);
		}
	}

	private void collectVirtualCallArgs(String desc) {
		collectArgs(1, desc);
		loadArgTo(0, VALUE.type);
	}

	private void collectStaticCallArgs(String desc) {
		collectArgs(0, desc);
	}

	private static int totalSize(Type[] args) {
		int size = 0;
		for (Type arg : args) {
			size += arg.getSize();
		}
		return size;
	}

	private void loadArgTo(int idx, Type type) {
		// value args
		MethodVisitor jit = this.jit;
		jit.visitInsn(DUP); // value args args
		jvm_swap(2, type.getSize()); // args args value
		toVM(type); // args args value
		jit.visitLdcInsn(idx); // args args value idx
		jit.visitInsn(SWAP);
		jit.visitInsn(AASTORE);
	}

	private void loadTopTo(int idx) {
		MethodVisitor jit = this.jit;
		jit.visitInsn(DUP); // args args
		jit.visitLdcInsn(idx); // args args idx
		GET_TOP.emit(jit); // args args idx value
		jit.visitInsn(AASTORE);
	}

	private void toJava(Type type) {
		toJava(type, jit);
	}

	private void toVM(Type type) {
		switch (type.getSort()) {
			case Type.LONG:
				longOf();
				break;
			case Type.DOUBLE:
				doubleOf();
				break;
			case Type.INT:
			case Type.CHAR:
			case Type.SHORT:
			case Type.BYTE:
			case Type.BOOLEAN:
				intOf();
				break;
			case Type.FLOAT:
				floatOf();
				break;
		}
	}

	private void dummyValue(Type type) {
		MethodVisitor jit = this.jit;
		switch (type.getSort()) {
			case Type.LONG:
				jit.visitInsn(LCONST_0);
				break;
			case Type.DOUBLE:
				jit.visitInsn(DCONST_0);
				break;
			case Type.INT:
				jit.visitInsn(ICONST_0);
				break;
			case Type.FLOAT:
				jit.visitInsn(FCONST_0);
				break;
			case Type.VOID:
				break;
			default:
				jit.visitInsn(ACONST_NULL);
		}
	}

	private void dropArgs(boolean vrt, Type[] args) {
		MethodVisitor jit = this.jit;
		for (Type arg : args) {
			jit.visitInsn(arg.getSize() == 2 ? POP2 : POP);
		}
		if (vrt) {
			jit.visitInsn(POP);
		}
	}

	private void dropArgs(boolean vrt, String desc) {
		dropArgs(vrt, Type.getArgumentTypes(desc));
	}

	private static void emitInt(int v, MethodVisitor visitor) {
		if (v >= -1 && v <= 5) {
			visitor.visitInsn(ICONST_0 + v);
		} else if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
			visitor.visitIntInsn(BIPUSH, v);
		} else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
			visitor.visitIntInsn(SIPUSH, v);
		} else {
			visitor.visitLdcInsn(v);
		}
	}

	private static AbstractInsnNode unmask(AbstractInsnNode insnNode) {
		if (insnNode instanceof DelegatingInsnNode) {
			return ((DelegatingInsnNode<?>) insnNode).getDelegate();
		}
		return insnNode;
	}

	private void loadConstants(MethodVisitor mv) {
		mv.visitFieldInsn(GETSTATIC, className, "constants", "[Ljava/lang/Object;");
	}

	private void loadCtx(MethodVisitor mv) {
		mv.visitVarInsn(ALOAD, ctxIndex);
	}

	private static void toJava(Type type, MethodVisitor mv) {
		switch (type.getSort()) {
			case Type.LONG:
				AS_LONG.emit(mv);
				break;
			case Type.DOUBLE:
				AS_DOUBLE.emit(mv);
				break;
			case Type.INT:
				AS_INT.emit(mv);
				break;
			case Type.FLOAT:
				AS_FLOAT.emit(mv);
				break;
			case Type.CHAR:
				AS_INT.emit(mv);
				mv.visitInsn(I2C);
				break;
			case Type.SHORT:
				AS_INT.emit(mv);
				mv.visitInsn(I2S);
				break;
			case Type.BYTE:
			case Type.BOOLEAN:
				AS_INT.emit(mv);
				mv.visitInsn(I2B);
				break;
			case Type.VOID:
				mv.visitInsn(POP);
		}
	}

	private static Type ensureVMValue(Type type) {
		return type.getSort() >= Type.ARRAY ? VALUE.type : type;
	}

	private static void ensureVMValues(Type[] types) {
		for (int i = 0, j = types.length; i < j; types[i] = ensureVMValue(types[i++])) {
		}
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

	static {
		boolean canUseStaticFinal;
		try {
			UnsafeUtil.get().staticFieldBase(JitCompiler.class.getDeclaredField("CAN_USE_STATIC_FINAL"));
			canUseStaticFinal = true;
		} catch (NoSuchFieldException ex) {
			throw new ExceptionInInitializerError(ex);
		} catch (NoSuchMethodError ex) {
			canUseStaticFinal = false;
		}
		CAN_USE_STATIC_FINAL = canUseStaticFinal;
	}

	private static final class ClassType {

		final Type type;
		final String internalName;
		final String desc;

		private ClassType(Class<?> klass) {
			Type type = this.type = Type.getType(klass);
			internalName = type.getInternalName();
			desc = type.getDescriptor();
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
			int opcode = this.opcode;
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
			StringBuilder b = new StringBuilder();
			b.append('(');
			for (ClassType arg : args) {
				b.append(arg.desc);
			}
			return b.append(')').append(rt.desc).toString();
		}
	}

	@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class DynamicCallInfo {
		private final String desc;
		private final Handle handle;
	}

	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class MethodCallInfo {
		int opcode;
		String owner;
		String name;
		String desc;

		MethodCallInfo(MethodInsnNode node) {
			this(node.getOpcode(), node.owner, node.name, node.desc);
		}
	}

	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private static final class NewMultiArrayInfo {
		String desc;
		int dimensions;
	}
}
