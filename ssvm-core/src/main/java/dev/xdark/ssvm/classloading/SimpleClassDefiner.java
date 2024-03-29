package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.util.ClassUtil;
import org.objectweb.asm.ClassReader;

/**
 * Default implementation for class definer.
 *
 * @author xDark
 */
public final class SimpleClassDefiner implements ClassDefiner {

	@Override
	public ParsedClassData parseClass(String name, byte[] classBytes, int off, int len, String source) {
		ClassReader cr = new ClassReader(classBytes, off, len);
		return new ParsedClassData(cr, ClassUtil.readNode(cr));
	}
}
