package dev.xdark.ssvm;

//<editor-fold desc="Imports">
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.rewrite.BooleanArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldByteProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldCharProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldDoubleProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldFloatProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldIntProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldLongProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldReferenceProcessor;
import dev.xdark.ssvm.execution.rewrite.GetFieldShortProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticByteProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticCharProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticDoubleProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticFloatProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticIntProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticLongProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticReferenceProcessor;
import dev.xdark.ssvm.execution.rewrite.GetStaticShortProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldByteProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldCharProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldDoubleProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldFloatProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldIntProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldLongProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldReferenceProcessor;
import dev.xdark.ssvm.execution.rewrite.PutFieldShortProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticByteProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticCharProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticDoubleProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticFloatProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticIntProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticLongProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticReferenceProcessor;
import dev.xdark.ssvm.execution.rewrite.PutStaticShortProcessor;
import dev.xdark.ssvm.execution.rewrite.ReferenceArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.ByteArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.CharArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.DoubleArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.DynamicCallProcessor;
import dev.xdark.ssvm.execution.rewrite.FloatArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.IntArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.LongArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.ShortArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.VMCastProcessor;
import dev.xdark.ssvm.execution.rewrite.VMInterfaceCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMNewProcessor;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.execution.rewrite.VMSpecialCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMStaticCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMVirtualCallProcessor;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.natives.*;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_BOOLEAN_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_BYTE_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CHAR_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CHECKCAST;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CONSTANT_DOUBLE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CONSTANT_FLOAT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CONSTANT_INT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CONSTANT_LONG;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_CONSTANT_REFERENCE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_DOUBLE_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_DYNAMIC_CALL;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_FLOAT_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_BYTE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_CHAR;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_DOUBLE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_FLOAT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_INT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_LONG;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_REFERENCE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_SHORT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_BYTE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_CHAR;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_DOUBLE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_FLOAT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_INT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_LONG;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_REFERENCE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETSTATIC_SHORT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_INT_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_INVOKEINTERFACE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_INVOKESPECIAL;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_INVOKESTATIC;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_INVOKEVIRTUAL;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_LONG_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_NEW;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_BYTE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_CHAR;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_DOUBLE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_FLOAT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_INT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_LONG;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_REFERENCE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_SHORT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_BYTE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_CHAR;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_DOUBLE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_FLOAT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_INT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_LONG;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_REFERENCE;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTSTATIC_SHORT;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_REFERENCE_NEW_ARRAY;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_SHORT_NEW_ARRAY;
import static org.objectweb.asm.Opcodes.*;
//</editor-fold>

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	// https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/classfile/javaClasses.cpp#L3180
	public static final String CLASS_LOADER_OOP = "classLoaderOop";
	public static final String VM_INDEX = "vmindex";
	public static final String VM_TARGET = "vmtarget";
	public static final String VM_HOLDER = "vmholder";
	public static final String PROTECTION_DOMAIN = "protectionDomain";

	/**
	 * Sets up VM instance.
	 *
	 * @param vm VM to set up.
	 */
	static void init(VirtualMachine vm) {
		//<editor-fold desc="Natives registration">
		setInstructions(vm);
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
		//</editor-fold>
	}

	/**
	 * Injects VM related things.
	 * This must be invoked as early as
	 * possible.
	 *
	 * @param vm VM instance.
	 */
	static void injectPhase1(VirtualMachine vm) {
		InstanceJavaClass cl = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
		List<FieldNode> fields = cl.getNode().fields;
		fields.add(new FieldNode(
			ACC_PRIVATE | ACC_VM_HIDDEN,
			PROTECTION_DOMAIN,
			"Ljava/security/ProtectionDomain;",
			null,
			null
		));
	}

	/**
	 * Injects VM related things.
	 *
	 * @param vm VM instance.
	 */
	static void injectPhase2(VirtualMachine vm) {
		//<editor-fold desc="Field injection">
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass classLoader = symbols.java_lang_ClassLoader();

		classLoader.getNode().fields.add(new FieldNode(
			ACC_PRIVATE | ACC_VM_HIDDEN,
			CLASS_LOADER_OOP,
			"Ljava/lang/Object;",
			null,
			null
		));

		vm.getInvokeDynamicLinker().setupMethodHandles();
		{
			InstanceJavaClass resolvedMethodName = symbols.java_lang_invoke_ResolvedMethodName();
			List<FieldNode> fields = resolvedMethodName.getNode().fields;
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
			InstanceJavaClass fd = symbols.java_io_FileDescriptor();
			// For whatever reason unix/macos does not have
			// 'handle' field, we need to inject it
			List<FieldNode> fields = fd.getNode().fields;
			for (int i = 0; i < fields.size(); i++) {
				FieldNode fn = fields.get(i);
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
		//</editor-fold>
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vm VM instance.
	 */
	private static void setInstructions(VirtualMachine vm) {
		//<editor-fold desc="VM instructions">
		VMInterface vmi = vm.getInterface();
		vmi.setProcessor(NOP, new NopProcessor());

		vmi.setProcessor(ACONST_NULL, new ConstantReferenceProcessor(vm.getMemoryManager().nullValue()));

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
		vmi.setProcessor(LDC, new dev.xdark.ssvm.execution.asm.LdcProcessor());

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

		vmi.setProcessor(IDIV, new IntDivisionProcessor());
		vmi.setProcessor(LDIV, new LongDivisionProcessor());
		vmi.setProcessor(FDIV, new BiFloatProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(DDIV, new BiDoubleProcessor((v1, v2) -> v1 / v2));

		vmi.setProcessor(IREM, new IntRemainderProcessor());
		vmi.setProcessor(LREM, new LongRemainderProcessor());
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
		vmi.setProcessor(ARETURN, new ReturnReferenceProcessor());
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
		vmi.setProcessor(ANEWARRAY, new ObjectArrayProcessor());
		vmi.setProcessor(NEWARRAY, new PrimitiveArrayProcessor());
		vmi.setProcessor(ARRAYLENGTH, new ArrayLengthProcessor());

		vmi.setProcessor(ATHROW, new ThrowProcessor());

		vmi.setProcessor(CHECKCAST, new CastProcessor());
		vmi.setProcessor(INSTANCEOF, new InstanceofProcessor());

		vmi.setProcessor(MONITORENTER, new MonitorEnterProcessor());
		vmi.setProcessor(MONITOREXIT, new MonitorExitProcessor());

		vmi.setProcessor(MULTIANEWARRAY, new MultiNewArrayProcessor());

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(ObjectValue::isNull));

		// VM opcodes
		vmi.setProcessor(VM_DYNAMIC_CALL, new DynamicCallProcessor());
		vmi.setProcessor(VM_NEW, new VMNewProcessor());
		vmi.setProcessor(VM_BOOLEAN_NEW_ARRAY, new BooleanArrayProcessor());
		vmi.setProcessor(VM_CHAR_NEW_ARRAY, new CharArrayProcessor());
		vmi.setProcessor(VM_FLOAT_NEW_ARRAY, new FloatArrayProcessor());
		vmi.setProcessor(VM_DOUBLE_NEW_ARRAY, new DoubleArrayProcessor());
		vmi.setProcessor(VM_BYTE_NEW_ARRAY, new ByteArrayProcessor());
		vmi.setProcessor(VM_SHORT_NEW_ARRAY, new ShortArrayProcessor());
		vmi.setProcessor(VM_INT_NEW_ARRAY, new IntArrayProcessor());
		vmi.setProcessor(VM_LONG_NEW_ARRAY, new LongArrayProcessor());
		vmi.setProcessor(VM_REFERENCE_NEW_ARRAY, new ReferenceArrayProcessor());
		vmi.setProcessor(VM_INVOKESTATIC, new VMStaticCallProcessor());
		vmi.setProcessor(VM_INVOKESPECIAL, new VMSpecialCallProcessor());
		vmi.setProcessor(VM_INVOKEVIRTUAL, new VMVirtualCallProcessor());
		vmi.setProcessor(VM_INVOKEINTERFACE, new VMInterfaceCallProcessor());
		vmi.setProcessor(VM_CHECKCAST, new VMCastProcessor());
		vmi.setProcessor(VM_GETSTATIC_BOOLEAN, new GetStaticByteProcessor());
		vmi.setProcessor(VM_GETSTATIC_CHAR, new GetStaticCharProcessor());
		vmi.setProcessor(VM_GETSTATIC_BYTE, new GetStaticByteProcessor());
		vmi.setProcessor(VM_GETSTATIC_SHORT, new GetStaticShortProcessor());
		vmi.setProcessor(VM_GETSTATIC_INT, new GetStaticIntProcessor());
		vmi.setProcessor(VM_GETSTATIC_FLOAT, new GetStaticFloatProcessor());
		vmi.setProcessor(VM_GETSTATIC_LONG, new GetStaticLongProcessor());
		vmi.setProcessor(VM_GETSTATIC_DOUBLE, new GetStaticDoubleProcessor());
		vmi.setProcessor(VM_GETSTATIC_REFERENCE, new GetStaticReferenceProcessor());
		vmi.setProcessor(VM_PUTSTATIC_BOOLEAN, new PutStaticByteProcessor());
		vmi.setProcessor(VM_PUTSTATIC_CHAR, new PutStaticCharProcessor());
		vmi.setProcessor(VM_PUTSTATIC_BYTE, new PutStaticByteProcessor());
		vmi.setProcessor(VM_PUTSTATIC_SHORT, new PutStaticShortProcessor());
		vmi.setProcessor(VM_PUTSTATIC_INT, new PutStaticIntProcessor());
		vmi.setProcessor(VM_PUTSTATIC_FLOAT, new PutStaticFloatProcessor());
		vmi.setProcessor(VM_PUTSTATIC_LONG, new PutStaticLongProcessor());
		vmi.setProcessor(VM_PUTSTATIC_DOUBLE, new PutStaticDoubleProcessor());
		vmi.setProcessor(VM_PUTSTATIC_REFERENCE, new PutStaticReferenceProcessor());
		vmi.setProcessor(VM_CONSTANT_INT, new dev.xdark.ssvm.execution.rewrite.ConstantIntProcessor());
		vmi.setProcessor(VM_CONSTANT_FLOAT, new dev.xdark.ssvm.execution.rewrite.ConstantFloatProcessor());
		vmi.setProcessor(VM_CONSTANT_LONG, new dev.xdark.ssvm.execution.rewrite.ConstantLongProcessor());
		vmi.setProcessor(VM_CONSTANT_DOUBLE, new dev.xdark.ssvm.execution.rewrite.ConstantDoubleProcessor());
		vmi.setProcessor(VM_CONSTANT_REFERENCE, new dev.xdark.ssvm.execution.rewrite.ConstantReferenceProcessor());

		vmi.setProcessor(VM_PUTFIELD_BOOLEAN, new PutFieldByteProcessor());
		vmi.setProcessor(VM_PUTFIELD_CHAR, new PutFieldCharProcessor());
		vmi.setProcessor(VM_PUTFIELD_BYTE, new PutFieldByteProcessor());
		vmi.setProcessor(VM_PUTFIELD_SHORT, new PutFieldShortProcessor());
		vmi.setProcessor(VM_PUTFIELD_INT, new PutFieldIntProcessor());
		vmi.setProcessor(VM_PUTFIELD_FLOAT, new PutFieldFloatProcessor());
		vmi.setProcessor(VM_PUTFIELD_LONG, new PutFieldLongProcessor());
		vmi.setProcessor(VM_PUTFIELD_DOUBLE, new PutFieldDoubleProcessor());
		vmi.setProcessor(VM_PUTFIELD_REFERENCE, new PutFieldReferenceProcessor());

		vmi.setProcessor(VM_GETFIELD_BOOLEAN, new GetFieldByteProcessor());
		vmi.setProcessor(VM_GETFIELD_CHAR, new GetFieldCharProcessor());
		vmi.setProcessor(VM_GETFIELD_BYTE, new GetFieldByteProcessor());
		vmi.setProcessor(VM_GETFIELD_SHORT, new GetFieldShortProcessor());
		vmi.setProcessor(VM_GETFIELD_INT, new GetFieldIntProcessor());
		vmi.setProcessor(VM_GETFIELD_FLOAT, new GetFieldFloatProcessor());
		vmi.setProcessor(VM_GETFIELD_LONG, new GetFieldLongProcessor());
		vmi.setProcessor(VM_GETFIELD_DOUBLE, new GetFieldDoubleProcessor());
		vmi.setProcessor(VM_GETFIELD_REFERENCE, new GetFieldReferenceProcessor());
		//</editor-fold>
	}
}
