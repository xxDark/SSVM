package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
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

	/**
	 * Converts array of values back to their original
	 * values.
	 * Used for reflection calls.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param loader
	 * 		Class laoder to use.
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

}
