package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.jit.CodeInstaller;
import dev.xdark.ssvm.jit.CompiledData;
import dev.xdark.ssvm.jit.JitCompiler;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
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
		VirtualMachine vm = new VirtualMachine();
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
		VMOperations ops = vm.getOperations();
		ObjectValue nullValue = vm.getMemoryManager().nullValue();
		InstanceClass jc = ops.defineClass(
			nullValue,
			null,
			bc, 0, bc.length,
			nullValue,
			"JVM_DefineClass",
			true
		);
		JavaMethod m = vm.getLinkResolver().resolveStaticMethod(jc, "doMixTest", "()J");
		long test = doMixTest();
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			ops.invokeVoid(m, ts);
		}
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			assertEquals(test, ops.invokeLong(m, ts));
		}
		CodeInstaller.ClassDefiner definer = new JitClassLoader();
		CodeInstaller.install(m, definer, JitCompiler.compile(m));
		{
			JavaMethod mix = vm.getLinkResolver().resolveStaticMethod(jc, "mix", "(I)I");
			CodeInstaller.install(mix, definer, JitCompiler.compile(mix));
		}
		{
			Locals ts = vm.getThreadStorage().newLocals(m);
			assertEquals(test, ops.invokeLong(m, ts));
		}
		{
			JavaMethod call = vm.getLinkResolver().resolveStaticMethod(jc, "doCallTest", "()V");
			CodeInstaller.install(call, definer, JitCompiler.compile(call));
			Locals ts = vm.getThreadStorage().newLocals(call);
			ops.invokeVoid(call, ts);

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
		Number number;
		if (!"hello, world".equals(s) || !(number = 10507).equals(Integer.valueOf(10507))) {
			throw new IllegalStateException();
		}
		number.intValue();
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
