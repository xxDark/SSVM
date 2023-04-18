package dev.xdark.ssvm;

import dev.xdark.ssvm.util.TypeSafeMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypeSafeMapTest {

	@Test
	public void testMap() {
		TypeSafeMap map = new TypeSafeMap();
		String key = "key";
		String value = "value";
		map.subMap(String.class).put(key, value);
		assertEquals(value, map.get(key));
		assertEquals(value, map.get(String.class, key));
		map.subMap(Object.class).put(key, value);
		assertEquals(value, map.get(Object.class, key));
	}
}
