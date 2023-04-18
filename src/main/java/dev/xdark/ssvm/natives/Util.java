package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
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

	void copyReflectionArguments(VirtualMachine vm, JavaClass[] argTypes, ArrayValue array, Locals locals, int offset) {
		VMHelper helper = vm.getHelper();
		for (int i = 0; i < argTypes.length; i++) {
			JavaClass type = argTypes[i];
			int sort = type.getSort();
			ObjectValue value = array.getReference(i);
			if (sort < Type.ARRAY) {
				switch (sort) {
					case Type.BOOLEAN:
						locals.setInt(offset++, helper.unboxBoolean(value) ? 1 : 0);
						break;
					case Type.CHAR:
						locals.setInt(offset++, helper.unboxChar(value));
						break;
					case Type.BYTE:
					case Type.SHORT:
					case Type.INT:
						locals.setInt(offset++, helper.unboxInt(value));
						break;
					case Type.FLOAT:
						locals.setFloat(offset++, helper.unboxFloat(value));
						break;
					case Type.LONG:
						locals.setLong(offset, helper.unboxLong(value));
						offset += 2;
						break;
					case Type.DOUBLE:
						locals.setDouble(offset, helper.unboxDouble(value));
						offset += 2;
						break;
					default:
						throw new PanicException("Bad sort: " + sort);
				}
			} else {
				locals.setReference(offset++, value);
			}
		}
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
