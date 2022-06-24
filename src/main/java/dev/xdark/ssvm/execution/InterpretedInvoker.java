package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.Opcodes;

/**
 * Invoker that passes the control to
 * the interpreter to run a method.
 *
 * @author xDark
 */
public final class InterpretedInvoker implements MethodInvoker {

	@Override
	public Result intercept(ExecutionContext ctx) {
		JavaMethod method = ctx.getMethod();
		int access = method.getAccess();
		if ((access & Opcodes.ACC_NATIVE) != 0) {
			throwLinkageError(ctx);
		}
		if ((access & Opcodes.ACC_ABSTRACT) != 0) {
			throwAbstractError(ctx);
		}
		Interpreter.execute(ctx);
		return Result.ABORT;
	}

	private static void throwLinkageError(ExecutionContext ctx) {
		ctx.getHelper().throwException(ctx.getSymbols().java_lang_UnsatisfiedLinkError(), ctx.getMethod().toString());
	}

	private static void throwAbstractError(ExecutionContext ctx) {
		ctx.getHelper().throwException(ctx.getSymbols().java_lang_AbstractMethodError(), ctx.getMethod().toString());
	}
}
