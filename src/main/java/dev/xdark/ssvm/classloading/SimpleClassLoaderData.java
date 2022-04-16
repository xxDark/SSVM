package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class storage.
 *
 * @author xDark
 */
public final class SimpleClassLoaderData implements ClassLoaderData {

	private final Map<String, InstanceJavaClass> table = new HashMap<>();

	@Override
	public InstanceJavaClass getClass(String name) {
		return table.get(name);
	}

	@Override
	public void linkClass(InstanceJavaClass jc) {
		String name = jc.getInternalName();
		if (table.putIfAbsent(name, jc) != null) {
			throw new IllegalStateException(name);
		}
		jc.link();
	}

	@Override
	public void forceLinkClass(InstanceJavaClass jc) {
		table.put(jc.getInternalName(), jc);
	}
}
