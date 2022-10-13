package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;

/**
 * VM class operations.
 *
 * @author xDark
 */
public interface ClassOperations {

	/**
	 * Links instance class.
	 *
	 * @param instanceClass Class to link.
	 */
	void link(InstanceClass instanceClass);

	/**
	 * Initializes instance class.
	 *
	 * @param instanceClass Class to initialize.
	 */
	void initialize(InstanceClass instanceClass);

	/**
	 * Defines new class in the VM.
	 * Throws VM exception if class failed verification.
	 *
	 * @param classLoader      Class loader.
	 * @param data             Parsed class data.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source.
	 * @param shouldBeLinked   Whether the class should be linked to the class loader.
	 * @return Defined class.
	 */
	InstanceClass defineClass(ObjectValue classLoader, ParsedClassData data, ObjectValue protectionDomain, String source, boolean shouldBeLinked);

	/**
	 * Defines new class in the VM.
	 * Throws VM exception if class failed verification.
	 *
	 * @param classLoader      Class loader.
	 * @param name             Class name.
	 * @param b                Class bytes.
	 * @param off              Bytes offset.
	 * @param len              Bytes length.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source.
	 * @param shouldBeLinked   Whether the class should be linked to the class loader.
	 * @return Defined class.
	 */
	InstanceClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source, boolean shouldBeLinked);

	/**
	 * Attempts to find a class in the VM.
	 * Throws VM exception if class was not found,
	 * or failed to initialize.
	 *
	 * @param classLoader  Class loader to search class in.
	 * @param internalName Class internal name.
	 * @param initialize   Whether the class should be initialized.
	 * @return Class instance.
	 */
	JavaClass findClass(ObjectValue classLoader, String internalName, boolean initialize);

	/**
	 * Attempts to find a class in the VM.
	 * Throws VM exception if class was not found,
	 * or failed to initialize.
	 *
	 * @param classLoader Class loader to search class in.
	 * @param type        Class type.
	 * @param initialize  Whether the class should be initialized.
	 * @return Class instance.
	 * @throws PanicException If {@literal type} is a primitive type.
	 */
	JavaClass findClass(ObjectValue classLoader, Type type, boolean initialize);
}
