package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
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
		val classLoader = ctx.getOwner().getClassLoader();
		try {
			if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
				helper.throwException(symbols.java_lang_IllegalStateException, "Bootstrap tag is not static");
			}
			val owner = bootstrap.getOwner();
			val jc = helper.findClass(classLoader, owner, true);
			if (!(jc instanceof InstanceJavaClass)) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, owner);
			}
			val name = bootstrap.getName();
			val desc = bootstrap.getDesc();
			val bootstrapMethod = ((InstanceJavaClass) jc).getStaticMethod(name, desc);
			if (bootstrapMethod == null) {
				helper.throwException(symbols.java_lang_NoSuchMethodException, owner + '.' + name + desc);
			}

			val caller = ctx.getOwner();

			// Call MethodHandleNatives#linkMethodHandleConstant
			val args = new Value[]{
					caller.getOop(),
					new IntValue(bootstrap.getTag()),
					jc.getOop(),
					helper.newUtf8(name),
					helper.methodType(caller.getClassLoader(), Type.getMethodType(desc))
			};
			val linker = (InstanceValue) helper.invokeStatic(symbols.java_lang_invoke_MethodHandleNatives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", new Value[0], args).getResult();

		} catch (VMException ex) {
			val oop = ex.getOop();
			helper.throwException(symbols.java_lang_BootstrapMethodError, "CallSite initialization exception", oop);
		}
		return Result.CONTINUE;
	}
}
