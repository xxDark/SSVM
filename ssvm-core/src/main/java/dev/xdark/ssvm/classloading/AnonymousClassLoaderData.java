package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.CloseableLock;

import java.util.Collection;

/**
 * Anonymous class loader data.
 *
 * @author xDark
 */
public final class AnonymousClassLoaderData implements ClassLoaderData {

	private final ClassLoaderData anonymous;
	private final ClassLoaderData host;

	public AnonymousClassLoaderData(ClassLoaderData anonymous, ClassLoaderData host) {
		this.anonymous = anonymous;
		this.host = host;
	}

	@Override
	public InstanceClass getClass(String name) {
		InstanceClass data = anonymous.getClass(name);
		if (data == null) {
			data = host.getClass(name);
		}
		return data;
	}

	@Override
	public boolean linkClass(InstanceClass jc) {
		return anonymous.linkClass(jc);
	}

	@Override
	public CloseableLock lock() {
		return anonymous.lock();
	}

	@Override
	public Collection<InstanceClass> list() {
		return anonymous.list();
	}
}
