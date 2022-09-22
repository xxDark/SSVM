package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.mirror.type.JavaClass;

final class MethodResolution {
	final JavaClass klass;
	final String name;
	final String desc;

	MethodResolution(JavaClass klass, String name, String desc) {
		this.klass = klass;
		this.name = name;
		this.desc = desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MethodResolution that = (MethodResolution) o;

		if (!klass.equals(that.klass)) {
			return false;
		}
		if (!name.equals(that.name)) {
			return false;
		}
		return desc.equals(that.desc);
	}

	@Override
	public int hashCode() {
		int result = klass.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + desc.hashCode();
		return result;
	}
}
