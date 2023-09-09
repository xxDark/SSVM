package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Delegating VM interface implementation to allow users to swap out one or two items from a given type, without
 * being tied to the same initialization process of that type.
 *
 * @author Matt Coley
 */
public class DelegatingVMInterface implements VMInterface {
	private final VMInterface delegate;

	public DelegatingVMInterface(VMInterface delegate) {
		if (delegate == null)
			throw new IllegalArgumentException("Delegate cannot be null");
		this.delegate = delegate;
	}

	@Override
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return delegate.getProcessor(insn);
	}

	@Override
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(int opcode) {
		return delegate.getProcessor(opcode);
	}

	@Override
	public void setProcessor(int opcode, InstructionProcessor<?> processor) {
		delegate.setProcessor(opcode, processor);
	}

	@Override
	public MethodInvoker getInvoker(JavaMethod method) {
		return delegate.getInvoker(method);
	}

	@Override
	public void setInvoker(JavaMethod method, MethodInvoker invoker) {
		delegate.setInvoker(method, invoker);
	}

	@Override
	public boolean setInvoker(InstanceClass jc, String name, String desc, MethodInvoker invoker) {
		return delegate.setInvoker(jc, name, desc, invoker);
	}

	@Override
	public void onMethodEnter(ExecutionContext<?> ctx) {
		delegate.onMethodEnter(ctx);
	}

	@Override
	public void onMethodExit(ExecutionContext<?> ctx) {
		delegate.onMethodExit(ctx);
	}

	@Override
	public void registerInstructionInterceptor(InstructionInterceptor interceptor) {
		delegate.registerInstructionInterceptor(interceptor);
	}

	@Override
	public void removeInstructionInterceptor(InstructionInterceptor interceptor) {
		delegate.removeInstructionInterceptor(interceptor);
	}

	@Override
	public void registerMethodEnterListener(MethodEnterListener listener) {
		delegate.registerMethodEnterListener(listener);
	}

	@Override
	public void registerMethodExitListener(MethodExitListener listener) {
		delegate.registerMethodExitListener(listener);
	}

	@Override
	public void removeMethodEnterListener(MethodEnterListener listener) {
		delegate.removeMethodEnterListener(listener);
	}

	@Override
	public void removeMethodExitListener(MethodExitListener listener) {
		delegate.removeMethodExitListener(listener);
	}

	@Override
	public List<InstructionInterceptor> getInstructionInterceptors() {
		return delegate.getInstructionInterceptors();
	}

	@Override
	public List<MethodEnterListener> getMethodEnterListeners() {
		return delegate.getMethodEnterListeners();
	}

	@Override
	public List<MethodExitListener> getMethodExitListeners() {
		return delegate.getMethodExitListeners();
	}

	@Override
	public void setLinkageErrorHandler(Consumer<ExecutionContext<?>> linkageErrorHandler) {
		delegate.setLinkageErrorHandler(linkageErrorHandler);
	}

	@Override
	public void setAbstractMethodHandler(Consumer<ExecutionContext<?>> abstractMethodHandler) {
		delegate.setAbstractMethodHandler(abstractMethodHandler);
	}

	@Override
	public void setMaxIterationsHandler(Consumer<ExecutionContext<?>> maxIterationsHandler) {
		delegate.setMaxIterationsHandler(maxIterationsHandler);
	}

	@Override
	public void handleLinkageError(ExecutionContext<?> ctx) {
		delegate.handleLinkageError(ctx);
	}

	@Override
	public void handleAbstractMethodError(ExecutionContext<?> ctx) {
		delegate.handleAbstractMethodError(ctx);
	}

	@Override
	public void handleMaxInterations(ExecutionContext<?> ctx) {
		delegate.handleMaxInterations(ctx);
	}

	@Override
	public VMInterface copy() {
		return delegate.copy();
	}
}