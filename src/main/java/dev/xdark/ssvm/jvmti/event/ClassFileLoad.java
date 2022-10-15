package dev.xdark.ssvm.jvmti.event;

import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.MutableValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Fired when a class is loaded.
 *
 * @author xDark
 */
@FunctionalInterface
public interface ClassFileLoad {

	/**
	 * Similar to ClassFileLoadHook.
	 *
	 * @param classBeingRedefined Class being redefined.
	 * @param classLoader         Class loader.
	 * @param protectionDomain    Protection domain.
	 * @param data                Class data.
	 */
	void invoke(
		InstanceClass classBeingRedefined,
		ObjectValue classLoader,
		ObjectValue protectionDomain,
		MutableValue<ParsedClassData> data
	);
}
