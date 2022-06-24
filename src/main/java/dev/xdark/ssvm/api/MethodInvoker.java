package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Interpreter;
import dev.xdark.ssvm.execution.Result;

/**
 * Instruments method call.
 *
 * @author xDark
 */
public interface MethodInvoker {

	/**
	 * Intercepts execution content.
	 *
	 * @param ctx content to intercept.
	 * @return interception result.
	 */
	Result intercept(ExecutionContext ctx);

	/**
	 * @return method invoker that does nothing.
	 */
	static MethodInvoker noop() {
		return ctx -> Result.ABORT;
	}

	/**
	 * Returns new invoker that passes the control back to the interpreter
	 * if {@link Result#CONTINUE} is the result of invocation.
	 *
	 * @param invoker backing invoker.
	 * @return new invoker.
	 */
	static MethodInvoker interpreted(MethodInvoker invoker) {
		return ctx -> {
			Result result = invoker.intercept(ctx);
			if (result == Result.CONTINUE) {
				// Pass control back to interpreter
				Interpreter.execute(ctx);
			}
			return Result.ABORT;
		};
	}
}
