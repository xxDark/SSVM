package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.SynchronizedMemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.SynchronizedMemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.opentest4j.TestAbortedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class TestUtil {

	public void test(Class<?> klass, boolean bootstrap, Consumer<InstanceJavaClass> init) {
		VirtualMachine vm = newVirtualMachine();
		if (bootstrap) {
			vm.bootstrap();
		} else {
			vm.initialize();
		}
		byte[] result;
		try(InputStream in = TestUtil.class.getClassLoader().getResourceAsStream(klass.getName().replace('.', '/') + ".class")) {
			byte[] bytes = new byte[1024];
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			int r;
			while((r = in.read(bytes)) != -1) {
				out.write(bytes, 0, r);
			}
			result = out.toByteArray();
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		VMHelper helper = vm.getHelper();
		ObjectValue nullValue = vm.getMemoryManager().nullValue();
		InstanceJavaClass res;
		try {
			res = helper.defineClass(nullValue, null, result, 0, result.length, nullValue, "JVM_DefineClass");
		} catch(VMException ex) {
			throw new IllegalStateException(helper.toJavaException(ex.getOop()));
		}
		if (init != null) {
			init.accept(res);
		}
		ThreadStorage ts = vm.getThreadStorage();
		for (JavaMethod m : res.getStaticMethodLayout().getAll()) {
			MethodNode node = m.getNode();
			List<AnnotationNode> annotations = node.visibleAnnotations;
			if (annotations == null || annotations.stream().noneMatch(x -> "Ldev/xdark/ssvm/enhanced/VMTest;".equals(x.desc))) {
				continue;
			}
			try {
				helper.invokeDirect(m, ts.newLocals(m));
			} catch(VMException ex) {
				InstanceValue oop = ex.getOop();
				System.err.println(oop);
				try {
					JavaMethod printStackTrace = vm.getLinkResolver().resolveVirtualMethod(oop, "printStackTrace", "()V");
					Locals locals = ts.newLocals(printStackTrace);
					locals.set(0, oop);
					helper.invokeDirect(printStackTrace, locals);
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

			@Override
			protected MemoryAllocator createMemoryAllocator() {
				return new SynchronizedMemoryAllocator(super.createMemoryAllocator());
			}

			@Override
			protected MemoryManager createMemoryManager() {
				return new SynchronizedMemoryManager(super.createMemoryManager());
			}
		};
	}
}
