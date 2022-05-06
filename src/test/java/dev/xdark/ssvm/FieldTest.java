package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class FieldTest {

	private static VirtualMachine vm;

	@BeforeAll
	private static void setup() {
		vm = new VirtualMachine();
	}

	@Test
	public void testClassFieldDefaults() {
		ClassNode node = new ClassNode();
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String stringCst = "Hello, World!";
		long longCst = rng.nextLong();
		double doubleCst = rng.nextDouble();
		int intCst = rng.nextInt();
		float floatCst = rng.nextFloat();
		node.visit(V11, ACC_PUBLIC, "Test", null, null, null);
		node.visitField(ACC_STATIC, "string", "Ljava/lang/String;", null, stringCst);
		node.visitField(ACC_STATIC, "long", "J", null, longCst);
		node.visitField(ACC_STATIC, "double", "D", null, doubleCst);
		node.visitField(ACC_STATIC, "int", "I", null, intCst);
		node.visitField(ACC_STATIC, "float", "F", null, floatCst);

		InstanceJavaClass c = TestUtil.createClass(vm, node);
		assertEquals(stringCst, vm.getHelper().readUtf8(c.getStaticValue("string", "Ljava/lang/String;")));
		assertEquals(longCst, c.getStaticValue("long", "J").asLong());
		assertEquals(doubleCst, c.getStaticValue("double", "D").asDouble());
		assertEquals(intCst, c.getStaticValue("int", "I").asInt());
		assertEquals(floatCst, c.getStaticValue("float", "F").asFloat());
	}

	@Test
	public void testVirtualFields() {
		ClassNode node = new ClassNode();
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String stringCst = "Hello, World!";
		long longCst = rng.nextLong();
		double doubleCst = rng.nextDouble();
		int intCst = rng.nextInt();
		float floatCst = rng.nextFloat();
		node.visit(V11, ACC_PUBLIC, "Test", null, "java/lang/Object", null);
		node.visitField(ACC_PRIVATE, "string", "Ljava/lang/String;", null, null);
		node.visitField(ACC_PRIVATE, "long", "J", null, null);
		node.visitField(ACC_PRIVATE, "double", "D", null, null);
		node.visitField(ACC_PRIVATE, "int", "I", null, null);
		node.visitField(ACC_PRIVATE, "float", "F", null, null);

		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(stringCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "string", "Ljava/lang/String;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(longCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "long", "J");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(doubleCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "double", "D");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(intCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "int", "I");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(floatCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "float", "F");
		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 1);

		InstanceJavaClass c = TestUtil.createClass(vm, node);
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		VMHelper helper = vm.getHelper();
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		assertEquals(stringCst, vm.getHelper().readUtf8(instance.getValue("string", "Ljava/lang/String;")));
		assertEquals(longCst, instance.getLong("long"));
		assertEquals(doubleCst, instance.getDouble("double"));
		assertEquals(intCst, instance.getInt("int"));
		assertEquals(floatCst, instance.getFloat("float"));
	}

	@Test
	public void testVirtualStaticFields() {
		ClassNode node = new ClassNode();
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String staticStringCst = "Hello, World!";
		String virtualStringCst = "Yet another Hello World!";
		double staticDouble = rng.nextDouble();
		float virtualFloat = rng.nextFloat();
		node.visit(V11, ACC_PUBLIC, "Test", null, "java/lang/Object", null);
		node.visitField(ACC_STATIC, "string", "Ljava/lang/String;", null, null);
		node.visitField(ACC_STATIC, "double", "D", null, null);
		node.visitField(ACC_PRIVATE, "string1", "Ljava/lang/String;", null, null);
		node.visitField(ACC_PRIVATE, "float", "F", null, null);
		MethodVisitor mv = node.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitLdcInsn(staticStringCst);
		mv.visitFieldInsn(PUTSTATIC, "Test", "string", "Ljava/lang/String;");
		mv.visitLdcInsn(staticDouble);
		mv.visitFieldInsn(PUTSTATIC, "Test", "double", "D");
		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 0);

		mv = node.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(virtualStringCst);
		mv.visitFieldInsn(PUTFIELD, "Test", "string1", "Ljava/lang/String;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(virtualFloat);
		mv.visitFieldInsn(PUTFIELD, "Test", "float", "F");
		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 1);

		InstanceJavaClass c = TestUtil.createClass(vm, node);
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		VMHelper helper = vm.getHelper();
		helper.invokeExact(c, "<init>", "()V", new Value[0], new Value[]{instance});
		assertEquals(staticStringCst, helper.readUtf8(c.getStaticValue("string", "Ljava/lang/String;")));
		assertEquals(staticDouble, c.getStaticValue("double", "D").asDouble());
		assertEquals(virtualStringCst, helper.readUtf8(instance.getValue("string1", "Ljava/lang/String;")));
		assertEquals(virtualFloat, instance.getFloat("float"));
	}
}
