package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultStringOperations implements StringOperations {

	private static final int STRING_COPY_THRESHOLD = 256;
	private final MemoryManager memoryManager;
	private final ThreadManager threadManager;
	private final Symbols symbols;
	private final LinkResolver linkResolver;
	private final VMOperations ops;

	@Override
	public InstanceValue newUtf8(String value) {
		return newUtf8FromChars(toChars(value));
	}

	@Override
	public InstanceValue newUtf8FromChars(ArrayValue value) {
		InstanceClass jc = symbols.java_lang_String();
		InstanceValue wrapper = memoryManager.newInstance(jc);
		JavaField charValue = jc.getField("value", "[C");
		if (charValue != null) {
			memoryManager.writeValue(wrapper, charValue.getOffset(), value);
		} else {
			JavaMethod init = linkResolver.resolveSpecialMethod(jc, "<init>", "([C)V");
			Locals locals = threadManager.currentThreadStorage().newLocals(init);
			locals.setReference(0, wrapper);
			locals.setReference(1, value);
			ops.invokeVoid(init, locals);
		}
		return wrapper;
	}

	@Override
	public String readUtf8(ObjectValue value) {
		if (value.isNull()) {
			return null;
		}
		InstanceClass jc = (InstanceClass) value.getJavaClass();
		if (jc != symbols.java_lang_String()) {
			throw new IllegalStateException("Not a string: " + value + " (" + jc + ')');
		}
		JavaField charValue = jc.getField("value", "[C");
		ArrayValue array;
		if (charValue != null) {
			array = (ArrayValue) memoryManager.readReference(value, charValue.getOffset());
		} else {
			JavaMethod toCharArray = linkResolver.resolveVirtualMethod(jc, jc, "toCharArray", "()[C");
			Locals locals = threadManager.currentThreadStorage().newLocals(toCharArray);
			locals.setReference(0, value);
			array = (ArrayValue) ops.invokeReference(toCharArray, locals);
		}
		return UnsafeUtil.newString(ops.toJavaChars(array));
	}

	@Override
	public ArrayValue toChars(String value) {
		int length = value.length();
		ArrayValue wrapper = ops.allocateCharArray(length);
		if (UnsafeUtil.stringValueFieldAccessible() || length <= STRING_COPY_THRESHOLD) {
			MemoryData memory = wrapper.getMemory().getData();
			char[] chars = UnsafeUtil.getChars(value);
			memory.write(memoryManager.arrayBaseOffset(wrapper), chars, 0, chars.length);
		} else {
			while (length-- != 0) {
				wrapper.setChar(length, value.charAt(length));
			}
		}
		return wrapper;
	}
}
