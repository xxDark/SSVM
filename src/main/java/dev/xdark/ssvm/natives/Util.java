package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.io.AnnotationWriter;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
	Value[] convertReflectionArgs(VirtualMachine vm, ObjectValue loader, Type[] argTypes, ArrayValue array) {
		val helper = vm.getHelper();
		int total = 0;
		for (val arg : argTypes) {
			total += arg.getSize();
		}
		val result = new Value[total];
		int x = 0;
		for (int i = 0; i < argTypes.length; i++) {
			val originalClass = helper.findClass(loader, argTypes[i].getInternalName(), true);
			val value = (ObjectValue) array.getValue(i);
			if (value.isNull() || !originalClass.isPrimitive()) {
				result[x++] = value;
			} else {
				if ((result[x++] = helper.unboxGeneric(value, originalClass)).isWide()) {
					result[x++] = TopValue.INSTANCE;
				}
			}
		}
		return result;
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

	/**
	 * Converts {@link AnnotationsAttribute} back
	 * to it's raw form.
	 *
	 * @param attribute
	 * 		Attribute to convert.
	 *
	 * @return raw bytes.
	 */
	byte[] toBytes(AnnotationsAttribute attribute) {
		val baos = new ByteArrayOutputStream();
		val writer = new AnnotationWriter(new DataOutputStream(baos));
		try {
			writer.writeAnnotations(attribute);
		} catch (IOException ex) {
			throw new RuntimeException(ex); // Should never happen.
		}
		return baos.toByteArray();
	}

	/**
	 * Converts {@link ParameterAnnotationsAttribute} back
	 * to it's raw form.
	 *
	 * @param attribute
	 * 		Attribute to convert.
	 *
	 * @return raw bytes.
	 */
	byte[] toBytes(ParameterAnnotationsAttribute attribute) {
		val baos = new ByteArrayOutputStream();
		val writer = new AnnotationWriter(new DataOutputStream(baos));
		try {
			writer.writeParameterAnnotations(attribute);
		} catch (IOException ex) {
			throw new RuntimeException(ex); // Should never happen.
		}
		return baos.toByteArray();
	}

	/**
	 * Converts {@link AnnotationDefaultAttribute} back
	 * to it's raw form.
	 *
	 * @param attribute
	 * 		Attribute to convert.
	 *
	 * @return raw bytes.
	 */
	byte[] toBytes(AnnotationDefaultAttribute attribute) {
		val baos = new ByteArrayOutputStream();
		val writer = new AnnotationWriter(new DataOutputStream(baos));
		try {
			writer.writeAnnotationDefault(attribute);
		} catch (IOException ex) {
			throw new RuntimeException(ex); // Should never happen.
		}
		return baos.toByteArray();
	}
}
