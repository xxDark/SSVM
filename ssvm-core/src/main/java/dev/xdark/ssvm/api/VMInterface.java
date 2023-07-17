package dev.xdark.ssvm.api;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Interpreter;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public final class VMInterface {
	private static final int MAX_INSNS = 1024;
	private final InstructionProcessor[] processors = new InstructionProcessor[MAX_INSNS];
	private final Map<JavaMethod, MethodInvoker> invokerMap = new HashMap<>();
	private final List<MethodEnterListener> methodEnters = new ArrayList<>();
	private final List<MethodExitListener> methodExits = new ArrayList<>();
	private final List<MethodEnterListener> methodEntersView = Collections.unmodifiableList(methodEnters);
	private final List<MethodExitListener> methodExitsView  = Collections.unmodifiableList(methodExits);
	private final List<InstructionInterceptor> instructionInterceptors = new ArrayList<>();
	private final List<InstructionInterceptor> instructionInterceptorsView = Collections.unmodifiableList(instructionInterceptors);
	private Consumer<ExecutionContext<?>> linkageErrorHandler = VMInterface::handleLinkageError0;
	private Consumer<ExecutionContext<?>> abstractMethodHandler = VMInterface::handleAbstractMethodError0;

	public VMInterface() {
		Arrays.fill(processors, new UnknownInstructionProcessor());
	}

	/**
	 * Gets an instruction processor.
	 *
	 * @param <I>  Type of the instruction.
	 * @param insn Instruction to get processor for.
	 * @return instruction processor.
	 */
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return processors[insn.getOpcode()];
	}

	/**
	 * Gets an instruction processor by opcode.
	 *
	 * @param opcode Instruction opcode.
	 * @param <I>    Instruction type.
	 * @return instruction processor.
	 */
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(int opcode) {
		return processors[opcode];
	}

	/**
	 * Sets an instruction processor.
	 *
	 * @param opcode    Opcode of the instruction.
	 * @param processor Processor of the opcode.
	 */
	public void setProcessor(int opcode, InstructionProcessor<?> processor) {
		processors[opcode] = processor;
	}

	/**
	 * Returns method invoker based off a method.
	 *
	 * @param method method to search invoker for.
	 * @return method invoker.
	 */
	public MethodInvoker getInvoker(JavaMethod method) {
		return invokerMap.get(method);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param method  Method to set invoker for.
	 * @param invoker Method invoker.
	 */
	public void setInvoker(JavaMethod method, MethodInvoker invoker) {
		invokerMap.put(method, invoker);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param jc      Instance class.
	 * @param name    Name of the method.
	 * @param desc    Descriptor of the method.
	 * @param invoker Method invoker.
	 * @return {@code true} if method was registered,
	 * {@code false} otherwise.
	 * @see Modifier#ACC_COMPILED
	 */
	public boolean setInvoker(InstanceClass jc, String name, String desc, MethodInvoker invoker) {
		JavaMethod method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		setInvoker(method, invoker);
		return true;
	}

	/**
	 * Called by {@link Interpreter} when a method starts execution.
	 *
	 * @param ctx Context of the method being executed.
	 */
	public void onMethodEnter(ExecutionContext<?> ctx) {
		for (MethodEnterListener listener : methodEnters) {
			listener.handle(ctx);
		}
	}

	/**
	 * Called by {@link Interpreter} when a method finishes execution.
	 *
	 * @param ctx Context of the method being executed.
	 */
	public void onMethodExit(ExecutionContext<?> ctx) {
		for (MethodExitListener listener : methodExits) {
			listener.handle(ctx);
		}
	}

	/**
	 * Registers instruction interceptor.
	 *
	 * @param interceptor Interceptor to register.
	 */
	public void registerInstructionInterceptor(InstructionInterceptor interceptor) {
		instructionInterceptors.add(interceptor);
	}

	/**
	 * Removes instruction interceptor.
	 *
	 * @param interceptor Interceptor to remove.
	 */
	public void removeInstructionInterceptor(InstructionInterceptor interceptor) {
		instructionInterceptors.remove(interceptor);
	}

	/**
	 * Registers a method enter listener.
	 *
	 * @param listener Listener to register.
	 */
	public void registerMethodEnterListener(MethodEnterListener listener) {
		methodEnters.add(listener);
	}

	/**
	 * Registers a method exit listener.
	 *
	 * @param listener Listener to register.
	 */
	public void registerMethodExitListener(MethodExitListener listener) {
		methodExits.add(listener);
	}

	/**
	 * Removes a method enter listener.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeMethodEnterListener(MethodEnterListener listener) {
		methodEnters.remove(listener);
	}

	/**
	 * Removes a method exit listener.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeMethodExitListener(MethodExitListener listener) {
		methodExits.remove(listener);
	}

	/**
	 * @return Instruction interceptors.
	 */
	public List<InstructionInterceptor> getInstructionInterceptors() {
		return instructionInterceptorsView;
	}

	/**
	 * @return Method enter listeners.
	 */
	public List<MethodEnterListener> getMethodEnterListeners() {
		return methodEntersView;
	}

	/**
	 * @return Method exit listeners.
	 */
	public List<MethodExitListener> getMethodExitListeners() {
		return methodExitsView;
	}

	/**
	 * @param linkageErrorHandler Handler of linkage errors.
	 *                            Consumes an execution context of the method that is unlinked.
	 */
	public void setLinkageErrorHandler(Consumer<ExecutionContext<?>> linkageErrorHandler) {
		this.linkageErrorHandler = linkageErrorHandler;
	}
	/**
	 * @param abstractMethodHandler Handler of abstract method errors.
	 *                              Consumes an execution context of the method that is abstract.
	 */
	public void setAbstractMethodHandler(Consumer<ExecutionContext<?>> abstractMethodHandler) {
		this.abstractMethodHandler = abstractMethodHandler;
	}

	/**
	 * @param ctx Context of the native method not linked.
	 */
	public void handleLinkageError(ExecutionContext<?> ctx) {
		linkageErrorHandler.accept(ctx);
	}

	/**
	 * @param ctx Context of the abstract method that was attempted to be invoked.
	 */
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
