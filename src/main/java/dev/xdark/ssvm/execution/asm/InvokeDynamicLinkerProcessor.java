package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
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
			var jc = vm.findClass(classLoader, bootstrap.getOwner(), true);
			if (!(jc instanceof InstanceJavaClass)) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, bootstrap.getOwner());
			}
			var bootstrapMethod = ((InstanceJavaClass) jc).getMethod(bootstrap.getName(), bootstrap.getDesc());
		} catch (VMException ex) {
			var oop = ex.getOop();
			helper.throwException(symbols.java_lang_BootstrapMethodError, "CallSite initialization exception", oop);
		}
		return Result.CONTINUE;
	}
}
