package dev.xdark.ssvm;

import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
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
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val c = symbols.java_lang_String;
		val str = "Hello, World";
		val chars = helper.toVMChars(str.toCharArray());
		val parameters = helper.toVMValues(new ObjectValue[]{vm.getPrimitives().charPrimitive.newArrayClass().getOop()});
		val constructor = (InstanceValue) helper.invokeVirtual("getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", new Value[0], new Value[]{
				c.getOop(),
				parameters
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		constructor.setBoolean("override", true);
		val value = helper.invokeVirtual("newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{
				constructor,
				helper.toVMValues(new ObjectValue[]{chars})
		}).getResult();
		assertEquals(str, helper.readUtf8(value));
	}

	@Test
	public void testMethod() {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val c = symbols.java_lang_String;
		val str = "Hello, World";
		val instance = helper.newUtf8(str);
		val parameters = helper.emptyArray(symbols.java_lang_Class);
		val method = (InstanceValue) helper.invokeVirtual("getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", new Value[0], new Value[]{
				c.getOop(),
				helper.newUtf8("toUpperCase"),
				parameters
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		method.setBoolean("override", true);
		val lower = helper.invokeVirtual("invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{
				method,
				instance,
				helper.emptyArray(symbols.java_lang_Object)
		}).getResult();
		assertEquals(str.toUpperCase(), helper.readUtf8(lower));
	}

	@Test
	public void testField() {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val c = symbols.java_lang_Integer;
		val primitive = ThreadLocalRandom.current().nextInt();
		val instance = helper.invokeStatic(symbols.java_lang_Integer, "valueOf", "(I)Ljava/lang/Integer;", new Value[0], new Value[]{
				new IntValue(primitive)
		}).getResult();
		val field = (InstanceValue) helper.invokeVirtual("getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", new Value[0], new Value[]{
				c.getOop(),
				helper.newUtf8("value"),
		}).getResult();
		// We have to inject 'override' flag because VM
		// performs caller check here
		// and backtrace is empty at this point
		// since we call all methods 'outside' of VM
		field.setBoolean("override", true);
		val backing = helper.invokeVirtual("getInt", "(Ljava/lang/Object;)I", new Value[0], new Value[]{
				field,
				instance,
		}).getResult();
		assertEquals(primitive, backing.asInt());
	}
}
