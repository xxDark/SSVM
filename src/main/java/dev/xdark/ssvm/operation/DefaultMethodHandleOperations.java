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
	private final VMOperations ops;

	@Override
	public InstanceValue methodType(JavaClass returnType, ArrayValue parameterTypes) {
		JavaMethod method = linkResolver.resolveStaticMethod(symbols.java_lang_invoke_MethodHandleNatives(), "findMethodHandleType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;");
		Locals locals = threadManager.currentThreadStorage().newLocals(method);
		locals.setReference(0, returnType.getOop());
		locals.setReference(1, parameterTypes);
		return (InstanceValue) ops.invokeReference(method, locals);
	}

	@Override
	public InstanceValue methodType(JavaClass returnType, JavaClass[] parameterTypes) {
		ArrayValue array = ops.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			array.setReference(i, parameterTypes[i].getOop());
		}
		return methodType(returnType, array);
	}

	@Override
	public InstanceValue methodType(ObjectValue classLoader, Type returnType, Type[] parameterTypes) {
		VMOperations ops = this.ops;
		JavaClass rt = ops.findClass(classLoader, returnType, false);
		ArrayValue array = ops.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			JavaClass argument = ops.findClass(classLoader, parameterTypes[i], false);
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
		VMOperations ops = this.ops;
		ObjectValue classLoader = returnType.getClassLoader();
		ArrayValue array = ops.allocateArray(symbols.java_lang_Class(), parameterTypes.length);
		for (int i = 0, j = parameterTypes.length;  i< j; i++) {
			JavaClass argument = ops.findClass(classLoader, parameterTypes[i], false);
			array.setReference(i, argument.getOop());
		}
		return methodType(returnType, array);
	}

	@Override
	public InstanceValue linkMethodHandleConstant(InstanceClass caller, Handle handle) {
		VMOperations ops = this.ops;
		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod link = linkResolver.resolveStaticMethod(natives, "linkMethodHandleConstant", "(Ljava/lang/Class;ILjava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;");
		Locals locals = threadManager.currentThreadStorage().newLocals(link);
		locals.setReference(0, caller.getOop());
		locals.setInt(1, handle.getTag());
		ObjectValue cl = caller.getClassLoader();
		locals.setReference(2, ops.findClass(cl, handle.getOwner(), false).getOop());
		locals.setReference(3, ops.newUtf8(handle.getName()));
		locals.setReference(4, methodType(cl, Type.getMethodType(handle.getDesc())));
		return (InstanceValue) ops.invokeReference(link, locals);
	}
}
