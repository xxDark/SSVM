package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JDKTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		(vm = new VirtualMachine()).bootstrap();
	}

	@Test
	public void testHashMap() {
		val c = (InstanceJavaClass) vm.findBootstrapClass("java/util/HashMap", true);
		val helper = vm.getHelper();
		val instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	@Test
	public void testConcurrentHashMap() {
		val c = (InstanceJavaClass) vm.findBootstrapClass("java/util/concurrent/ConcurrentHashMap", true);
		val helper = vm.getHelper();
		val instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	@Test
	public void testTreeMap() {
		val c = (InstanceJavaClass) vm.findBootstrapClass("java/util/TreeMap", true);
		val helper = vm.getHelper();
		val instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	private static void testMapImplementation(InstanceValue map) {
		val keyBase = "key";
		val valueBase = "value";
		val helper = vm.getHelper();
		for (int i = 0; i < 100; i++) {
			val key = helper.newUtf8(keyBase + i);
			val $value = valueBase + i;
			val value = helper.newUtf8($value);
			helper.invokeVirtual("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key, value});
			val v = helper.invokeVirtual("get", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key});
			assertEquals($value, helper.readUtf8(v.getResult()));
		}
		helper.invokeVirtual("clear", "()V",new Value[0], new Value[]{map});
		assertTrue(helper.invokeVirtual("isEmpty", "()Z", new Value[0], new Value[]{map}).getResult().asBoolean());
	}
}
