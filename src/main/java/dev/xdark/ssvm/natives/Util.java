package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
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
			val t = types[i];
			val arg = args[i];
			if (LONG.equals(t)) args[i] = helper.boxLong(arg);
			else if (DOUBLE.equals(t)) args[i] = helper.boxDouble(arg);
			else if (INT.equals(t)) args[i] = helper.boxInt(arg);
			else if (FLOAT.equals(t)) args[i] = helper.boxFloat(arg);
			else if (CHAR.equals(t)) args[i] = helper.boxChar(arg);
			else if (SHORT.equals(t)) args[i] = helper.boxShort(arg);
			else if (BYTE.equals(t)) args[i] = helper.boxByte(arg);
			else if (BOOLEAN.equals(t)) args[i] = helper.boxBoolean(arg);
			else if (t.getSort() == Type.OBJECT) {
				if (arg instanceof DoubleValue) args[i] = helper.boxDouble(arg);
				else if (arg instanceof LongValue) args[i] = helper.boxLong(arg);
				else if (arg instanceof IntValue) args[i] = helper.boxInt(arg);
				else if (arg instanceof FloatValue) args[i] = helper.boxFloat(arg);
			}
		}
	}
}
