package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvocation;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.util.BoundedQueue;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.Opcodes;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple execution engine.
 *
 * @author xDark
 */
public class SimpleExecutionEngine implements ExecutionEngine {

	private static final ExecutionOptions DEFAULT_OPTIONS = ExecutionOptions.builder().build();
	private final Queue<SimpleExecutionContext<?>> contentQueue;
	private final EngineReference<ExecutionContext<?>> contentReference;
	protected final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public SimpleExecutionEngine(VirtualMachine vm) {
		this.vm = vm;
		Queue<SimpleExecutionContext<?>> contextQueue = new BoundedQueue<>(new ConcurrentLinkedQueue<>(), 128);
		this.contentQueue = contextQueue;
		contentReference = context -> contextQueue.offer((SimpleExecutionContext<?>) context);
	}

	@Override
	public void execute(ExecutionContext<?> ctx) {
		JavaMethod jm = ctx.getMethod();
		int access = jm.getModifiers();
		boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
		if (isNative) {
			ctx.setLineNumber(-2);
		}
		ExecutionOptions options = ctx.getOptions();
		VirtualMachine vm = this.vm;
		ThreadManager threadManager = vm.getThreadManager();
		Backtrace backtrace = threadManager.currentThread().getBacktrace();
		backtrace.push(((SimpleExecutionContext<?>) ctx).frame);
		VMInterface vmi = vm.getInterface();
		jm.increaseInvocation();
		ObjectValue lock = null;
		if (options.useEnterLocking() && (access & Opcodes.ACC_SYNCHRONIZED) != 0) {
			if (((access & Opcodes.ACC_STATIC)) == 0) {
				lock = ctx.getLocals().loadReference(0);
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
			Result result = vmi.getInvoker(jm).intercept(ctx);
			if (result == Result.ABORT) {
				return;
			}
			vm.getHelper().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError(), jm.toString());
		} catch (VMException ex) {
			throw ex;
		} catch (Exception ex) {
			doCleanup = false;
			throw new IllegalStateException("Uncaught VM error at: " + jm, ex);
		} catch (Throwable t) {
			doCleanup = false;
			throw t;
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
						try {
							if (lock != null) {
								ctx.monitorExit(lock);
							}
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
	public <R extends ValueSink> ExecutionContext<R> createContext(ExecutionRequest<R> request) {
		SimpleExecutionContext<R> context = (SimpleExecutionContext<R>) contentQueue.poll();
		if (context == null) {
			context = new SimpleExecutionContext<>(contentReference);
		}
		context.init(request.getOptions(), request.getMethod(), request.getStack(), request.getLocals(), request.getResultSink());
		return context;
	}

	@Override
	public ExecutionOptions defaultOptions() {
		return DEFAULT_OPTIONS;
	}
}
