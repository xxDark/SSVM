package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import org.objectweb.asm.ClassReader;

/**
 * Default implementation of boot class loader that
 * pulls classes from native boot class loader.
 *
 * @author xDark
 */
public final class RuntimeBootClassLoader implements BootClassLoader {

	private final ClassLoader delegate = new BootClassLoaderDelegate();

	@Override
	public LookupResult findBootClass(String name) throws Exception {
		ClassReader cr;
		try (var in = delegate.getResourceAsStream(name + ".class")) {
			if (in == null) {
				return null;
			}
			cr = new ClassReader(in);
		}
		var node = ClassUtil.readNode(cr);
		return new LookupResult(cr, node);
	}

	private static final class BootClassLoaderDelegate extends ClassLoader {

		BootClassLoaderDelegate() {
			super(null);
		}
	}
}
