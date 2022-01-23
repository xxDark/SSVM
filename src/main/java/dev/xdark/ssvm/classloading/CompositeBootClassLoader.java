package dev.xdark.ssvm.classloading;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Boot class loader that pulls classes
 * from a list of available loaders.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class CompositeBootClassLoader implements BootClassLoader {

	private final Iterable<? extends BootClassLoader> classLoaders;

	@Override
	public ClassParseResult findBootClass(String name) {
		for (val cl : classLoaders) {
			val res = cl.findBootClass(name);
			if (res != null) return res;
		}
		return null;
	}
}
