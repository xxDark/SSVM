package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.jit.JitClass;
import dev.xdark.ssvm.jit.JitCompiler;
import dev.xdark.ssvm.jit.JitInstaller;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class JitTest {

	private static long a;
	private static int b;
	private static String c;
	private static long d;
	private static int e;

	@Test
	public void testJit() throws IOException {
		VirtualMachine vm = new VirtualMachine() {
			@Override
			protected FileDescriptorManager createFileDescriptorManager() {
				return new HostFileDescriptorManager();
			}
		};
		vm.bootstrap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream in = JitTest.class.getClassLoader().getResourceAsStream(Type.getInternalName(JitTest.class) + ".class")) {
			byte[] buf = new byte[512];
			int r;
			while ((r = in.read(buf)) != -1) {
				baos.write(buf, 0, r);
			}
		}
		byte[] bc = baos.toByteArray();
		VMHelper helper = vm.getHelper();
		ObjectValue nullValue = vm.getMemoryManager().nullValue();
		InstanceJavaClass jc = helper.defineClass(
			nullValue,
			null,
			bc, 0, bc.length,
			nullValue,
			"JVM_DefineClass"
		);
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		a = rng.nextLong();
		b = rng.nextInt();
		c = Long.toBinaryString(rng.nextLong());
		d = rng.nextLong();
		e = rng.nextInt();
		JavaMethod m = jc.getStaticMethod("jitCall", "(JILjava/lang/String;JI)V");
		// Force compile
		JitClassLoader loader = new JitClassLoader();
		try {
			for (JavaMethod toCompile : jc.getStaticMethodLayout().getAll()) {
				if ("testJit".equals(toCompile.getName())) {
					continue;
				}
				JitClass compiled = JitCompiler.compile(toCompile, 3);
				JitInstaller.install(toCompile, loader, compiled);
			}
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
		try {
			Locals locals =vm.getThreadStorage().newLocals(m);
			locals.setLong(0, a);
			locals.setInt(2, b);
			locals.setReference(3, helper.newUtf8(c));
			locals.setLong(4, d);
			locals.setInt(6, e);
			helper.invoke(m, locals);
		} catch (VMException ex) {
			InstanceValue oop = ex.getOop();
			JavaMethod printStackTrace = vm.getPublicLinkResolver().resolveVirtualMethod(oop, "printStackTrace", "()V");
			Locals locals = vm.getThreadStorage().newLocals(printStackTrace);
			locals.setReference(0, oop);
			helper.invoke(printStackTrace, locals);
			throw ex;
		}
		VMOperations ops = vm.getPublicOperations();
		assertEquals(a, ops.getLong(jc, "a"));
		assertEquals(b, ops.getInt(jc, "b"));
		assertEquals(c, helper.readUtf8(ops.getReference(jc, "c", "Ljava/lang/String;")));
		assertEquals(d, ops.getLong(jc, "d"));
		assertEquals(e, ops.getInt(jc, "e"));
	}

	private static void jitCall(long a, int b, String c, long d, int e) {
		jitCallInner(a, b, c, d, e);
		testThrowInInvokeDynamic();
		testThrow();
		testMultiArray();
	}

	private static void jitCallInner(long a, int b, String c, long d, int e) {
		JitTest.a = a;
		JitTest.b = b;
		JitTest.c = c;
		JitTest.d = d;
		JitTest.e = e;
	}

	private static void testThrowInInvokeDynamic() {
		try {
			Runnable r = () -> {
				throw new IllegalArgumentException();
			};
			r.run();
			throw new IllegalStateException();
		} catch (IllegalArgumentException ignored) {
		}
	}

	private static void testThrow() {
		try {
			throwInner();
		} catch (IllegalStateException | IllegalArgumentException ignored) {
		}
	}

	private static void throwInner() {
		throw new IllegalArgumentException();
	}

	private static void testMultiArray() {
		int[][][] array = new int[1][1][1];
		array[0] = new int[1][1];
		array[0][0] = new int[1];
		array[0][0][0] = 5;
		if (array[0][0][0] != 5) {
			throw new IllegalStateException();
		}
	}

	private static final class JitClassLoader extends ClassLoader
		implements JitInstaller.ClassDefiner {

		@Override
		public Class<?> define(JitClass jitClass) {
			byte[] code = jitClass.getCode();
			return defineClass(jitClass.getClassName().replace('/', '.'), code, 0, code.length);
		}
	}
}
