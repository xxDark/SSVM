package dev.xdark.ssvm.util;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * InvokeDynamic linkage logic.
 *
 * @author xDark
 */
@UtilityClass
public class InvokeDynamicLinker {

	/**
	 * Links {@link InvokeDynamicInsnNode}.
	 *
	 * @param insn
	 * 		Node to link.
	 * @param ctx
	 * 		Execution context.
	 *
	 * @return Linked method handle or call site.
	 */
	public InstanceValue linkCall(InvokeDynamicInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val bootstrap = insn.bsm;
		try {

			if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
				helper.throwException(symbols.java_lang_IllegalStateException, "Bootstrap tag is not static");
			}
			val caller = ctx.getOwner();

			val linker = helper.linkMethodHandleConstant(caller, bootstrap);

			val $bsmArgs = insn.bsmArgs;
			val bsmArgs = new ObjectValue[$bsmArgs.length];
			for (int i = 0; i < bsmArgs.length; i++) {
				bsmArgs[i] = helper.forInvokeDynamicCall($bsmArgs[i]);
			}

			val stringPool = vm.getStringPool();
			val appendix = helper.newArray(symbols.java_lang_Object, 1);
			val args = helper.toVMValues(bsmArgs);
			val linkArgs = new Value[]{
					caller.getOop(),
					linker,
					stringPool.intern(insn.name),
					helper.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)),
					args,
					appendix
			};

			val natives = symbols.java_lang_invoke_MethodHandleNatives;
			helper.invokeStatic(natives, "linkCallSite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;", new Value[0], linkArgs);
			return (InstanceValue) appendix.getValue(0);
		} catch (VMException ex) {
			val oop = ex.getOop();
			helper.throwException(symbols.java_lang_BootstrapMethodError, "CallSite initialization exception", oop);
			return null;
		}
	}

	/**
	 * Invokes linked dynamic call.
	 *
	 * @param args
	 * 		Call arguments.
	 * @param desc
	 * 		Call descriptor.
	 * @param handle
	 * 		Call site or method handle.
	 * @param ctx
	 * 		Execution context.
	 *
	 * @return invocation result.
	 */
	public Value dynamicCall(Value[] args, String desc, InstanceValue handle, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		if (vm.getSymbols().java_lang_invoke_CallSite.isAssignableFrom(handle.getJavaClass())) {
			handle = helper.checkNotNull(helper.invokeVirtual("getTarget", "()Ljava/lang/invoke/MethodHandle;", new Value[0], new Value[]{handle}).getResult());
		}
		if (args[0] == null) {
			// The slot was reserved, use it
			args[0] = handle;
		} else {
			// Need to copy
			val copy = new Value[args.length + 1];
			System.arraycopy(args, 0, copy, 1, args.length);
			copy[0] = handle;
			args = copy;
		}
		return helper.invokeVirtual("invokeExact", desc, new Value[0], args).getResult();
	}
}
