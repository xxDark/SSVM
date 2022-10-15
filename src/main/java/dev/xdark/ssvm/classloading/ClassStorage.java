package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.metadata.MetadataStorage;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Class storage.
 * <p>
 * See {@code ava_lang_Class::as_Klass} in HotSpot.
 *
 * @author xDark
 */
public interface ClassStorage extends MetadataStorage<JavaClass> {

	/**
	 * Returns class mirror for the oop.
	 * VM will panic if id field is out of sync.
	 *
	 * @param oop Class to get mirror for.
	 * @return Class mirror.
	 */
	JavaClass lookup(InstanceValue oop);
}
