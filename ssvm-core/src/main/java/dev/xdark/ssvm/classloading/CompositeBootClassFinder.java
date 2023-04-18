package dev.xdark.ssvm.classloading;

import lombok.RequiredArgsConstructor;

/**
 * Boot class loader that pulls classes
 * from a list of available loaders.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class CompositeBootClassFinder implements BootClassFinder {

	private final Iterable<? extends BootClassFinder> classLoaders;

	@Override
	public ParsedClassData findBootClass(String name) {
		for (BootClassFinder cl : classLoaders) {
			ParsedClassData res = cl.findBootClass(name);
			if (res != null) {
				return res;
			}
		}
		return null;
	}
}
