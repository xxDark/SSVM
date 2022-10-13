package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Class storage.
 * <p>
 * See {@code ava_lang_Class::as_Klass} in HotSpot.
 *
 * @author xDark
 */
public interface ClassStorage {

	/**
	 * Registers new class.
	 *
	 * @param klass Class to register.
	 * @return Class id.
	 */
	int register(JavaClass klass);

	/**
	 * Returns class mirror for the oop.
	 * VM will panic if id field is out of sync.
	 *
	 * @param oop Class to get mirror for.
	 * @return Class mirror.
	 */
	JavaClass lookup(InstanceValue oop);

	/**
	 * @param classId Class id.
	 * @return Class by it's id or {@code null},
	 * if not found.
	 */
	JavaClass lookup(int classId);
}
