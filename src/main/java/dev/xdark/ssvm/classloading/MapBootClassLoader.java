package dev.xdark.ssvm.classloading;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Boot class loader that pulls classes
 * from map.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class MapBootClassLoader implements BootClassLoader {

	private final Map<String, ClassParseResult> classMap;

	@Override
	public ClassParseResult findBootClass(String name) {
		return classMap.get(name);
	}
}
