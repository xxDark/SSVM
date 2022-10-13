package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Set;

final class ExceptionInfo {
	final Set<InstanceClass> types;
	final ObjectValue nullConstant;

	ExceptionInfo(Set<InstanceClass> types, ObjectValue nullConstant) {
		this.types = types;
		this.nullConstant = nullConstant;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExceptionInfo that = (ExceptionInfo) o;

		if (!types.equals(that.types)) {
			return false;
		}
		return nullConstant.equals(that.nullConstant);
	}

	@Override
	public int hashCode() {
		int result = types.hashCode();
		result = 31 * result + nullConstant.hashCode();
		return result;
	}
}
