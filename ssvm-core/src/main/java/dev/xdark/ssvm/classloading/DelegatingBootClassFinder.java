package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Boot class loader that pulls
 * it's resources from Java {@link ClassLoader}.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DelegatingBootClassFinder implements BootClassFinder {

	private final ClassLoader delegate;

	@Override
	public ParsedClassData findBootClass(String name) {
		ClassReader cr;
		try (InputStream in = delegate.getResourceAsStream(name + ".class")) {
			if (in == null) {
				return null;
			}
			cr = new ClassReader(in);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read bootstrap class: " + name, ex);
		}
		ClassNode node = ClassUtil.readNode(cr);
		return new ParsedClassData(cr, node);
	}
}
