package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.Type;

/**
 * Package-private util for native implementations.
 *
 * @author xDark
 */
@UtilityClass
final class Util {

	private final Type LONG = Type.getType(Long.class);
	private final Type DOUBLE = Type.getType(Double.class);
	private final Type INT = Type.getType(Integer.class);
	private final Type FLOAT = Type.getType(Float.class);
	private final Type CHAR = Type.getType(Character.class);
	private final Type SHORT = Type.getType(Short.class);
	private final Type BOOLEAN = Type.getType(Boolean.class);
	private final Type BYTE = Type.getType(Byte.class);

	/**
	 * Converts array of values back to their original
	 * values.
	 * Used for reflection calls.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param loader
	 * 		Class loader to use.
	 * @param argTypes
	 * 		Original types.
	 * @param array
	 * 		Array to convert.
	 *
	 * @return original values array.
	 */
	Value[] convertReflectionArgs(VirtualMachine vm, Value loader, Type[] argTypes, ArrayValue array) {
		val helper = vm.getHelper();
		val result = new Value[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			val originalClass = helper.findClass(loader, argTypes[i].getInternalName(), true);
			val value = (ObjectValue) array.getValue(i);
			if (value.isNull() || !originalClass.isPrimitive()) {
				result[i] = value;
			} else {
				result[i] = helper.unboxGeneric(value, originalClass);
			}
		}
		return result;
	}

	/**
	 * Converts array of values back to their wrapper types
	 * if needed.
	 * Used for InvokeDynamic.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param types
	 * 		Parameter types.
	 * @param args
	 * 		Arguments.
	 */
	void convertInvokeDynamicArgs(VirtualMachine vm, Type[] types, Value[] args) {
		// TODO figure out why this is even needed
		// either I'm dumb, or there is some magic in the JVM.
		val helper = vm.getHelper();
		for (int i = 0, j = Math.min(args.length, types.length); i < j; i++) {
			args[i] = convertInvokeDynamicArgument(helper, types[i], args[i]);
		}
	}

	/**
	 * Performs argument conversion to its
	 * wrapper/primitive type if needed.
	 *
	 * @param helper
	 * 		VM helper.
	 * @param type
	 * 		Argument type.
	 * @param arg
	 * 		Argument.
	 *
	 * @return boxed/unboxed argument or itself,
	 * if conversion is not needed.
	 */
	Value convertInvokeDynamicArgument(VMHelper helper, Type type, Value arg) {
		if (!(arg instanceof ObjectValue)) {
			if (LONG.equals(type)) return helper.boxLong(arg);
			else if (DOUBLE.equals(type)) return helper.boxDouble(arg);
			else if (INT.equals(type)) return helper.boxInt(arg);
			else if (FLOAT.equals(type)) return helper.boxFloat(arg);
			else if (CHAR.equals(type)) return helper.boxChar(arg);
			else if (SHORT.equals(type)) return helper.boxShort(arg);
			else if (BYTE.equals(type)) return helper.boxByte(arg);
			else if (BOOLEAN.equals(type)) return helper.boxBoolean(arg);
			if (type.getSort() == Type.OBJECT) {
				if (arg instanceof DoubleValue) return helper.boxDouble(arg);
				else if (arg instanceof LongValue) return helper.boxLong(arg);
				else if (arg instanceof IntValue) return helper.boxInt(arg);
				else if (arg instanceof FloatValue) return helper.boxFloat(arg);
			}
		} else if (arg instanceof InstanceValue) {
			val obj = (InstanceValue) arg;
			switch (type.getSort()) {
				case Type.LONG:
					return helper.unboxLong(obj);
				case Type.DOUBLE:
					return helper.unboxDouble(obj);
				case Type.INT:
					return helper.unboxInt(obj);
				case Type.FLOAT:
					return helper.unboxFloat(obj);
				case Type.CHAR:
					return helper.unboxChar(obj);
				case Type.SHORT:
					return helper.unboxShort(obj);
				case Type.BYTE:
					return helper.unboxByte(obj);
				case Type.BOOLEAN:
					return helper.unboxBoolean(obj);
			}
		}
		return arg;
	}
}
