package dev.xdark.ssvm;

import dev.xdark.ssvm.invoke.Argument;
import dev.xdark.ssvm.invoke.InvocationUtil;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link InvocationUtil}
 */
public class InvocationTest {
	@Test
	public void testIntClassMethods() {
		VirtualMachine vm = new VirtualMachine();
		vm.bootstrap();
		InvocationUtil util = InvocationUtil.create(vm);
		InstanceClass java_lang_integer = vm.getSymbols().java_lang_Integer();
		int value = Long.hashCode(System.currentTimeMillis());
		int primitive = util.invokeInt(
			java_lang_integer.getMethod("parseInt", "(Ljava/lang/String;)I"),
			Argument.reference(vm.getOperations().newUtf8(Integer.toString(value)))
		);
		assertEquals(value, primitive);
		InstanceClass java_lang_long = vm.getSymbols().java_lang_Long();
		ObjectValue wrapper = util.invokeReference(
			java_lang_long.getMethod("valueOf", "(J)Ljava/lang/Long;"),
			Argument.int64(value)
		);
		primitive = util.invokeInt(
			java_lang_long.getMethod("intValue", "()I"),
			Argument.reference(wrapper)
		);
		assertEquals(value, primitive);
	}
}
