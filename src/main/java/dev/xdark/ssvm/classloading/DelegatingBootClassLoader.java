package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.objectweb.asm.ClassReader;

import java.io.IOException;

/**
 * Boot class loader that pulls
 * it's resources from Java {@link ClassLoader}.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DelegatingBootClassLoader implements BootClassLoader {

	private final ClassLoader delegate;

	@Override
	public ClassParseResult findBootClass(String name) {
		ClassReader cr;
		try (val in = delegate.getResourceAsStream(name + ".class")) {
			if (in == null) {
				return null;
			}
			cr = new ClassReader(in);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read bootstrap class: " + name, ex);
		}
		val node = ClassUtil.readNode(cr);
		return new ClassParseResult(cr, node);
	}
}
