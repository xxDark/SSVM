package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * VM InvokeDynamic operations.
 *
 * @author xDark
 */
public interface InvokeDynamicOperations {

	int MN_IS_METHOD = 0x00010000,
		MN_IS_CONSTRUCTOR = 0x00020000,
		MN_IS_FIELD = 0x00040000,
		MN_IS_TYPE = 0x00080000,
		MN_CALLER_SENSITIVE = 0x00100000,
		MN_REFERENCE_KIND_SHIFT = 24,
		MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
		MN_SEARCH_SUPERCLASSES = 0x00100000,
		MN_SEARCH_INTERFACES = 0x00200000;
	byte REF_getField = 1,
		REF_getStatic = 2,
		REF_putField = 3,
		REF_putStatic = 4,
		REF_invokeVirtual = 5,
		REF_invokeStatic = 6,
		REF_invokeSpecial = 7,
		REF_newInvokeSpecial = 8,
		REF_invokeInterface = 9;
	int IS_METHOD = MN_IS_METHOD,
		IS_CONSTRUCTOR = MN_IS_CONSTRUCTOR,
		IS_FIELD = MN_IS_FIELD,
		IS_TYPE = MN_IS_TYPE;
	int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;

	/**
	 * Links {@link InvokeDynamicInsnNode}.
	 *
	 * @param insn   Node to link.
	 * @param caller Method caller.
	 * @return Linked method handle or call site.
	 */
	InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceClass caller);

	/**
	 * Links {@link org.objectweb.asm.ConstantDynamic}.
	 *
	 * @param node   Node to link.
	 * @param caller Method caller.
	 * @return Resulting constant.
	 */
	InstanceValue linkDynamic(ConstantDynamic node, InstanceClass caller);

	/**
	 * Invokes linked dynamic call.
	 *
	 * @param stack  Stack to sink arguments from.
	 * @param desc   Call descriptor.
	 * @param handle Call site or method handle.
	 * @param sink   Result sink.
	 */
	void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink);

	/**
	 * Reads method handle target.
	 *
	 * @param handle Handle to read target from.
	 * @return Method handle target.
	 * Throws VM exception if handle is not initialized.
	 */
	JavaMethod readVMTargetFromHandle(InstanceValue handle);

	/**
	 * Reads method handle target.
	 *
	 * @param vmentry Member name to read target from.
	 * @return Method handle target.
	 * Throws VM exception if handle is not initialized.
	 */
	JavaMethod readVMTargetFromMemberName(InstanceValue vmentry);

	/**
	 * Initializes method member.
	 *
	 * @param refKind    Reference kind.
	 * @param memberName Member name instance.
	 * @param handle     Method handle.
	 * @param mnType     Linkage type.
	 */
	void initMethodMember(int refKind, InstanceValue memberName, JavaMethod handle, int mnType);

	/**
	 * Initializes field member.
	 *
	 * @param refKind    Reference kind.
	 * @param memberName Member name instance.
	 * @param handle     Field handle.
	 */
	void initFieldMember(int refKind, InstanceValue memberName, JavaField handle);
}
