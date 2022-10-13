package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultMethodHandleOperations implements MethodHandleOperations {

	private final Symbols symbols;
	private final ThreadManager threadManager;
	private final LinkResolver linkResolver;
	private final ClassOperations classOperations;
	private final InvocationOperations invocationOperations;
	private final AllocationOperations allocationOperations;
	private final StringOperations stringOperations;

	@Override
	public InstanceValue methodType(JavaClass returnType, ArrayValue parameterTypes) {
		JavaMethod method = linkResolver.resolveStaticMethod(symbols.java_lang_invoke_MethodHandleNatives(), "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;");
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setReference(0, returnType.getOop());
		locals.setReference(1, parameterTypes);
		return (InstanceValue) invocationOperations.invokeReference(method, locals);
	}

	@Override
	public InstanceValue methodType(JavaClass returnType, JavaClass[] parameterTypes) {
		ArrayValue array = allocationOperations.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			array.setReference(i, parameterTypes[i].getOop());
		}
		return methodType(returnType, array);
	}

	@Override
	public InstanceValue methodType(ObjectValue classLoader, Type returnType, Type[] parameterTypes) {
		ClassOperations classOperations = this.classOperations;
		JavaClass rt = classOperations.findClass(classLoader, returnType, false);
		ArrayValue array = allocationOperations.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			JavaClass argument = classOperations.findClass(classLoader, parameterTypes[i], false);
			array.setReference(i, argument.getOop());
		}
		return methodType(rt, array);
	}

	@Override
	public InstanceValue methodType(ObjectValue classLoader, Type methodType) {
		Assertions.check(methodType.getSort() == Type.METHOD, "not a method type");
		return methodType(classLoader, methodType.getReturnType(), methodType.getArgumentTypes());
	}

	@Override
	public InstanceValue methodType(JavaClass returnType, Type[] parameterTypes) {
		ClassOperations classOperations = this.classOperations;
		ObjectValue classLoader = returnType.getClassLoader();
		ArrayValue array = allocationOperations.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			JavaClass argument = classOperations.findClass(classLoader, parameterTypes[i], false);
			array.setReference(i, argument.getOop());
		}
		return methodType(returnType, array);
	}

	@Override
	public InstanceValue linkMethodHandleConstant(InstanceClass caller, Handle handle) {
		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod link = linkResolver.resolveStaticMethod(natives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;");
		Locals locals = threadManager.currentThreadStorage().newLocals(link);
		locals.setReference(0, caller.getOop());
		locals.setInt(1, handle.getTag());
		locals.setReference(2, classOperations.findClass(caller.getClassLoader(), handle.getOwner(), false).getOop());
		locals.setReference(3, stringOperations.newUtf8(handle.getName()));
		locals.setReference(4, methodType(caller.getClassLoader(), Type.getMethodType(handle.getDesc())));
		return (InstanceValue) invocationOperations.invokeReference(link, locals);
	}
}
