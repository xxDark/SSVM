package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * This is a temporary solution to pull
 * references by their addresses and will be removed later.
 */
@Deprecated
public interface ReferenceMap {

	ObjectValue getReference(long address);
}
