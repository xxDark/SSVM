package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple class storage.
 *
 * @author xDark
 */
public final class SimpleClassLoaderData implements ClassLoaderData {

	private final Map<String, InstanceJavaClass> table = new HashMap<>();
	private final Collection<InstanceJavaClass> classesView = Collections.unmodifiableCollection(table.values());

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

	@Override
	public Collection<InstanceJavaClass> getAll() {
		return classesView;
	}

	@Override
	public void claim(ClassLoaderData classLoaderData) {
		table.putAll(classLoaderData.getAll().stream().collect(Collectors.toMap(
				InstanceJavaClass::getInternalName,
				Function.identity()
		)));
	}
}
