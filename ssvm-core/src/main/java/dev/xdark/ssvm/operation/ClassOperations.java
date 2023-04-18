package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	void link(@NotNull InstanceClass instanceClass);

	/**
	 * Initializes instance class.
	 *
	 * @param instanceClass Class to initialize.
	 */
	void initialize(@NotNull InstanceClass instanceClass);

	/**
	 * Performs an instanceof check.
	 *
	 * @param value Value to do the check on.
	 * @param type  Class to check against.
	 * @return {@code true} if instance type check succeeds.
	 */
	boolean isInstanceOf(@NotNull ObjectValue value, @NotNull JavaClass type);

	/**
	 * Defines new class in the VM.
	 * Throws VM exception if class failed verification.
	 *
	 * @param classLoader      Class loader.
	 * @param data             Parsed class data.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source.
	 * @param options          Class definition options.
	 * @return Defined class.
	 */
	@NotNull
	InstanceClass defineClass(ObjectValue classLoader, ParsedClassData data, ObjectValue protectionDomain, String source, int options);

	/**
	 * Defines new class in the VM.
	 * Throws VM exception if class failed verification.
	 *
	 * @param classLoader      Class loader.
	 * @param data             Parsed class data.
	 * @param protectionDomain Protection domain.
	 * @param source           Class source.
	 * @return Defined class.
	 */
	default InstanceClass defineClass(ObjectValue classLoader, ParsedClassData data, ObjectValue protectionDomain, String source) {
		return defineClass(classLoader, data, protectionDomain, source, 0);
	}

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
	 * @param options          Class definition options.
	 * @return Defined class.
	 */
	@NotNull
	InstanceClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source, int options);

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
	 * @return Defined class.
	 */
	@NotNull
	default InstanceClass defineClass(ObjectValue classLoader, String name, byte[] b, int off, int len, ObjectValue protectionDomain, String source) {
		return defineClass(classLoader, name, b, off, len, protectionDomain, source, 0);
	}

	/**
	 * Attempts to find a class in the VM.
	 * Throws VM exception if class was not found,
	 * or failed to initialize.
	 *
	 * @param klass        Host class.
	 * @param internalName Class internal name.
	 * @param initialize   Whether the class should be initialized.
	 * @return Class instance.
	 */
	@NotNull
	JavaClass findClass(JavaClass klass, String internalName, boolean initialize);

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
	@NotNull
	JavaClass findClass(ObjectValue classLoader, String internalName, boolean initialize);

	/**
	 * Attempts to find a bootstrap class in the VM.
	 *
	 * @param internalName Class internal name.
	 * @param initialize   Whether the class should be initialized.
	 * @return Class instance or {@code null},
	 * if not found.
	 */
	@Nullable
	JavaClass findBootstrapClassOrNull(String internalName, boolean initialize);

	/**
	 * Attempts to find a class in the VM.
	 * Throws VM exception if class was not found,
	 * or failed to initialize.
	 *
	 * @param klass      Host class.
	 * @param type       Class type.
	 * @param initialize Whether the class should be initialized.
	 * @return Class instance.
	 */
	@NotNull
	JavaClass findClass(JavaClass klass, Type type, boolean initialize);

	/**
	 * Attempts to find a class in the VM.
	 * Throws VM exception if class was not found,
	 * or failed to initialize.
	 *
	 * @param classLoader Class loader to search class in.
	 * @param type        Class type.
	 * @param initialize  Whether the class should be initialized.
	 * @return Class instance.
	 */
	@NotNull
	JavaClass findClass(ObjectValue classLoader, Type type, boolean initialize);
}
