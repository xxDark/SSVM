package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Links invokedynamic calls.
 *
 * @author xDark
 */
public final class InvokeDynamicLinkerProcessor implements InstructionProcessor<InvokeDynamicInsnNode> {

	@Override
	public Result execute(InvokeDynamicInsnNode insn, ExecutionContext ctx) {
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

			val linked = (InstanceValue) appendix.getValue(0);

			// Rewrite instruction
			val list = ctx.getMethod().getNode().instructions;
			list.set(insn, new LinkedDynamicCallNode(insn, linked));
			// Move insn position backwards so that VM visits
			// us yet again.
			ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		} catch (VMException ex) {
			val oop = ex.getOop();
			helper.throwException(symbols.java_lang_BootstrapMethodError, "CallSite initialization exception", oop);
		}
		return Result.CONTINUE;
	}
}
