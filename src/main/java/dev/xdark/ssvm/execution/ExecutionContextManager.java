package dev.xdark.ssvm.execution;

/**
 * Responsible for managing of {@link ExecutionContext}.
 *
 * @author xDark
 */
public interface ExecutionContextManager {

	/**
	 * Creates new execution context.
	 *
	 * @param request execution request.
	 * @return new execution context.
	 */
	ExecutionContext createContext(ExecutionRequest request);
}
