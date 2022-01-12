package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
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
		var vm = ctx.getVM();
		var helper = vm.getHelper();
		var symbols = vm.getSymbols();
		var bootstrap = insn.bsm;
		var classLoader = ctx.getOwner().getClassLoader();
		try {
			if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
				helper.throwException(symbols.java_lang_IllegalStateException, "Bootstrap tag is not static");
			}
			var owner = bootstrap.getOwner();
			var jc = helper.findClass(classLoader, owner, true);
			if (!(jc instanceof InstanceJavaClass)) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, owner);
			}
			var name = bootstrap.getName();
			var desc = bootstrap.getDesc();
			var bootstrapMethod = ((InstanceJavaClass) jc).getStaticMethod(name, desc);
			if (bootstrapMethod == null) {
				helper.throwException(symbols.java_lang_NoSuchMethodException, owner + '.' + name + desc);
			}

			var caller = ctx.getOwner();

			// Call MethodHandleNatives#linkMethodHandleConstant
			var args = new Value[]{
					caller.getOop(),
					new IntValue(bootstrap.getTag()),
					jc.getOop(),
					helper.newUtf8(name),
					helper.methodType(caller.getClassLoader(), Type.getMethodType(desc))
			};
			var linker = (InstanceValue) helper.invokeStatic(symbols.java_lang_invoke_MethodHandleNatives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", new Value[0], args).getResult();

		} catch (VMException ex) {
			var oop = ex.getOop();
			helper.throwException(symbols.java_lang_BootstrapMethodError, "CallSite initialization exception", oop);
		}
		return Result.CONTINUE;
	}
}
