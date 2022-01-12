package dev.xdark.ssvm;

import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).bootstrap();
	}

	@Test
	public void testConstructor() {
		var helper = vm.getHelper();
		var symbols = vm.getSymbols();
		var c = symbols.java_lang_String;
		var str = "Hello, World";
		var chars = helper.toVMChars(str.toCharArray());
		var parameters = helper.toVMValues(new ObjectValue[]{vm.getPrimitives().charPrimitive.newArrayClass().getOop()});
		var constructor = (InstanceValue) helper.invokeVirtual("getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", new Value[0], new Value[]{
				c.getOop(),
				parameters
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		constructor.setBoolean("override", true);
		var value = helper.invokeVirtual("newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{
				constructor,
				helper.toVMValues(new ObjectValue[]{chars})
		}).getResult();
		assertEquals(str, helper.readUtf8(value));
	}

	@Test
	public void testMethod() {
		var helper = vm.getHelper();
		var symbols = vm.getSymbols();
		var c = symbols.java_lang_String;
		var str = "Hello, World";
		var instance = helper.newUtf8(str);
		var parameters = helper.emptyArray(symbols.java_lang_Class);
		var method = (InstanceValue) helper.invokeVirtual("getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", new Value[0], new Value[]{
				c.getOop(),
				helper.newUtf8("toUpperCase"),
				parameters
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		method.setBoolean("override", true);
		var lower = helper.invokeVirtual("invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{
				method,
				instance,
				helper.emptyArray(symbols.java_lang_Object)
		}).getResult();
		assertEquals(str.toUpperCase(), helper.readUtf8(lower));
	}

	@Test
	public void testField() {
		var helper = vm.getHelper();
		var symbols = vm.getSymbols();
		var c = symbols.java_lang_Integer;
		var primitive = ThreadLocalRandom.current().nextInt();
		var instance = helper.invokeStatic(symbols.java_lang_Integer, "valueOf", "(I)Ljava/lang/Integer;", new Value[0], new Value[]{
				new IntValue(primitive)
		}).getResult();
		var field = (InstanceValue) helper.invokeVirtual("getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", new Value[0], new Value[]{
				c.getOop(),
				helper.newUtf8("value"),
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		field.setBoolean("override", true);
		var backing = helper.invokeVirtual("getInt", "(Ljava/lang/Object;)I", new Value[0], new Value[]{
				field,
				instance,
		}).getResult();
		assertEquals(primitive, backing.asInt());
	}
}
