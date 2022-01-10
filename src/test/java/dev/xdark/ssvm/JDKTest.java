package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JDKTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testHashMap() {
		var c = (InstanceJavaClass) vm.findBootstrapClass("java/util/HashMap", true);
		var helper = vm.getHelper();
		var instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	@Test
	public void testConcurrentHashMap() {
		var c = (InstanceJavaClass) vm.findBootstrapClass("java/util/concurrent/ConcurrentHashMap", true);
		var helper = vm.getHelper();
		var instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	private static void testMapImplementation(InstanceValue map) {
		var keyBase = "key";
		var valueBase = "value";
		var helper = vm.getHelper();
		for (int i = 0; i < 100; i++) {
			var key = helper.newUtf8(keyBase + i);
			var $value = valueBase + i;
			var value = helper.newUtf8($value);
			helper.invokeVirtual("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key, value});
			var v = helper.invokeVirtual("get", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key});
			assertEquals($value, helper.readUtf8(v.getResult()));
		}
	}
}
