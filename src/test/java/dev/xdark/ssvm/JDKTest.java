package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
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
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(c, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.set(0, instance);
		helper.invoke(init, locals);
		testMapImplementation(instance);
	}

	@Test
	public void testConcurrentHashMap() {
		InstanceJavaClass c = (InstanceJavaClass) vm.findBootstrapClass("java/util/concurrent/ConcurrentHashMap", true);
		VMHelper helper = vm.getHelper();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(c, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.set(0, instance);
		helper.invoke(init, locals);
		testMapImplementation(instance);
	}

	@Test
	public void testTreeMap() {
		InstanceJavaClass c = (InstanceJavaClass) vm.findBootstrapClass("java/util/TreeMap", true);
		VMHelper helper = vm.getHelper();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(c, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.set(0, instance);
		helper.invoke(init, locals);
		testMapImplementation(instance);
	}

	private static void testMapImplementation(InstanceValue map) {
		String keyBase = "key";
		String valueBase = "value";
		VMHelper helper = vm.getHelper();
		LinkResolver linkResolver = vm.getPublicLinkResolver();
		ThreadStorage ts = vm.getThreadStorage();
		JavaMethod put = linkResolver.resolveVirtualMethod(map, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
		JavaMethod get = linkResolver.resolveVirtualMethod(map, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
		for (int i = 0; i < 100; i++) {
			ObjectValue key = helper.newUtf8(keyBase + i);
			String $value = valueBase + i;
			ObjectValue value = helper.newUtf8($value);
			{
				Locals locals = ts.newLocals(put);
				locals.set(0, map);
				locals.set(1, key);
				locals.set(2, value);
				helper.invoke(put, locals);
			}
			Locals locals = ts.newLocals(get);
			locals.set(0 ,map);
			locals.set(1, key);
			ExecutionContext v = helper.invoke(get, locals);
			assertEquals($value, helper.readUtf8(v.getResult()));
		}
		JavaMethod clear = linkResolver.resolveVirtualMethod(map, "clear", "()V");
		Locals locals = ts.newLocals(clear);
		locals.set(0, map);
		helper.invoke(clear, locals);
		JavaMethod isEmpty = linkResolver.resolveVirtualMethod(map, "isEmpty", "()Z");
		locals = ts.newLocals(isEmpty);
		locals.set(0, map);
		assertTrue(helper.invoke(isEmpty, locals).getResult().asBoolean());
	}
}
