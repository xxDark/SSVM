package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.type.InstanceJavaClass;

/**
 * Called upon VM initialization.
 */
public interface VMInitializer {

	/**
	 * Called early upon VM initialization.
	 *
	 * @param vm VM being initialized.
	 */
	default void initBegin(VirtualMachine vm) {
	}

	/**
	 * Called when class loader storage is being
	 * set and initialized.
	 *
	 * @param vm VM being initialized.
	 */
	default void initClassLoaders(VirtualMachine vm) {
	}

	/**
	 * Called upon linkage of {@code java/lang/Class}
	 * and {@code java/lang/Object}.
	 *
	 * @param vm     VM being initialized.
	 * @param klass  {@code java/lang/Class} class.
	 * @param object {@code java/lang/Object} class.
	 */
	default void bootLink(VirtualMachine vm, InstanceJavaClass klass, InstanceJavaClass object) {
	}

	/**
	 * Called after {@link NativeJava} initialization is complete.
	 *
	 * @param vm VM being initialized.
	 */
	default void nativeInit(VirtualMachine vm) {
	}
}
