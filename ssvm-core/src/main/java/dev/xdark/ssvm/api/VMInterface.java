package dev.xdark.ssvm.api;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.InterpretedInvoker;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public final class VMInterface {

	private static final int MAX_INSNS = 1024;
	private final InstructionProcessor[] processors = new InstructionProcessor[MAX_INSNS];
	private final Map<JavaMethod, MethodInvoker> invokerMap = new HashMap<>();
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
	 * @return Instruction interceptors.
	 */
	public List<InstructionInterceptor> getInterceptors() {
		return interceptorsView;
	}
}
