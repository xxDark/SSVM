package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import org.objectweb.asm.Opcodes;

/**
 * Invoker that passes the control to
 * the interpreter to run a method.
 *
 * @author xDark
 */
public final class InterpretedInvoker implements MethodInvoker {

	@Override
	public Result intercept(ExecutionContext<?> ctx) {
		JavaMethod method = ctx.getMethod();
		int access = method.getModifiers();
		VMInterface vmi = ctx.getVM().getInterface();
		if ((access & Opcodes.ACC_NATIVE) != 0) {
			vmi.handleLinkageError(ctx);

			// The linkage error handling should throw before we get here, unless it is overridden by a user.
			// In that case we still want to abort.
			return Result.ABORT;
		}
		if ((access & Opcodes.ACC_ABSTRACT) != 0) {
			vmi.handleAbstractMethodError(ctx);

			// The abstract method error handling should throw before we get here, unless it is overridden by a user.
			// In that case we still want to abort.
			return Result.ABORT;
		}

		Interpreter.execute(ctx);
		return Result.ABORT;
	}
}
