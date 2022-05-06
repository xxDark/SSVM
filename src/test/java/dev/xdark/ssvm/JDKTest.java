package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
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
		InstanceJavaClass c = (InstanceJavaClass) vm.findBootstrapClass("java/util/HashMap", true);
		VMHelper helper = vm.getHelper();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	@Test
	public void testConcurrentHashMap() {
		InstanceJavaClass c = (InstanceJavaClass) vm.findBootstrapClass("java/util/concurrent/ConcurrentHashMap", true);
		VMHelper helper = vm.getHelper();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	@Test
	public void testTreeMap() {
		InstanceJavaClass c = (InstanceJavaClass) vm.findBootstrapClass("java/util/TreeMap", true);
		VMHelper helper = vm.getHelper();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		testMapImplementation(instance);
	}

	private static void testMapImplementation(InstanceValue map) {
		String keyBase = "key";
		String valueBase = "value";
		VMHelper helper = vm.getHelper();
		for (int i = 0; i < 100; i++) {
			ObjectValue key = helper.newUtf8(keyBase + i);
			String $value = valueBase + i;
			ObjectValue value = helper.newUtf8($value);
			helper.invokeVirtual("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key, value});
			ExecutionContext v = helper.invokeVirtual("get", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value[0], new Value[]{map, key});
			assertEquals($value, helper.readUtf8(v.getResult()));
		}
		helper.invokeVirtual("clear", "()V",new Value[0], new Value[]{map});
		assertTrue(helper.invokeVirtual("isEmpty", "()Z", new Value[0], new Value[]{map}).getResult().asBoolean());
	}
}
