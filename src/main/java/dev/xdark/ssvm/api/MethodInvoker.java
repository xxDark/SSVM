package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;

/**
 * Instruments method call.
 *
 * @author xDark
 */
public interface MethodInvoker {

	Result intercept(ExecutionContext ctx);

	/**
	 * @return method invoker that does nothing.
	 */
	static MethodInvoker noop() {
		return ctx -> Result.ABORT;
	}
}
