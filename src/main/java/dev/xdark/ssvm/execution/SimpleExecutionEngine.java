package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvocation;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Opcodes;

/**
 * Simple execution engine.
 *
 * @author xDark
 */
public class SimpleExecutionEngine implements ExecutionEngine {

	private static final ExecutionContextOptions DEFAULT_OPTIONS = ExecutionContextOptions.builder().build();
	protected final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public SimpleExecutionEngine(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public void execute(ExecutionContext ctx, ExecutionContextOptions options) {
		JavaMethod jm = ctx.getMethod();
		int access = jm.getAccess();
		boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
		if (isNative) {
			ctx.setLineNumber(-2);
		}
		VirtualMachine vm = this.vm;
		ThreadManager threadManager = vm.getThreadManager();
		Backtrace backtrace = threadManager.currentThread().getBacktrace();
		backtrace.push(StackFrame.ofContext(ctx));
		VMInterface vmi = vm.getInterface();
		jm.increaseInvocation();
		ObjectValue lock = null;
		if (options.useEnterLocking() && (access & Opcodes.ACC_SYNCHRONIZED) != 0) {
			if (((access & Opcodes.ACC_STATIC)) == 0) {
				lock = ctx.getLocals().load(0);
			} else {
				lock = jm.getOwner().getOop();
			}
			ctx.monitorEnter(lock);
		}
		boolean useInvocationHooks = options.useInvocationHooks();
		boolean doCleanup = true;
		try {
			if (useInvocationHooks) {
				for (MethodInvocation invocation : vmi.getInvocationHooks(jm, true)) {
					invocation.handle(ctx);
				}
			}
			if (options.searchForHooks()) {
				MethodInvoker invoker = vmi.getInvoker(jm);
				if (invoker != null) {
					Result result = invoker.intercept(ctx);
					if (result == Result.ABORT) {
						return;
					}
				}
			}
			if (isNative) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError(), ctx.getOwner().getInternalName() + '.' + jm.getName() + jm.getDesc());
			}
			if ((access & Opcodes.ACC_ABSTRACT) != 0) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_AbstractMethodError(), ctx.getOwner().getInternalName() + '.' + jm.getName() + jm.getDesc());
			}
			Interpreter.execute(ctx, options);
		} catch (VMException ex) {
			throw ex;
		} catch (Exception ex) {
			doCleanup = false;
			throw new IllegalStateException("Uncaught VM error at: " + jm, ex);
		} finally {
			if (doCleanup) {
				try {
					try {
						if (useInvocationHooks) {
							for (MethodInvocation invocation : vmi.getInvocationHooks(jm, false)) {
								invocation.handle(ctx);
							}
						}
					} finally {
						if (lock != null) {
							ctx.monitorExit(lock);
						}
						try {
							ctx.verifyMonitors();
						} finally {
							DisposeUtil.dispose(ctx);
						}
					}
				} finally {
					backtrace.pop();
				}
			}
		}
	}

	@Override
	public ExecutionContext createContext(JavaMethod method, Stack stack, Locals locals) {
		return new SimpleExecutionContext(method, stack, locals);
	}

	@Override
	public ExecutionContextOptions defaultOptions() {
		return DEFAULT_OPTIONS;
	}
}
