package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.jit.CodeInstaller;
import dev.xdark.ssvm.jit.CompiledData;
import dev.xdark.ssvm.jit.JitCompiler;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JitTest {

	@Test
	public void testJit() throws Exception {
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
		JavaMethod m = vm.getPublicLinkResolver().resolveStaticMethod(jc, "doMixTest", "()J");
		long test = doMixTest();
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			helper.invoke(m, ts);
		}
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			assertEquals(test, helper.invokeLong(m, ts));
		}
		CodeInstaller.ClassDefiner definer = new JitClassLoader();
		CodeInstaller.install(m, definer, JitCompiler.compile(m));
		{
			JavaMethod mix = vm.getPublicLinkResolver().resolveStaticMethod(jc, "mix", "(I)I");
			CodeInstaller.install(mix, definer, JitCompiler.compile(mix));
		}
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			assertEquals(test, helper.invokeLong(m, ts));
		}
		{
			JavaMethod call = vm.getPublicLinkResolver().resolveStaticMethod(jc, "doCallTest", "()V");
			CodeInstaller.install(call, definer, JitCompiler.compile(call));
			Locals ts = vm.getThreadStorage().newLocals(call);
			helper.invoke(call, ts);

		}
	}

	private static long doMixTest() {
		int x = 0;
		for (int i = 0; i < 1_00_000; i++) {
			x += mix(i);
		}
		return x;
	}

	private static int mix(int v) {
		return v * 2;
	}

	private static void doCallTest() {
		String s = new String("hello, world");
		if (!"hello, world".equals(s)) {
			throw new IllegalStateException();
		}
		Number number = 10507;
		if (!number.equals(Integer.valueOf(10507))) {
			throw new IllegalStateException();
		}
	}

	private static final class JitClassLoader extends ClassLoader
		implements CodeInstaller.ClassDefiner {

		@Override
		public Class<?> define(CompiledData jitClass) {
			byte[] code = jitClass.getBytecode();
			try {
				Files.write(Paths.get("Test.class"), code);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return defineClass(jitClass.getClassName().replace('/', '.'), code, 0, code.length);
		}
	}
}
