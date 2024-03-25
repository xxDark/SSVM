package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;
import java.util.function.Consumer;

/**
 * Simple implementation of interfacing capabilities to configure/adjust the VM.
 *
 * @author xDark
 */
public class SimpleVMInterface implements VMInterface {
	private static final int MAX_INSNS = 1024;
	private final InstructionProcessor[] processors;
	private final Map<JavaMethod, MethodInvoker> invokerMap;
	private final List<MethodEnterListener> methodEnters;
	private final List<MethodExitListener> methodExits;
	private final List<MethodEnterListener> methodEntersView;
	private final List<MethodExitListener> methodExitsView;
	private final List<InstructionInterceptor> instructionInterceptors;
	private final List<InstructionInterceptor> instructionInterceptorsView;
	private Consumer<ExecutionContext<?>> linkageErrorHandler = SimpleVMInterface::handleLinkageError0;
	private Consumer<ExecutionContext<?>> abstractMethodHandler = SimpleVMInterface::handleAbstractMethodError0;
	private Consumer<ExecutionContext<?>> maxIterationsHandler = SimpleVMInterface::handleMaxIterations0;

	private SimpleVMInterface(InstructionProcessor<?>[] processors, Map<JavaMethod, MethodInvoker> invokerMap,
							  List<MethodEnterListener> methodEnters, List<MethodExitListener> methodExits,
							  List<InstructionInterceptor> instructionInterceptors) {
		this.processors = processors;
		this.invokerMap = invokerMap;
		this.methodEnters = methodEnters;
		this.methodExits = methodExits;
		this.instructionInterceptors = instructionInterceptors;

		methodEntersView = Collections.unmodifiableList(methodEnters);
		methodExitsView = Collections.unmodifiableList(methodExits);
		instructionInterceptorsView = Collections.unmodifiableList(instructionInterceptors);
	}

	public SimpleVMInterface() {
		this(new InstructionProcessor[MAX_INSNS],
				new HashMap<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>()
		);

		Arrays.fill(processors, new UnknownInstructionProcessor());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return processors[insn.getOpcode()];
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(int opcode) {
		return processors[opcode];
	}

	@Override
	public void setProcessor(int opcode, InstructionProcessor<?> processor) {
		processors[opcode] = processor;
	}

	@Override
	public MethodInvoker getInvoker(JavaMethod method) {
		return invokerMap.get(method);
	}

	@Override
	public void setInvoker(JavaMethod method, MethodInvoker invoker) {
		invokerMap.put(method, invoker);
	}

	@Override
	public boolean setInvoker(InstanceClass jc, String name, String desc, MethodInvoker invoker) {
		JavaMethod method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		setInvoker(method, invoker);
		return true;
	}

	@Override
	public void onMethodEnter(ExecutionContext<?> ctx) {
		for (MethodEnterListener listener : methodEnters) {
			listener.handle(ctx);
		}
	}

	@Override
	public void onMethodExit(ExecutionContext<?> ctx) {
		for (MethodExitListener listener : methodExits) {
			listener.handle(ctx);
		}
	}

	@Override
	public void registerInstructionInterceptor(InstructionInterceptor interceptor) {
		instructionInterceptors.add(interceptor);
	}

	@Override
	public void removeInstructionInterceptor(InstructionInterceptor interceptor) {
		instructionInterceptors.remove(interceptor);
	}

	@Override
	public void registerMethodEnterListener(MethodEnterListener listener) {
		methodEnters.add(listener);
	}

	@Override
	public void registerMethodExitListener(MethodExitListener listener) {
		methodExits.add(listener);
	}

	@Override
	public void removeMethodEnterListener(MethodEnterListener listener) {
		methodEnters.remove(listener);
	}

	@Override
	public void removeMethodExitListener(MethodExitListener listener) {
		methodExits.remove(listener);
	}

	@Override
	public List<InstructionInterceptor> getInstructionInterceptors() {
		return instructionInterceptorsView;
	}

	@Override
	public List<MethodEnterListener> getMethodEnterListeners() {
		return methodEntersView;
	}

	@Override
	public List<MethodExitListener> getMethodExitListeners() {
		return methodExitsView;
	}

	@Override
	public void setLinkageErrorHandler(Consumer<ExecutionContext<?>> linkageErrorHandler) {
		this.linkageErrorHandler = linkageErrorHandler;
	}

	@Override
	public void setAbstractMethodHandler(Consumer<ExecutionContext<?>> abstractMethodHandler) {
		this.abstractMethodHandler = abstractMethodHandler;
	}

	@Override
	public void setMaxIterationsHandler(Consumer<ExecutionContext<?>> maxIterationsHandler) {
		this.maxIterationsHandler = maxIterationsHandler;
	}

	@Override
	public void handleLinkageError(ExecutionContext<?> ctx) {
		linkageErrorHandler.accept(ctx);
	}

	@Override
	public void handleAbstractMethodError(ExecutionContext<?> ctx) {
		abstractMethodHandler.accept(ctx);
	}

	@Override
	public void handleMaxInterations(ExecutionContext<?> ctx) {
		maxIterationsHandler.accept(ctx);
	}

	@Override
	public VMInterface copy() {
		SimpleVMInterface copy = new SimpleVMInterface(processors, new HashMap<>(invokerMap),
				new ArrayList<>(methodEnters), new ArrayList<>(methodExits),
				new ArrayList<>(instructionInterceptors)
		);
		System.arraycopy(processors, 0, copy.processors, 0, Math.min(processors.length, copy.processors.length));
		return copy;
	}

	// Default impl for handling linkage errors is to throw UnsatisfiedLinkError
	private static void handleLinkageError0(ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ctx.getSymbols().java_lang_UnsatisfiedLinkError(), ctx.getMethod().toString());
	}

	// Default impl for handling abstract method errors is to throw AbstractMethodError
	private static void handleAbstractMethodError0(ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ctx.getSymbols().java_lang_AbstractMethodError(), ctx.getMethod().toString());
	}

	// Default impl for handling max iteration is to throw IllegalStateException
	private static void handleMaxIterations0(ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ctx.getSymbols().java_lang_IllegalStateException(), ctx.getMethod().toString());
	}
}