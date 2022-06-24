package dev.xdark.ssvm.api;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.asm.VirtualInsnNode;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.InterpretedInvoker;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public final class VMInterface {

	private static final MethodInvoker FALLBACK_INVOKER = new InterpretedInvoker();
	private static final int MAX_INSNS = 1024;
	private final InstructionProcessor[] processors = new InstructionProcessor[MAX_INSNS];
	private final Map<JavaMethod, MethodInvoker> invokerMap = new HashMap<>();
	private final List<MethodInvocation> globalEnter = new ArrayList<>();
	private final List<MethodInvocation> globalExit = new ArrayList<>();
	private final Map<JavaMethod, MethodInvocation> methodEnter = new HashMap<>();
	private final Map<JavaMethod, MethodInvocation> methodExit = new HashMap<>();
	private final List<InstructionInterceptor> interceptors = new ArrayList<>();
	private final List<InstructionInterceptor> interceptorsView = Collections.unmodifiableList(interceptors);

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
		return processors[getOpcode(insn)];
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
		return invokerMap.getOrDefault(method, FALLBACK_INVOKER);
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
	 * @see Modifier#ACC_JIT
	 */
	public boolean setInvoker(InstanceJavaClass jc, String name, String desc, MethodInvoker invoker) {
		JavaMethod method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		setInvoker(method, invoker);
		return true;
	}

	/**
	 * Registers global method enter hook.
	 *
	 * @param invocation Hook to register.
	 */
	public void registerMethodEnter(MethodInvocation invocation) {
		globalEnter.add(invocation);
	}

	/**
	 * Registers global method enter hook.
	 *
	 * @param invocation Hook to register.
	 */
	public void registerMethodExit(MethodInvocation invocation) {
		globalExit.add(invocation);
	}

	/**
	 * Registers global method exit hook.
	 *
	 * @param invocation Hook to unregister.
	 */
	public void removeMethodEnter(MethodInvocation invocation) {
		globalEnter.remove(invocation);
	}

	/**
	 * Registers global method exit hook.
	 *
	 * @param invocation Hook to unregister.
	 */
	public void removeMethodExit(MethodInvocation invocation) {
		globalExit.remove(invocation);
	}

	/**
	 * Registers specific method enter hook.
	 *
	 * @param call       Method being hooked.
	 * @param invocation Hook to register.
	 */
	public void registerMethodEnter(JavaMethod call, MethodInvocation invocation) {
		methodEnter.put(call, invocation);
	}

	/**
	 * Registers specific method enter hook.
	 *
	 * @param jc         Instance class.
	 * @param name       Name of the method.
	 * @param desc       Descriptor of the method.
	 * @param invocation Hook to register.
	 * @return {@code true} if method was registered,
	 * {@code false} otherwise.
	 */
	public boolean registerMethodEnter(InstanceJavaClass jc, String name, String desc, MethodInvocation invocation) {
		JavaMethod method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		methodEnter.put(method, invocation);
		return true;
	}

	/**
	 * Registers specific method exit hook.
	 *
	 * @param method     Method being hooked.
	 * @param invocation Hook to unregister.
	 */
	public void removeMethodEnter(JavaMethod method, MethodInvocation invocation) {
		methodExit.put(Objects.requireNonNull(method, "method"), invocation);
	}

	/**
	 * Registers specific method exit hook.
	 *
	 * @param method     Method being hooked.
	 * @param invocation Hook to register.
	 */
	public void registerMethodExit(JavaMethod method, MethodInvocation invocation) {
		methodExit.put(Objects.requireNonNull(method, "method"), invocation);
	}

	/**
	 * Registers specific method exit hook.
	 *
	 * @param jc         Instance class.
	 * @param name       Name of the method.
	 * @param desc       Descriptor of the method.
	 * @param invocation Hook to register.
	 * @return {@code true} if method was registered,
	 * {@code false} otherwise.
	 */
	public boolean registerMethodExit(InstanceJavaClass jc, String name, String desc, MethodInvocation invocation) {
		JavaMethod method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		methodExit.put(method, invocation);
		return true;
	}

	/**
	 * Registers instruction interceptor.
	 *
	 * @param interceptor Interceptor to register.
	 */
	public void registerInstructionInterceptor(InstructionInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	/**
	 * Removes instruction interceptor.
	 *
	 * @param interceptor Interceptor to remove.
	 */
	public void removeInstructionInterceptor(InstructionInterceptor interceptor) {
		interceptors.remove(interceptor);
	}

	/**
	 * Returns stream of invocation hooks.
	 *
	 * @param call  Call info.
	 * @param enter True if called upon method entering, method exit otherwise.
	 * @return iterable of invocation hooks.
	 */
	public Iterable<MethodInvocation> getInvocationHooks(JavaMethod call, boolean enter) {
		Map<JavaMethod, MethodInvocation> map;
		List<MethodInvocation> list;
		if (enter) {
			map = methodEnter;
			list = globalEnter;
		} else {
			map = methodExit;
			list = globalExit;
		}
		MethodInvocation invocation = map.get(call);
		if (invocation == null) {
			return list;
		}
		if (list.isEmpty()) {
			return () -> new SingletonIterator(invocation);
		}
		return () -> new InvocationIterator(invocation, list);
	}

	/**
	 * @return Instruction interceptors.
	 */
	public List<InstructionInterceptor> getInterceptors() {
		return interceptorsView;
	}

	private static int getOpcode(AbstractInsnNode node) {
		if (node instanceof VirtualInsnNode) {
			return ((VirtualInsnNode) node).getVirtualOpcode();
		}
		return node.getOpcode();
	}

	private static final class InvocationIterator implements Iterator<MethodInvocation> {

		private MethodInvocation first;
		private final List<MethodInvocation> rest;
		private int index;

		private InvocationIterator(MethodInvocation first, List<MethodInvocation> rest) {
			this.first = first;
			this.rest = rest;
		}

		@Override
		public boolean hasNext() {
			return first != null || index < rest.size();
		}

		@Override
		public MethodInvocation next() {
			MethodInvocation first = this.first;
			if (first != null) {
				this.first = null;
				return first;
			}
			return rest.get(index++);
		}
	}

	private static final class SingletonIterator implements Iterator<MethodInvocation> {

		private MethodInvocation invocation;

		private SingletonIterator(MethodInvocation invocation) {
			this.invocation = invocation;
		}

		@Override
		public boolean hasNext() {
			return invocation != null;
		}

		@Override
		public MethodInvocation next() {
			MethodInvocation invocation = this.invocation;
			if (invocation == null) {
				throw new NoSuchElementException();
			}
			this.invocation = null;
			return invocation;
		}
	}
}
