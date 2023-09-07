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
	private final InstructionProcessor[] processors = new InstructionProcessor[MAX_INSNS];
	private final Map<JavaMethod, MethodInvoker> invokerMap = new HashMap<>();
	private final List<MethodEnterListener> methodEnters = new ArrayList<>();
	private final List<MethodExitListener> methodExits = new ArrayList<>();
	private final List<MethodEnterListener> methodEntersView = Collections.unmodifiableList(methodEnters);
	private final List<MethodExitListener> methodExitsView = Collections.unmodifiableList(methodExits);
	private final List<InstructionInterceptor> instructionInterceptors = new ArrayList<>();
	private final List<InstructionInterceptor> instructionInterceptorsView = Collections.unmodifiableList(instructionInterceptors);
	private Consumer<ExecutionContext<?>> linkageErrorHandler = SimpleVMInterface::handleLinkageError0;
	private Consumer<ExecutionContext<?>> abstractMethodHandler = SimpleVMInterface::handleAbstractMethodError0;

	public SimpleVMInterface() {
		Arrays.fill(processors, new UnknownInstructionProcessor());
	}

	@Override
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return processors[insn.getOpcode()];
	}

	@Override
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
	public void handleLinkageError(ExecutionContext<?> ctx) {
		linkageErrorHandler.accept(ctx);
	}

	@Override
	public void handleAbstractMethodError(ExecutionContext<?> ctx) {
		abstractMethodHandler.accept(ctx);
	}

	// Default impl for handling linkage errors is to throw UnsatisfiedLinkError
	private static void handleLinkageError0(ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ctx.getSymbols().java_lang_UnsatisfiedLinkError(), ctx.getMethod().toString());
	}

	// Default impl for handling abstract method errors is to throw AbstractMethodError
	private static void handleAbstractMethodError0(ExecutionContext<?> ctx) {
		ctx.getOperations().throwException(ctx.getSymbols().java_lang_AbstractMethodError(), ctx.getMethod().toString());
	}
}