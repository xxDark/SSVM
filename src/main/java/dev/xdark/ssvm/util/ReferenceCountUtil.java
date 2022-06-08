package dev.xdark.ssvm.util;

import dev.xdark.ssvm.value.ReferenceCounted;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReferenceCountUtil {

	public void tryRetain(Value value) {
		if (value instanceof ReferenceCounted && !value.isNull()) {
			((ReferenceCounted) value).retain();
		}
	}

	public boolean tryRelease(Value value) {
		if (value instanceof ReferenceCounted && !value.isNull()) {
			return ((ReferenceCounted) value).release();
		}
		return false;
	}
}
