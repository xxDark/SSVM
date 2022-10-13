package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultInvokeDynamicOperations implements InvokeDynamicOperations {

	private final Symbols symbols;
	private final ThreadManager threadManager;
	private final StringPool stringPool;
	private final LinkResolver linkResolver;
	private final ExceptionOperations exceptionOperations;
	private final AllocationOperations allocationOperations;
	private final MethodHandleOperations methodHandleOperations;
	private final InvocationOperations invocationOperations;
	private final VerificationOperations verificationOperations;

	@Override
	public InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceClass caller) {
		Symbols symbols = this.symbols;
		Handle bootstrap = insn.bsm;
		if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
			exceptionOperations.throwException(symbols.java_lang_IllegalStateException(), "Bootstrap tag is not static");
		}
		InstanceValue linker = methodHandleOperations.linkMethodHandleConstant(caller, bootstrap);

		Object[] $bsmArgs = insn.bsmArgs;
		ArrayValue bsmArgs = allocationOperations.allocateArray(symbols.java_lang_Object(), $bsmArgs.length);
		for (int i = 0; i < $bsmArgs.length; i++) {
			bsmArgs.setReference(i, forInvokeDynamicCall($bsmArgs[i]));
		}

		StringPool stringPool = this.stringPool;
		ArrayValue appendix = allocationOperations.allocateArray(symbols.java_lang_Object(), 1);
		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod method = natives.getMethod("linkCallSite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
		Locals linkArgs;
		if (method == null) {
			// Bogus cp index entry which was removed
			// shortly after it was added, shaking
			method = natives.getMethod("linkCallSite", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setInt(1, 0);
			linkArgs.setReference(2, linker);
			linkArgs.setReference(3, stringPool.intern(insn.name));
			linkArgs.setReference(4, methodHandleOperations.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(5, bsmArgs);
			linkArgs.setReference(6, appendix);
		} else {
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setReference(1, linker);
			linkArgs.setReference(2, stringPool.intern(insn.name));
			linkArgs.setReference(3, methodHandleOperations.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(4, bsmArgs);
			linkArgs.setReference(5, appendix);
		}

		invocationOperations.invokeVoid(method, linkArgs);
		return (InstanceValue) appendix.getReference(0);
	}

	@Override
	public void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink) {
		ThreadStorage ts = threadManager.currentOsThread().getStorage();
		if (symbols.java_lang_invoke_CallSite().isAssignableFrom(handle.getJavaClass())) {
			// See linkCallSiteImpl
			JavaMethod getTarget = linkResolver.resolveVirtualMethod(handle, "getTarget", "()Ljava/lang/invoke/MethodHandle;");
			Locals locals = ts.newLocals(getTarget);
			locals.setReference(0, handle);
			handle = verificationOperations.checkNotNull(invocationOperations.invokeReference(getTarget, locals));
		}
		JavaMethod invokeExact = linkResolver.resolveVirtualMethod(handle, "invokeExact", desc);
		Locals locals = ts.newLocals(invokeExact);
		locals.setReference(0, handle);
		stack.sinkInto(locals, 1, invokeExact.getMaxArgs() - 1);
		invocationOperations.invoke(invokeExact, locals, sink);
	}
}
