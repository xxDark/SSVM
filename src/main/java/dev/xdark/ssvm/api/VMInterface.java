package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public final class VMInterface {

	private static final int MAX_INSNS = 1024;
	private static final int JVM_INSNS = Opcodes.IFNONNULL;
	private final InstructionProcessor[] processors = new InstructionProcessor[MAX_INSNS];
	private final Map<JavaMethod, MethodInvoker> invokerMap = new HashMap<>();
	private final List<MethodInvocation> globalEnter = new ArrayList<>();
	private final List<MethodInvocation> globalExit = new ArrayList<>();
	private final Map<JavaMethod, MethodInvocation> methodEnter = new HashMap<>();
	private final Map<JavaMethod, MethodInvocation> methodExit = new HashMap<>();

	/**
	 * Gets an instruction processor.
	 *
	 * @param <I>
	 * 		Type of the instruction.
	 * @param insn
	 * 		Instruction to get processor for.
	 */
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return processors[insn.getOpcode()];
	}

	/**
	 * Gets an instruction processor by opcode.
	 *
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public InstructionProcessor<?> getProcessor(int opcode) {
		return processors[opcode];
	}

	/**
	 * Sets an instruction processor.
	 *
	 * @param opcode
	 * 		Opcode of the instruction.
	 * @param processor
	 * 		Processor of the opcode.
	 */
	public void setProcessor(int opcode, InstructionProcessor<?> processor) {
		processors[opcode] = processor;
	}

	/**
	 * Returns method invoker based off call info.
	 *
	 * @param call
	 * 		Call info.
	 *
	 * @return method invoker.
	 */
	public MethodInvoker getInvoker(JavaMethod call) {
		return invokerMap.get(call);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param call
	 * 		Call information.
	 * @param invoker
	 * 		Method invoker.
	 */
	public void setInvoker(JavaMethod call, MethodInvoker invoker) {
		invokerMap.put(call, invoker);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param jc
	 * 		Instance class.
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 * @param invoker
	 * 		Method invoker.
	 *
	 * @return {@code true} if method was registeed,
	 * {@code false} otherwise.
	 */
	public boolean setInvoker(InstanceJavaClass jc, String name, String desc, MethodInvoker invoker) {
		var method = jc.getMethod(name, desc);
		if (method == null) {
			return false;
		}
		setInvoker(method, invoker);
		return true;
	}

	/**
	 * Registers global method enter hook.
	 *
	 * @param invocation
	 * 		Hook to register.
	 */
	public void registerMethodEnter(MethodInvocation invocation) {
		globalEnter.add(invocation);
	}

	/**
	 * Registers global method exit hook.
	 *
	 * @param invocation
	 * 		Hook to unregister.
	 */
	public void removeMethodEnter(MethodInvocation invocation) {
		globalEnter.remove(invocation);
	}

	/**
	 * Registers specific method enter hook.
	 *
	 * @param invocation
	 * 		Hook to register.
	 */
	public void registerMethodEnter(JavaMethod call, MethodInvocation invocation) {
		methodEnter.put(call, invocation);
	}

	/**
	 * Registers specific method exit hook.
	 *
	 * @param invocation
	 * 		Hook to unregister.
	 */
	public void removeMethodEnter(JavaMethod call, MethodInvocation invocation) {
		methodExit.put(call, invocation);
	}

	/**
	 * Returns stream of invocation hooks.
	 *
	 * @param call
	 * 		Call info.
	 * @param enter
	 * 		True if called upon method entering, method exit otherwise.
	 *
	 * @return stream of invocation hooks.
	 */
	public Stream<MethodInvocation> getInvocationHooks(JavaMethod call, boolean enter) {
		Map<JavaMethod, MethodInvocation> map;
		List<MethodInvocation> list;
		if (enter) {
			map = methodEnter;
			list = globalEnter;
		} else {
			map = methodExit;
			list = globalExit;
		}
		var invocation = map.get(call);
		var stream = list.stream();
		if (invocation == null) return stream;
		return Stream.concat(Stream.of(invocation), stream);
	}
}
