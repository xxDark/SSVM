package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.natives.*;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.tree.FieldNode;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;
import static dev.xdark.ssvm.asm.VMOpcodes.DYNAMIC_CALL;
import static org.objectweb.asm.Opcodes.*;

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	public static final String CLASS_LOADER_OOP = "classLoaderOop";
	public static final String VM_INDEX = "vmindex";
	public static final String VM_TARGET = "vmtarget";
	public static final String VM_HOLDER = "vmholder";
	public static final String PROTECTION_DOMAIN = "protectionDomain";
	public static final String CONSTANT_POOL = "constantPool";

	/**
	 * Sets up VM instance.
	 *
	 * @param vm
	 * 		VM to set up.
	 */
	static void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		setInstructions(vmi);
		injectPhase2(vm);
		ClassNatives.init(vm);
		ObjectNatives.init(vm);
		SystemNatives.init(vm);
		ThreadNatives.init(vm);
		ClassLoaderNatives.init(vm);
		ThrowableNatives.init(vm);
		NativeLibraryNatives.init(vm);
		NativeLibraryNatives.init(vm);
		VMNatives.init(vm);
		UnsafeNatives.init(vm);
		RuntimeNatives.init(vm);
		DoubleNatives.init(vm);
		FloatNatives.init(vm);
		ArrayNatives.init(vm);
		ConstantPoolNatives.init(vm);
		GenericFileSystemNatives.init(vm);
		OldFileSystemNatives.init(vm);
		StackTraceElementNatives.init(vm);
		ReflectionNatives.init(vm);
		ConstructorAccessorNatives.init(vm);
		MethodAccessorNatives.init(vm);
		AccessControllerNatives.init(vm);
		MethodHandleNatives.init(vm);
		JigsawNatives.init(vm);
		SignalNatives.init(vm);
		StringNatives.init(vm);
		AtomicLongNatives.init(vm);
		URLClassPathNatives.init(vm);
		ZipFileNatives.init(vm);
		VMManagementNatives.init(vm);
		PackageNatives.init(vm);
		PerfNatives.init(vm);
		JarFileNatives.init(vm);
		MathNatives.init(vm);
		TimeZoneNatives.init(vm);
		CRC32Natives.init(vm);
		NativeSeedGeneratorNatives.init(vm);
		NetworkInterfaceNatives.init(vm);
		SeedGeneratorNatives.init(vm);
		ProxyNatives.init(vm);
		InflaterNatives.init(vm);
		ProcessEnvironmentNatives.init(vm);
		FileSystemNativeDispatcherNatives.init(vm);
		CDSNatives.init(vm);
		SystemPropsNatives.init(vm);
		ScopedMemoryAccessNatives.init(vm);
		ReferenceNatives.init(vm);
	}

	/**
	 * Injects VM related things.
	 * This must be invoked as early as
	 * possible.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	static void injectPhase1(VirtualMachine vm) {
		val cl = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
		val fields = cl.getNode().fields;
		fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				PROTECTION_DOMAIN,
				"Ljava/security/ProtectionDomain;",
				null,
				null
		));
		fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				CONSTANT_POOL,
				"Ljava/lang/Object;",
				null,
				null
		));
	}

	/**
	 * Injects VM related things.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	static void injectPhase2(VirtualMachine vm) {
		val symbols = vm.getSymbols();
		val classLoader = symbols.java_lang_ClassLoader;

		classLoader.getNode().fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				CLASS_LOADER_OOP,
				"Ljava/lang/Object;",
				null,
				null
		));

		inject:
		{
			val memberName = symbols.java_lang_invoke_MemberName;
			val fields = memberName.getNode().fields;
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_INDEX,
					"I",
					null,
					null
			));
			for (int i = 0; i < fields.size(); i++) {
				val fn = fields.get(i);
				if ("method".equals(fn.name) && "Ljava/lang/invoke/ResolvedMethodName;".equals(fn.desc)) {
					break inject;
				}
			}
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					"method",
					"Ljava/lang/invoke/ResolvedMethodName;",
					null,
					null
			));
		}

		{
			val resolvedMethodName = symbols.java_lang_invoke_ResolvedMethodName;
			val fields = resolvedMethodName.getNode().fields;
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_TARGET,
					"Ljava/lang/Object;",
					null,
					null
			));
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_HOLDER,
					"Ljava/lang/Object;",
					null,
					null
			));
		}
		inject:
		{
			val fd = symbols.java_io_FileDescriptor;
			// For whatever reason unix/macos does not have
			// 'handle' field, we need to inject it
			val fields = fd.getNode().fields;
			for (int i = 0; i < fields.size(); i++) {
				val fn = fields.get(i);
				if ("handle".equals(fn.name) && "J".equals(fn.desc)) {
					break inject;
				}
			}
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					"handle",
					"J",
					null,
					null
			));
		}
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vmi
	 * 		VM interface.
	 */
	private static void setInstructions(VMInterface vmi) {
		val nop = new NopProcessor();
		vmi.setProcessor(NOP, nop);

		vmi.setProcessor(ACONST_NULL, new ConstantProcessor(NullValue.INSTANCE));

		// ICONST_M1..INCONST_5
		for (int x = ICONST_M1; x <= ICONST_5; x++) {
			vmi.setProcessor(x, new ConstantIntProcessor(x - ICONST_0));
		}

		vmi.setProcessor(LCONST_0, new ConstantLongProcessor(0L));
		vmi.setProcessor(LCONST_1, new ConstantLongProcessor(1L));

		vmi.setProcessor(FCONST_0, new ConstantFloatProcessor(0.0F));
		vmi.setProcessor(FCONST_1, new ConstantFloatProcessor(1.0F));
		vmi.setProcessor(FCONST_2, new ConstantFloatProcessor(2.0F));

		vmi.setProcessor(DCONST_0, new ConstantDoubleProcessor(0.0D));
		vmi.setProcessor(DCONST_1, new ConstantDoubleProcessor(1.0D));

		vmi.setProcessor(BIPUSH, new BytePushProcessor());
		vmi.setProcessor(SIPUSH, new ShortPushProcessor());
		vmi.setProcessor(LDC, new LdcProcessor());

		vmi.setProcessor(LLOAD, new LongLoadProcessor());
		vmi.setProcessor(ILOAD, new IntLoadProcessor());
		vmi.setProcessor(FLOAD, new FloatLoadProcessor());
		vmi.setProcessor(DLOAD, new DoubleLoadProcessor());
		vmi.setProcessor(ALOAD, new ValueLoadProcessor());

		vmi.setProcessor(IALOAD, new LoadArrayIntProcessor());
		vmi.setProcessor(LALOAD, new LoadArrayLongProcessor());
		vmi.setProcessor(FALOAD, new LoadArrayFloatProcessor());
		vmi.setProcessor(DALOAD, new LoadArrayDoubleProcessor());
		vmi.setProcessor(AALOAD, new LoadArrayValueProcessor());
		vmi.setProcessor(BALOAD, new LoadArrayByteProcessor());
		vmi.setProcessor(CALOAD, new LoadArrayCharProcessor());
		vmi.setProcessor(SALOAD, new LoadArrayShortProcessor());

		vmi.setProcessor(IASTORE, new StoreArrayIntProcessor());
		vmi.setProcessor(LASTORE, new StoreArrayLongProcessor());
		vmi.setProcessor(FASTORE, new StoreArrayFloatProcessor());
		vmi.setProcessor(DASTORE, new StoreArrayDoubleProcessor());
		vmi.setProcessor(AASTORE, new StoreArrayValueProcessor());
		vmi.setProcessor(BASTORE, new StoreArrayByteProcessor());
		vmi.setProcessor(CASTORE, new StoreArrayCharProcessor());
		vmi.setProcessor(SASTORE, new StoreArrayShortProcessor());

		vmi.setProcessor(ISTORE, new IntStoreProcessor());
		vmi.setProcessor(LSTORE, new LongStoreProcessor());
		vmi.setProcessor(FSTORE, new FloatStoreProcessor());
		vmi.setProcessor(DSTORE, new DoubleStoreProcessor());
		vmi.setProcessor(ASTORE, new ValueStoreProcessor());

		vmi.setProcessor(POP, new PopProcessor());
		vmi.setProcessor(POP2, new Pop2Processor());
		vmi.setProcessor(DUP, new DupProcessor());
		vmi.setProcessor(DUP_X1, new DupX1Processor());
		vmi.setProcessor(DUP_X2, new DupX2Processor());
		vmi.setProcessor(DUP2, new Dup2Processor());
		vmi.setProcessor(DUP2_X1, new Dup2X1Processor());
		vmi.setProcessor(DUP2_X2, new Dup2X2Processor());
		vmi.setProcessor(SWAP, new SwapProcessor());

		vmi.setProcessor(IADD, new BiIntProcessor(Integer::sum));
		vmi.setProcessor(LADD, new BiLongProcessor(Long::sum));
		vmi.setProcessor(FADD, new BiFloatProcessor(Float::sum));
		vmi.setProcessor(DADD, new BiDoubleProcessor(Double::sum));

		vmi.setProcessor(ISUB, new BiIntProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(LSUB, new BiLongProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(FSUB, new BiFloatProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(DSUB, new BiDoubleProcessor((v1, v2) -> v1 - v2));

		vmi.setProcessor(IMUL, new BiIntProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(LMUL, new BiLongProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(FMUL, new BiFloatProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(DMUL, new BiDoubleProcessor((v1, v2) -> v1 * v2));

		vmi.setProcessor(IDIV, new BiIntProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(LDIV, new BiLongProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(FDIV, new BiFloatProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(DDIV, new BiDoubleProcessor((v1, v2) -> v1 / v2));

		vmi.setProcessor(IREM, new BiIntProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(LREM, new BiLongProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(FREM, new BiFloatProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(DREM, new BiDoubleProcessor((v1, v2) -> v1 % v2));

		vmi.setProcessor(INEG, new NegativeIntProcessor());
		vmi.setProcessor(LNEG, new NegativeLongProcessor());
		vmi.setProcessor(FNEG, new NegativeFloatProcessor());
		vmi.setProcessor(DNEG, new NegativeDoubleProcessor());

		vmi.setProcessor(ISHL, new BiIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(LSHL, new LongIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(ISHR, new BiIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(LSHR, new LongIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(IUSHR, new BiIntProcessor((v1, v2) -> v1 >>> v2));
		vmi.setProcessor(LUSHR, new LongIntProcessor((v1, v2) -> v1 >>> v2));

		vmi.setProcessor(IAND, new BiIntProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(LAND, new BiLongProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(IOR, new BiIntProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(LOR, new BiLongProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(IXOR, new BiIntProcessor((v1, v2) -> v1 ^ v2));
		vmi.setProcessor(LXOR, new BiLongProcessor((v1, v2) -> v1 ^ v2));

		vmi.setProcessor(IINC, new VariableIncrementProcessor());

		vmi.setProcessor(I2L, new IntToLongProcessor());
		vmi.setProcessor(I2F, new IntToFloatProcessor());
		vmi.setProcessor(I2D, new IntToDoubleProcessor());
		vmi.setProcessor(L2I, new LongToIntProcessor());
		vmi.setProcessor(L2F, new LongToFloatProcessor());
		vmi.setProcessor(L2D, new LongToDoubleProcessor());
		vmi.setProcessor(F2I, new FloatToIntProcessor());
		vmi.setProcessor(F2L, new FloatToLongProcessor());
		vmi.setProcessor(F2D, new FloatToDoubleProcessor());
		vmi.setProcessor(D2I, new DoubleToIntProcessor());
		vmi.setProcessor(D2L, new DoubleToLongProcessor());
		vmi.setProcessor(D2F, new DoubleToFloatProcessor());
		vmi.setProcessor(I2B, new IntToByteProcessor());
		vmi.setProcessor(I2C, new IntToCharProcessor());
		vmi.setProcessor(I2S, new IntToShortProcessor());

		vmi.setProcessor(LCMP, new LongCompareProcessor());
		vmi.setProcessor(FCMPL, new FloatCompareProcessor(-1));
		vmi.setProcessor(FCMPG, new FloatCompareProcessor(1));
		vmi.setProcessor(DCMPL, new DoubleCompareProcessor(-1));
		vmi.setProcessor(DCMPG, new DoubleCompareProcessor(1));

		vmi.setProcessor(IFEQ, new IntJumpProcessor(value -> value == 0));
		vmi.setProcessor(IFNE, new IntJumpProcessor(value -> value != 0));
		vmi.setProcessor(IFLT, new IntJumpProcessor(value -> value < 0));
		vmi.setProcessor(IFGE, new IntJumpProcessor(value -> value >= 0));
		vmi.setProcessor(IFGT, new IntJumpProcessor(value -> value > 0));
		vmi.setProcessor(IFLE, new IntJumpProcessor(value -> value <= 0));

		vmi.setProcessor(IF_ICMPEQ, new BiIntJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ICMPNE, new BiIntJumpProcessor((v1, v2) -> v1 != v2));
		vmi.setProcessor(IF_ICMPLT, new BiIntJumpProcessor((v1, v2) -> v1 < v2));
		vmi.setProcessor(IF_ICMPGE, new BiIntJumpProcessor((v1, v2) -> v1 >= v2));
		vmi.setProcessor(IF_ICMPGT, new BiIntJumpProcessor((v1, v2) -> v1 > v2));
		vmi.setProcessor(IF_ICMPLE, new BiIntJumpProcessor((v1, v2) -> v1 <= v2));

		vmi.setProcessor(IF_ACMPEQ, new BiValueJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ACMPNE, new BiValueJumpProcessor((v1, v2) -> v1 != v2));

		vmi.setProcessor(GOTO, new GotoProcessor());

		vmi.setProcessor(JSR, new JSRProcessor());
		vmi.setProcessor(RET, new RetProcessor());

		vmi.setProcessor(TABLESWITCH, new TableSwitchProcessor());
		vmi.setProcessor(LOOKUPSWITCH, new LookupSwitchProcessor());

		vmi.setProcessor(IRETURN, new ReturnIntProcessor());
		vmi.setProcessor(LRETURN, new ReturnLongProcessor());
		vmi.setProcessor(FRETURN, new ReturnFloatProcessor());
		vmi.setProcessor(DRETURN, new ReturnDoubleProcessor());
		vmi.setProcessor(ARETURN, new ReturnValueProcessor());
		vmi.setProcessor(RETURN, new ReturnVoidProcessor());

		vmi.setProcessor(GETSTATIC, new GetStaticProcessor());
		vmi.setProcessor(PUTSTATIC, new PutStaticProcessor());
		vmi.setProcessor(GETFIELD, new GetFieldProcessor());
		vmi.setProcessor(PUTFIELD, new PutFieldProcessor());
		vmi.setProcessor(INVOKEVIRTUAL, new VirtualCallProcessor());
		vmi.setProcessor(INVOKESPECIAL, new SpecialCallProcessor());
		vmi.setProcessor(INVOKESTATIC, new StaticCallProcessor());
		vmi.setProcessor(INVOKEINTERFACE, new InterfaceCallProcessor());

		vmi.setProcessor(INVOKEDYNAMIC, new InvokeDynamicLinkerProcessor());
		vmi.setProcessor(NEW, new NewProcessor());
		vmi.setProcessor(ANEWARRAY, new InstanceArrayProcessor());
		vmi.setProcessor(NEWARRAY, new PrimitiveArrayProcessor());
		vmi.setProcessor(ARRAYLENGTH, new ArrayLengthProcessor());

		vmi.setProcessor(ATHROW, new ThrowProcessor());

		vmi.setProcessor(CHECKCAST, new CastProcessor());
		vmi.setProcessor(INSTANCEOF, new InstanceofProcessor());

		vmi.setProcessor(MONITORENTER, new MonitorEnterProcessor());
		vmi.setProcessor(MONITOREXIT, new MonitorExitProcessor());

		vmi.setProcessor(MULTIANEWARRAY, new MultiNewArrayProcessor());

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(Value::isNull));

		// VM opcodes
		vmi.setProcessor(DYNAMIC_CALL, new DynamicCallProcessor());
		vmi.setProcessor(VMOpcodes.LDC, new VMLdcProcessor());
	}
}
