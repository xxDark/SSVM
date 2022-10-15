package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvocation;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.Opcodes;

/**
 * Simple execution engine.
 *
 * @author xDark
 */
public class SimpleExecutionEngine implements ExecutionEngine {

	private static final ExecutionOptions DEFAULT_OPTIONS = ExecutionOptions.builder().build();
	private final VirtualMachine vm;

	public SimpleExecutionEngine(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public <R extends ValueSink> ExecutionContext<R> execute(ExecutionRequest<R> request) {
		ThreadManager threadManager = vm.getThreadManager();
		Backtrace backtrace = threadManager.currentOsThread().getBacktrace();
		ExecutionContext<R> ctx = backtrace.push(request);
		JavaMethod jm = ctx.getMethod();
		int access = jm.getModifiers();
		boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
		if (isNative) {
			ctx.setLineNumber(-2);
		}
		VMInterface vmi = vm.getInterface();
		jm.increaseInvocation();
		ObjectValue lock = null;
		if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
			if (((access & Opcodes.ACC_STATIC)) == 0) {
				lock = ctx.getLocals().loadReference(0);
			} else {
				lock = jm.getOwner().getOop();
			}
			vm.getOperations().monitorEnter(lock);
		}
		boolean doCleanup = true;
		try {
			for (MethodInvocation invocation : vmi.getInvocationHooks(jm, true)) {
				invocation.handle(ctx);
			}
			Result result = vmi.getInvoker(jm).intercept(ctx);
			if (result == Result.ABORT) {
				return ctx;
			}
			vm.getOperations().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError(), jm.toString());
		} catch (VMException ex) {
			throw ex;
		} catch (Exception ex) {
			doCleanup = false;
			throw new PanicException("Uncaught VM error at: " + jm, ex);
		} catch (Throwable t) {
			doCleanup = false;
			throw t;
		} finally {
			if (doCleanup) {
				try {
					if (lock != null) {
						vm.getOperations().monitorExit(lock);
					}
				} finally {
					try {
						try {
							for (MethodInvocation invocation : vmi.getInvocationHooks(jm, false)) {
								invocation.handle(ctx);
							}
						} finally {
							DisposeUtil.dispose(ctx);
						}
					} finally {
						backtrace.pop();
					}
				}
			}
		}
		throw new PanicException("dead code");
	}

	@Override
	public ExecutionOptions defaultOptions() {
		return DEFAULT_OPTIONS;
	}
}
