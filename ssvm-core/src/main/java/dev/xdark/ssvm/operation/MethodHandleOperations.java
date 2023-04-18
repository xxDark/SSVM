package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * VM method handle operations.
 *
 * @author xDark
 */
public interface MethodHandleOperations {

	/**
	 * Makes new method type.
	 *
	 * @param returnType     Return type.
	 * @param parameterTypes Parameter types.
	 * @return New method type.
	 */
	InstanceValue methodType(JavaClass returnType, ArrayValue parameterTypes);

	/**
	 * Makes new method type.
	 *
	 * @param returnType     Return type.
	 * @param parameterTypes Parameter types.
	 * @return New method type.
	 */
	InstanceValue methodType(JavaClass returnType, JavaClass[] parameterTypes);

	/**
	 * Makes new method type.
	 *
	 * @param klass          Host class.
	 * @param returnType     Return type.
	 * @param parameterTypes Parameter types.
	 * @return New method type.
	 */
	InstanceValue methodType(JavaClass klass, Type returnType, Type[] parameterTypes);

	/**
	 * Makes new method type.
	 *
	 * @param classLoader    Class loader.
	 * @param returnType     Return type.
	 * @param parameterTypes Parameter types.
	 * @return New method type.
	 */
	InstanceValue methodType(ObjectValue classLoader, Type returnType, Type[] parameterTypes);

	/**
	 * Makes new method type.
	 *
	 * @param klass Host klass.
	 * @param methodType  ASM method type.
	 * @return New method type.
	 */
	InstanceValue methodType(JavaClass klass, Type methodType);

	/**
	 * Makes new method type.
	 *
	 * @param classLoader Class loader.
	 * @param methodType  ASM method type.
	 * @return New method type.
	 */
	InstanceValue methodType(ObjectValue classLoader, Type methodType);

	/**
	 * Makes new method type.
	 *
	 * @param returnType     Return type.
	 * @param parameterTypes Parameter types.
	 * @return New method type.
	 */
	InstanceValue methodType(JavaClass returnType, Type[] parameterTypes);

	/**
	 * Links method handle.
	 *
	 * @param caller Caller.
	 * @param handle Handle to link.
	 * @return Linked method handle.
	 */
	InstanceValue linkMethodHandleConstant(InstanceClass caller, Handle handle);
}
