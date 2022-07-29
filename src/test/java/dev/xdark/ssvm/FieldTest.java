package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
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
		(vm = new VirtualMachine()).initialize();
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
		VMOperations ops = vm.getPublicOperations();
		assertEquals(stringCst, vm.getHelper().readUtf8(ops.getReference(c, "string", "Ljava/lang/String;")));
		assertEquals(longCst, ops.getLong(c, "long"));
		assertEquals(doubleCst, ops.getDouble(c, "double"));
		assertEquals(intCst, ops.getInt(c, "int"));
		assertEquals(floatCst, ops.getFloat(c, "float"));
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

		VirtualMachine vm = FieldTest.vm;
		InstanceJavaClass c = TestUtil.createClass(vm, node);
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		VMHelper helper = vm.getHelper();
		VMOperations ops = vm.getPublicOperations();
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(c, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.set(0, instance);
		helper.invoke(init, locals);
		assertEquals(stringCst, vm.getHelper().readUtf8(instance.getValue("string", "Ljava/lang/String;")));
		assertEquals(longCst, ops.getLong(instance, c, "long"));
		assertEquals(doubleCst, ops.getDouble(instance, c, "double"));
		assertEquals(intCst, ops.getInt(instance, c, "int"));
		assertEquals(floatCst, ops.getFloat(instance, c, "float"));
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

		VirtualMachine vm = FieldTest.vm;
		InstanceJavaClass c = TestUtil.createClass(vm, node);
		VMOperations ops = vm.getPublicOperations();
		InstanceValue instance = vm.getMemoryManager().newInstance(c);
		VMHelper helper = vm.getHelper();
		JavaMethod init = vm.getPublicLinkResolver().resolveSpecialMethod(c, "<init>", "()V");
		Locals locals = vm.getThreadStorage().newLocals(init);
		locals.set(0, instance);
		helper.invoke(init, locals);
		assertEquals(staticStringCst, helper.readUtf8(ops.getReference(c, "string", "Ljava/lang/String;")));
		assertEquals(staticDouble, ops.getDouble(c, "double"));
		assertEquals(virtualStringCst, helper.readUtf8(ops.getReference(instance, "string1", "Ljava/lang/String;")));
		assertEquals(virtualFloat, ops.getFloat(instance, "float"));
	}
}
