package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import org.objectweb.asm.ClassReader;

import java.nio.ByteBuffer;

/**
 * Default implementation for class definer.
 *
 * @author xDark
 */
public final class SimpleClassDefiner implements ClassDefiner {

	@Override
	public ClassParseResult parseClass(String name, byte[] classBytes, int off, int len, String source) {
		var cr = new ClassReader(classBytes, off, len);
		return new ClassParseResult(cr, ClassUtil.readNode(cr));
	}
}
