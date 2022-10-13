package dev.xdark.ssvm.util;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Some VM operations implementations.
 *
 * @author xDArk
 */
public final class DefaultOperations implements Operations {

	private final Symbols symbols;
	private final Primitives primitives;
	private final Helper helper;
	private final MemoryManager memoryManager;
	private final LinkResolver linkResolver;

	public DefaultOperations(Symbols symbols, Primitives primitives, Helper helper, MemoryManager memoryManager, LinkResolver linkResolver) {
		this.symbols = symbols;
		this.primitives = primitives;
		this.helper = helper;
		this.memoryManager = memoryManager;
		this.linkResolver = linkResolver;
	}

	@Override
	public ObjectValue checkCast(ObjectValue value, JavaClass klass) {
		if (!value.isNull()) {
			JavaClass against = value.getJavaClass();
			if (!klass.isAssignableFrom(against)) {
				helper.throwException(symbols.java_lang_ClassCastException(), against.getName() + " cannot be cast to " + klass.getName());
			}
		}
		return value;
	}

	@Override
	public void throwException(ObjectValue value) {
		if (value.isNull()) {
			// NPE it is then.
			value = helper.newException(symbols.java_lang_NullPointerException());
		}
		throw new VMException((InstanceValue) value);
	}

	@Override
	public boolean instanceofCheck(ObjectValue value, JavaClass javaClass) {
		if (value.isNull()) {
			return false;
		}
		return javaClass.isAssignableFrom(value.getJavaClass());
	}
}
