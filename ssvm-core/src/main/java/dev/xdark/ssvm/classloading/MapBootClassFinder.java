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
public final class MapBootClassFinder implements BootClassFinder {

	private final Map<String, ParsedClassData> classMap;

	@Override
	public ParsedClassData findBootClass(String name) {
		return classMap.get(name);
	}
}
