package dev.xdark.ssvm.api;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Interpreter;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public interface VMInterface {
	/**
	 * Gets an instruction processor.
	 *
	 * @param <I>  Type of the instruction.
	 * @param insn Instruction to get processor for.
	 * @return instruction processor.
	 */
	<I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn);

	/**
	 * Gets an instruction processor by opcode.
	 *
	 * @param opcode Instruction opcode.
	 * @param <I>    Instruction type.
	 * @return instruction processor.
	 */
	<I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(int opcode);

	/**
	 * Sets an instruction processor.
	 *
	 * @param opcode    Opcode of the instruction.
	 * @param processor Processor of the opcode.
	 */
	void setProcessor(int opcode, InstructionProcessor<?> processor);

	/**
	 * Returns method invoker based off a method.
	 *
	 * @param method method to search invoker for.
	 * @return method invoker.
	 */
	MethodInvoker getInvoker(JavaMethod method);

	/**
	 * Sets an invoker for the method.
	 *
	 * @param method  Method to set invoker for.
	 * @param invoker Method invoker.
	 */
	void setInvoker(JavaMethod method, MethodInvoker invoker);

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
	boolean setInvoker(InstanceClass jc, String name, String desc, MethodInvoker invoker);

	/**
	 * Called by {@link Interpreter} when a method starts execution.
	 *
	 * @param ctx Context of the method being executed.
	 */
	void onMethodEnter(ExecutionContext<?> ctx);

	/**
	 * Called by {@link Interpreter} when a method finishes execution.
	 *
	 * @param ctx Context of the method being executed.
	 */
	void onMethodExit(ExecutionContext<?> ctx);

	/**
	 * Registers instruction interceptor.
	 *
	 * @param interceptor Interceptor to register.
	 */
	void registerInstructionInterceptor(InstructionInterceptor interceptor);

	/**
	 * Removes instruction interceptor.
	 *
	 * @param interceptor Interceptor to remove.
	 */
	void removeInstructionInterceptor(InstructionInterceptor interceptor);

	/**
	 * Registers a method enter listener.
	 *
	 * @param listener Listener to register.
	 */
	void registerMethodEnterListener(MethodEnterListener listener);

	/**
	 * Registers a method exit listener.
	 *
	 * @param listener Listener to register.
	 */
	void registerMethodExitListener(MethodExitListener listener);

	/**
	 * Removes a method enter listener.
	 *
	 * @param listener Listener to remove.
	 */
	void removeMethodEnterListener(MethodEnterListener listener);

	/**
	 * Removes a method exit listener.
	 *
	 * @param listener Listener to remove.
	 */
	void removeMethodExitListener(MethodExitListener listener);

	/**
	 * @return Instruction interceptors.
	 */
	List<InstructionInterceptor> getInstructionInterceptors();

	/**
	 * @return Method enter listeners.
	 */
	List<MethodEnterListener> getMethodEnterListeners();

	/**
	 * @return Method exit listeners.
	 */
	List<MethodExitListener> getMethodExitListeners();

	/**
	 * @param linkageErrorHandler Handler of linkage errors.
	 *                            Consumes an execution context of the method that is unlinked.
	 */
	void setLinkageErrorHandler(Consumer<ExecutionContext<?>> linkageErrorHandler);

	/**
	 * @param abstractMethodHandler Handler of abstract method errors.
	 *                              Consumes an execution context of the method that is abstract.
	 */
	void setAbstractMethodHandler(Consumer<ExecutionContext<?>> abstractMethodHandler);

	/**
	 * @param ctx Context of the native method not linked.
	 */
	void handleLinkageError(ExecutionContext<?> ctx);

	/**
	 * @param ctx Context of the abstract method that was attempted to be invoked.
	 */
	void handleAbstractMethodError(ExecutionContext<?> ctx);
}