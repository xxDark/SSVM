package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.jit.JitClass;
import dev.xdark.ssvm.jit.JitCompiler;
import dev.xdark.ssvm.jit.JitInstaller;
import dev.xdark.ssvm.value.*;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JitTest {

	private static long a;
	private static int b;
	private static String c;
	private static long d;
	private static int e;

	@Test
	public void testJit() throws IOException {
		val vm = new VirtualMachine() {
			@Override
			protected FileDescriptorManager createFileDescriptorManager() {
				return new HostFileDescriptorManager();
			}
		};
		vm.bootstrap();
		val baos = new ByteArrayOutputStream();
		try (val in = JitTest.class.getClassLoader().getResourceAsStream(Type.getInternalName(JitTest.class) + ".class")) {
			val buf = new byte[512];
			int r;
			while ((r = in.read(buf)) != -1) {
				baos.write(buf, 0, r);
			}
		}
		val bc = baos.toByteArray();
		val helper = vm.getHelper();
		val jc = helper.defineClass(
				NullValue.INSTANCE,
				null,
				bc, 0, bc.length,
				NullValue.INSTANCE,
				"JVM_DefineClass"
		);
		val rng = ThreadLocalRandom.current();
		a = rng.nextLong();
		b = rng.nextInt();
		c = Long.toBinaryString(rng.nextLong());
		d = rng.nextLong();
		e = rng.nextInt();
		val m = jc.getStaticMethod("jitCall", "(JILjava/lang/String;JI)V");
		// Force compile
		val loader = new JitClassLoader();
		try {
			for (val toCompile : jc.getStaticMethodLayout().getAll()) {
				val compiled = JitCompiler.compile(toCompile, 3);
				JitInstaller.install(toCompile, loader, compiled);
			}
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
		try {
			helper.invokeStatic(jc, m, new Value[0], new Value[]{
				LongValue.of(a),
				TopValue.INSTANCE,
				IntValue.of(b),
				helper.newUtf8(c),
				LongValue.of(d),
				TopValue.INSTANCE,
				IntValue.of(e)
			});
		} catch (VMException ex) {
			helper.invokeVirtual("printStackTrace", "()V", new Value[0], new Value[]{ex.getOop()});
			throw ex;
		}
		assertEquals(a, jc.getStaticValue("a", "J").asLong());
		assertEquals(b, jc.getStaticValue("b", "I").asInt());
		assertEquals(c, helper.readUtf8(jc.getStaticValue("c", "Ljava/lang/String;")));
		assertEquals(d, jc.getStaticValue("d", "J").asLong());
		assertEquals(e, jc.getStaticValue("e", "I").asInt());
	}

	private static void jitCall(long a, int b, String c, long d, int e) {
		jitCallInner(a, b, c, d, e);
		testThrowInInvokeDynamic();
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

	private static final class JitClassLoader extends ClassLoader
			implements JitInstaller.ClassDefiner {

		@Override
		public Class<?> define(JitClass jitClass) {
			val code = jitClass.getCode();
			return defineClass(jitClass.getClassName().replace('/', '.'), code, 0, code.length);
		}
	}
}
