package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import org.objectweb.asm.ClassReader;

import java.io.IOException;

/**
 * Default implementation of boot class loader that
 * pulls classes from native boot class loader.
 *
 * @author xDark
 */
public final class RuntimeBootClassLoader implements BootClassLoader {

	private final ClassLoader delegate = new BootClassLoaderDelegate();

	@Override
	public ClassParseResult findBootClass(String name) {
		ClassReader cr;
		try (var in = delegate.getResourceAsStream(name + ".class")) {
			if (in == null) {
				return null;
			}
			cr = new ClassReader(in);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read bootstrap class: " + name, ex);
		}
		var node = ClassUtil.readNode(cr);
		return new ClassParseResult(cr, node);
	}

	private static final class BootClassLoaderDelegate extends ClassLoader {

		BootClassLoaderDelegate() {
			super(null);
		}
	}
}
