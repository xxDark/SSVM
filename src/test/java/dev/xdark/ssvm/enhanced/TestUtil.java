package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.opentest4j.TestAbortedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

@UtilityClass
public class TestUtil {

	public void test(Class<?> klass, boolean bootstrap, Consumer<InstanceJavaClass> init) {
		val vm = newVirtualMachine();
		if (bootstrap) {
			vm.bootstrap();
		}
		byte[] result;
		try(val in = TestUtil.class.getClassLoader().getResourceAsStream(klass.getName().replace('.', '/') + ".class")) {
			val bytes = new byte[1024];
			val out = new ByteArrayOutputStream(1024);
			int r;
			while((r = in.read(bytes)) != -1) {
				out.write(bytes, 0, r);
			}
			result = out.toByteArray();
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		val helper = vm.getHelper();
		InstanceJavaClass res;
		try {
			res = helper.defineClass(NullValue.INSTANCE, null, result, 0, result.length, NullValue.INSTANCE, "JVM_DefineClass");
		} catch(VMException ex) {
			throw new IllegalStateException(helper.toJavaException(ex.getOop()));
		}
		if (init != null) {
			init.accept(res);
		}
		for (val m : res.getStaticMethodLayout().getAll()) {
			val node = m.getNode();
			val annotations = node.visibleAnnotations;
			if (annotations == null || annotations.stream().noneMatch(x -> "Ldev/xdark/ssvm/enhanced/VMTest;".equals(x.desc))) {
				continue;
			}
			try {
				helper.invokeStatic(res, m, new Value[0], new Value[0]);
			} catch(VMException ex) {
				System.err.println(ex.getOop());
				try {
					helper.invokeVirtual("printStackTrace", "()V", new Value[0], new Value[]{ex.getOop()});
				} catch(VMException ex1) {
					System.err.println(ex1.getOop());
					helper.toJavaException(ex1.getOop()).printStackTrace();
				}
				throw new TestAbortedException();
			}
		}
	}

	public void test(Class<?> klass, boolean bootstrap) {
		test(klass, bootstrap, null);
	}

	public void test(Class<?> klass) {
		test(klass, false, null);
	}

	private VirtualMachine newVirtualMachine() {
		return new VirtualMachine() {
			@Override
			protected FileDescriptorManager createFileDescriptorManager() {
				return new HostFileDescriptorManager();
			}
		};
	}
}
