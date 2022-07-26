package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
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
		pushFrame();
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass c = symbols.java_lang_String();
		String str = "Hello, World";
		ArrayValue chars = helper.toVMChars(str.toCharArray());
		ArrayValue parameters = helper.toVMValues(new ObjectValue[]{vm.getPrimitives().charPrimitive().newArrayClass().getOop()});
		InstanceValue constructor = (InstanceValue) helper.invokeVirtual("getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", new Value[]{
				c.getOop(),
				parameters
		}).getResult();
		constructor.setBoolean("override", true);
		Value value = helper.invokeVirtual("newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", new Value[]{
				constructor,
				helper.toVMValues(new ObjectValue[]{chars})
		}).getResult();
		assertEquals(str, helper.readUtf8(value));
	}

	@Test
	public void testMethod() {
		pushFrame();
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass c = symbols.java_lang_String();
		String str = "Hello, World";
		ObjectValue instance = helper.newUtf8(str);
		ArrayValue parameters = helper.emptyArray(symbols.java_lang_Class());
		InstanceValue method = (InstanceValue) helper.invokeVirtual("getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", new Value[]{
				c.getOop(),
				helper.newUtf8("toUpperCase"),
				parameters
		}).getResult();
		method.setBoolean("override", true);
		Value lower = helper.invokeVirtual("invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", new Value[]{
				method,
				instance,
				helper.emptyArray(symbols.java_lang_Object())
		}).getResult();
		assertEquals(str.toUpperCase(), helper.readUtf8(lower));
	}

	@Test
	public void testField() {
		pushFrame();
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass c = symbols.java_lang_Integer();
		int primitive = ThreadLocalRandom.current().nextInt();
		Value instance = helper.boxInt(IntValue.of(primitive));
		InstanceValue field = (InstanceValue) helper.invokeVirtual("getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", new Value[]{
				c.getOop(),
				helper.newUtf8("value"),
		}).getResult();
		field.setBoolean("override", true);
		Value backing = helper.invokeVirtual("getInt", "(Ljava/lang/Object;)I", new Value[]{
				field,
				instance,
		}).getResult();
		assertEquals(primitive, backing.asInt());
	}

	private static void pushFrame() {
		ThreadManager threadManager = vm.getThreadManager();
		threadManager.currentThread().getBacktrace()
				.push(StackFrame.from(
						vm.getSymbols().java_lang_System(),
						"junit",
						null,
						-2
				));
	}
}
