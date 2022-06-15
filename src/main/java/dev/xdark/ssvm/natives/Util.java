package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
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

	/**
	 * Converts array of values back to their original
	 * values.
	 * Used for reflection calls.
	 *
	 * @param vm       VM instance.
	 * @param loader   Class loader to use.
	 * @param argTypes Original types.
	 * @param array    Array to convert.
	 * @return original values array.
	 */
	Value[] convertReflectionArgs(VirtualMachine vm, ObjectValue loader, Type[] argTypes, ArrayValue array) {
		VMHelper helper = vm.getHelper();
		int total = 0;
		for (Type arg : argTypes) {
			total += arg.getSize();
		}
		Value[] result = new Value[total];
		int x = 0;
		for (int i = 0; i < argTypes.length; i++) {
			JavaClass originalClass = helper.findClass(loader, argTypes[i].getInternalName(), true);
			ObjectValue value = array.getValue(i);
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
	 * Converts {@link AnnotationsAttribute} back
	 * to it's raw form.
	 *
	 * @param attribute Attribute to convert.
	 * @return raw bytes.
	 */
	byte[] toBytes(AnnotationsAttribute attribute) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AnnotationWriter writer = new AnnotationWriter(new DataOutputStream(baos));
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
	 * @param attribute Attribute to convert.
	 * @return raw bytes.
	 */
	byte[] toBytes(ParameterAnnotationsAttribute attribute) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AnnotationWriter writer = new AnnotationWriter(new DataOutputStream(baos));
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
	 * @param attribute Attribute to convert.
	 * @return raw bytes.
	 */
	byte[] toBytes(AnnotationDefaultAttribute attribute) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AnnotationWriter writer = new AnnotationWriter(new DataOutputStream(baos));
		try {
			writer.writeAnnotationDefault(attribute);
		} catch (IOException ex) {
			throw new RuntimeException(ex); // Should never happen.
		}
		return baos.toByteArray();
	}
}
