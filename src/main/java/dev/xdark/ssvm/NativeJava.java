package dev.xdark.ssvm;

//<editor-fold desc="Imports">

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.asm.ArrayLengthProcessor;
import dev.xdark.ssvm.execution.asm.BiDoubleProcessor;
import dev.xdark.ssvm.execution.asm.BiFloatProcessor;
import dev.xdark.ssvm.execution.asm.BiIntJumpProcessor;
import dev.xdark.ssvm.execution.asm.BiIntProcessor;
import dev.xdark.ssvm.execution.asm.BiLongProcessor;
import dev.xdark.ssvm.execution.asm.BiValueJumpProcessor;
import dev.xdark.ssvm.execution.asm.BytePushProcessor;
import dev.xdark.ssvm.execution.asm.CastProcessor;
import dev.xdark.ssvm.execution.asm.ConstantDoubleProcessor;
import dev.xdark.ssvm.execution.asm.ConstantFloatProcessor;
import dev.xdark.ssvm.execution.asm.ConstantIntProcessor;
import dev.xdark.ssvm.execution.asm.ConstantLongProcessor;
import dev.xdark.ssvm.execution.asm.ConstantReferenceProcessor;
import dev.xdark.ssvm.execution.asm.DoubleCompareProcessor;
import dev.xdark.ssvm.execution.asm.DoubleLoadProcessor;
import dev.xdark.ssvm.execution.asm.DoubleStoreProcessor;
import dev.xdark.ssvm.execution.asm.DoubleToFloatProcessor;
import dev.xdark.ssvm.execution.asm.DoubleToIntProcessor;
import dev.xdark.ssvm.execution.asm.DoubleToLongProcessor;
import dev.xdark.ssvm.execution.asm.Dup2Processor;
import dev.xdark.ssvm.execution.asm.Dup2X1Processor;
import dev.xdark.ssvm.execution.asm.Dup2X2Processor;
import dev.xdark.ssvm.execution.asm.DupProcessor;
import dev.xdark.ssvm.execution.asm.DupX1Processor;
import dev.xdark.ssvm.execution.asm.DupX2Processor;
import dev.xdark.ssvm.execution.asm.FloatCompareProcessor;
import dev.xdark.ssvm.execution.asm.FloatLoadProcessor;
import dev.xdark.ssvm.execution.asm.FloatStoreProcessor;
import dev.xdark.ssvm.execution.asm.FloatToDoubleProcessor;
import dev.xdark.ssvm.execution.asm.FloatToIntProcessor;
import dev.xdark.ssvm.execution.asm.FloatToLongProcessor;
import dev.xdark.ssvm.execution.asm.GetFieldProcessor;
import dev.xdark.ssvm.execution.asm.GetStaticProcessor;
import dev.xdark.ssvm.execution.asm.GotoProcessor;
import dev.xdark.ssvm.execution.asm.InstanceofProcessor;
import dev.xdark.ssvm.execution.asm.IntDivisionProcessor;
import dev.xdark.ssvm.execution.asm.IntJumpProcessor;
import dev.xdark.ssvm.execution.asm.IntLoadProcessor;
import dev.xdark.ssvm.execution.asm.IntRemainderProcessor;
import dev.xdark.ssvm.execution.asm.IntStoreProcessor;
import dev.xdark.ssvm.execution.asm.IntToByteProcessor;
import dev.xdark.ssvm.execution.asm.IntToCharProcessor;
import dev.xdark.ssvm.execution.asm.IntToDoubleProcessor;
import dev.xdark.ssvm.execution.asm.IntToFloatProcessor;
import dev.xdark.ssvm.execution.asm.IntToLongProcessor;
import dev.xdark.ssvm.execution.asm.IntToShortProcessor;
import dev.xdark.ssvm.execution.asm.InterfaceCallProcessor;
import dev.xdark.ssvm.execution.asm.InvokeDynamicLinkerProcessor;
import dev.xdark.ssvm.execution.asm.JSRProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayByteProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayCharProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayDoubleProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayFloatProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayIntProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayLongProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayShortProcessor;
import dev.xdark.ssvm.execution.asm.LoadArrayValueProcessor;
import dev.xdark.ssvm.execution.asm.LongCompareProcessor;
import dev.xdark.ssvm.execution.asm.LongDivisionProcessor;
import dev.xdark.ssvm.execution.asm.LongIntProcessor;
import dev.xdark.ssvm.execution.asm.LongLoadProcessor;
import dev.xdark.ssvm.execution.asm.LongRemainderProcessor;
import dev.xdark.ssvm.execution.asm.LongStoreProcessor;
import dev.xdark.ssvm.execution.asm.LongToDoubleProcessor;
import dev.xdark.ssvm.execution.asm.LongToFloatProcessor;
import dev.xdark.ssvm.execution.asm.LongToIntProcessor;
import dev.xdark.ssvm.execution.asm.LookupSwitchProcessor;
import dev.xdark.ssvm.execution.asm.MonitorEnterProcessor;
import dev.xdark.ssvm.execution.asm.MonitorExitProcessor;
import dev.xdark.ssvm.execution.asm.MultiNewArrayProcessor;
import dev.xdark.ssvm.execution.asm.NegativeDoubleProcessor;
import dev.xdark.ssvm.execution.asm.NegativeFloatProcessor;
import dev.xdark.ssvm.execution.asm.NegativeIntProcessor;
import dev.xdark.ssvm.execution.asm.NegativeLongProcessor;
import dev.xdark.ssvm.execution.asm.NewProcessor;
import dev.xdark.ssvm.execution.asm.NopProcessor;
import dev.xdark.ssvm.execution.asm.ObjectArrayProcessor;
import dev.xdark.ssvm.execution.asm.Pop2Processor;
import dev.xdark.ssvm.execution.asm.PopProcessor;
import dev.xdark.ssvm.execution.asm.PrimitiveArrayProcessor;
import dev.xdark.ssvm.execution.asm.PutFieldProcessor;
import dev.xdark.ssvm.execution.asm.PutStaticProcessor;
import dev.xdark.ssvm.execution.asm.ReferenceStoreProcessor;
import dev.xdark.ssvm.execution.asm.RetProcessor;
import dev.xdark.ssvm.execution.asm.ReturnDoubleProcessor;
import dev.xdark.ssvm.execution.asm.ReturnFloatProcessor;
import dev.xdark.ssvm.execution.asm.ReturnIntProcessor;
import dev.xdark.ssvm.execution.asm.ReturnLongProcessor;
import dev.xdark.ssvm.execution.asm.ReturnReferenceProcessor;
import dev.xdark.ssvm.execution.asm.ReturnVoidProcessor;
import dev.xdark.ssvm.execution.asm.ShortPushProcessor;
import dev.xdark.ssvm.execution.asm.SpecialCallProcessor;
import dev.xdark.ssvm.execution.asm.StaticCallProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayByteProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayCharProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayDoubleProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayFloatProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayIntProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayLongProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayShortProcessor;
import dev.xdark.ssvm.execution.asm.StoreArrayValueProcessor;
import dev.xdark.ssvm.execution.asm.SwapProcessor;
import dev.xdark.ssvm.execution.asm.TableSwitchProcessor;
import dev.xdark.ssvm.execution.asm.ThrowProcessor;
import dev.xdark.ssvm.execution.asm.ValueJumpProcessor;
import dev.xdark.ssvm.execution.asm.ValueLoadProcessor;
import dev.xdark.ssvm.execution.asm.VariableIncrementProcessor;
import dev.xdark.ssvm.execution.asm.VirtualCallProcessor;
import dev.xdark.ssvm.execution.rewrite.BooleanArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.ByteArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.CharArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.DoubleArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.DynamicCallProcessor;
import dev.xdark.ssvm.execution.rewrite.FloatArrayProcessor;
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
import dev.xdark.ssvm.execution.rewrite.IntArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.LongArrayProcessor;
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
import dev.xdark.ssvm.execution.rewrite.ShortArrayProcessor;
import dev.xdark.ssvm.execution.rewrite.VMCastProcessor;
import dev.xdark.ssvm.execution.rewrite.VMInterfaceCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMNewProcessor;
import dev.xdark.ssvm.execution.rewrite.VMSpecialCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMStaticCallProcessor;
import dev.xdark.ssvm.execution.rewrite.VMVirtualCallProcessor;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.natives.AccessControllerNatives;
import dev.xdark.ssvm.natives.ArrayNatives;
import dev.xdark.ssvm.natives.AtomicLongNatives;
import dev.xdark.ssvm.natives.CDSNatives;
import dev.xdark.ssvm.natives.CRC32Natives;
import dev.xdark.ssvm.natives.ClassLoaderNatives;
import dev.xdark.ssvm.natives.ClassNatives;
import dev.xdark.ssvm.natives.ConstantPoolNatives;
import dev.xdark.ssvm.natives.ConstructorAccessorNatives;
import dev.xdark.ssvm.natives.DoubleNatives;
import dev.xdark.ssvm.natives.FileSystemNativeDispatcherNatives;
import dev.xdark.ssvm.natives.FloatNatives;
import dev.xdark.ssvm.natives.GenericFileSystemNatives;
import dev.xdark.ssvm.natives.InflaterNatives;
import dev.xdark.ssvm.natives.JarFileNatives;
import dev.xdark.ssvm.natives.JigsawNatives;
import dev.xdark.ssvm.natives.MathNatives;
import dev.xdark.ssvm.natives.MethodAccessorNatives;
import dev.xdark.ssvm.natives.MethodHandleNatives;
import dev.xdark.ssvm.natives.NativeLibraryNatives;
import dev.xdark.ssvm.natives.NativeSeedGeneratorNatives;
import dev.xdark.ssvm.natives.NetworkInterfaceNatives;
import dev.xdark.ssvm.natives.ObjectNatives;
import dev.xdark.ssvm.natives.OldFileSystemNatives;
import dev.xdark.ssvm.natives.PackageNatives;
import dev.xdark.ssvm.natives.PerfNatives;
import dev.xdark.ssvm.natives.ProcessEnvironmentNatives;
import dev.xdark.ssvm.natives.ProxyNatives;
import dev.xdark.ssvm.natives.ReferenceNatives;
import dev.xdark.ssvm.natives.ReflectionNatives;
import dev.xdark.ssvm.natives.RuntimeNatives;
import dev.xdark.ssvm.natives.ScopedMemoryAccessNatives;
import dev.xdark.ssvm.natives.SeedGeneratorNatives;
import dev.xdark.ssvm.natives.SignalNatives;
import dev.xdark.ssvm.natives.StackTraceElementNatives;
import dev.xdark.ssvm.natives.StringNatives;
import dev.xdark.ssvm.natives.SystemNatives;
import dev.xdark.ssvm.natives.SystemPropsNatives;
import dev.xdark.ssvm.natives.ThreadNatives;
import dev.xdark.ssvm.natives.ThrowableNatives;
import dev.xdark.ssvm.natives.TimeZoneNatives;
import dev.xdark.ssvm.natives.URLClassPathNatives;
import dev.xdark.ssvm.natives.UnsafeNatives;
import dev.xdark.ssvm.natives.VMManagementNatives;
import dev.xdark.ssvm.natives.VMNatives;
import dev.xdark.ssvm.natives.ZipFileNatives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.util.CloseableLock;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CALOAD;
import static org.objectweb.asm.Opcodes.CASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DCMPG;
import static org.objectweb.asm.Opcodes.DCMPL;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DNEG;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.F2I;
import static org.objectweb.asm.Opcodes.F2L;
import static org.objectweb.asm.Opcodes.FADD;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.FCMPG;
import static org.objectweb.asm.Opcodes.FCMPL;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FCONST_1;
import static org.objectweb.asm.Opcodes.FCONST_2;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.FNEG;
import static org.objectweb.asm.Opcodes.FREM;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.FSUB;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.IINC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.JSR;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LOOKUPSWITCH;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.MULTIANEWARRAY;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RET;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.TABLESWITCH;
//</editor-fold>

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	/**
	 * Sets up VM instance.
	 *
	 * @param vm VM to set up.
	 */
	static void initialization(VirtualMachine vm) {
		//<editor-fold desc="Natives registration">
		setInstructions(vm);
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
	 * Sets up JVMTI hooks.
	 *
	 * @param vm VM to set JVMTI hooks for.
	 */
	static void jvmtiPrepare(VirtualMachine vm) {
		{
			JVMTIEnv env = vm.newJvmtiEnv();
			Map<String, Consumer<InstanceClass>> map = new HashMap<>();
			map.put("java/lang/Class", klass -> {
				List<FieldNode> fields = klass.getNode().fields;
				fields.add(InjectedClassLayout.java_lang_Class_id.newNode());
				fields.add(InjectedClassLayout.java_lang_Class_protectionDomain.newNode());
			});
			map.put("java/lang/ClassLoader", klass -> {
				List<FieldNode> fields = klass.getNode().fields;
				fields.add(InjectedClassLayout.java_lang_ClassLoader_oop.newNode());
			});
			map.put("java/io/FileDescriptor", klass -> {
				List<FieldNode> fields = klass.getNode().fields;
				for (int i = 0; i < fields.size(); i++) {
					FieldNode fn = fields.get(i);
					if ("handle".equals(fn.name) && "J".equals(fn.desc)) {
						return;
					}
				}
				fields.add(InjectedClassLayout.java_io_FileDescriptor_handle.newNode());
			});
			map.put("java/lang/invoke/MemberName", klass -> {
				List<FieldNode> fields = klass.getNode().fields;
				fields.add(InjectedClassLayout.java_lang_invoke_MemberName_vmindex.newNode());
				for (int i = 0; i < fields.size(); i++) {
					FieldNode fn = fields.get(i);
					if ("method".equals(fn.name) && "Ljava/lang/invoke/ResolvedMethodName;".equals(fn.desc)) {
						return;
					}
				}
				fields.add(InjectedClassLayout.java_lang_invoke_MemberName_method.newNode());
			});
			map.put("java/lang/invoke/ResolvedMethodName", klass -> {
				List<FieldNode> fields = klass.getNode().fields;
				fields.add(InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.newNode());
				fields.add(InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.newNode());
			});
			env.setClassPrepare(klass -> {
				String name = klass.getInternalName();
				Consumer<InstanceClass> c = map.remove(name);
				if (c != null) {
					c.accept(klass);
					if (map.isEmpty()) {
						env.close();
					}
				}
			});
		}
	}

	static void postInitialization(VirtualMachine vm) {
		// Post initialization
		Symbols symbols = vm.getSymbols();
		InstanceClass throwable = symbols.java_lang_Throwable();
		InstanceClass methodAccessorImpl = symbols.reflect_MethodAccessorImpl();
		InstanceClass methodHandle = symbols.java_lang_invoke_MethodHandle();
		ClassLoaderData data = vm.getClassLoaders().getClassLoaderData(vm.getMemoryManager().nullValue());
		JVMTIEnv env = vm.newJvmtiEnv();
		ClassLink link = klass -> {
			if (throwable.isAssignableFrom(klass)) {
				// Hide Throwable constructors:
				hideThrowableMethods(klass);
			} else if (klass.getClassLoader().isNull()) {
				fixCallerSensitive(klass);
				if (methodAccessorImpl.isAssignableFrom(klass)) {
					// Fix MethodAccessorImpl:
					hideMethodAccessorImpl(klass);
				} else {
					String name = klass.getInternalName();
					if (name.startsWith("java/lang/invoke/")) {
						// Fix MethodHandles API:
						hideLambdaForm(klass);
						if (methodHandle == klass) {
							// Fix invokeXX:
							hideInvokeXX(klass);
							// Fix invokeWithArguments:
							callerSensitive(klass.getMethod("invokeWithArguments", "(Ljava/util/List;)Ljava/lang/Object;"));
						}
					}
				}
			}
		};
		env.setClassLink(link);
		try (CloseableLock lock = data.lock()) {
			// Instead of copying code, we will just fire ClassLink
			// for all classes for this JVMTI environment.
			data.list().forEach(link::invoke);
		}
	}

	private static void hideThrowableMethods(InstanceClass klass) {
		for (JavaMethod method : klass.methodArea().list()) {
			if (method.isConstructor()) {
				hiddenFrame(method);
			} else if ("fillInStackTrace".equals(method.getName())) {
				JavaClass[] args = method.getArgumentTypes();
				if (args.length == 0 || (args.length == 1 && args[0].getSort() == Type.INT)) {
					hiddenFrame(method);
				}
			}
		}
	}

	private static void hideLambdaForm(InstanceClass klass) {
		for (JavaMethod method : klass.methodArea().list()) {
			if (method.isHidden()) {
				hiddenFrame(method);
				callerSensitive(method);
			}
		}
	}

	private static void hideInvokeXX(InstanceClass klass) {
		for (JavaMethod method : klass.methodArea().list()) {
			if (method.isPolymorphic()) {
				hiddenFrame(method);
				callerSensitive(method);
			}
		}
	}

	private static void hideMethodAccessorImpl(InstanceClass klass) {
		klass.methodArea().stream().forEach(NativeJava::callerSensitive);
	}

	private static void fixCallerSensitive(InstanceClass klass) {
		klass.methodArea().stream().filter(JavaMethod::isCallerSensitive).forEach(NativeJava::callerSensitive);
	}

	private static void hiddenFrame(JavaMethod method) {
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_HIDDEN_FRAME;
	}

	private static void callerSensitive(JavaMethod method) {
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_CALLER_SENSITIVE;
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
		vmi.setProcessor(ASTORE, new ReferenceStoreProcessor());

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

	private static void makeHiddenMethod(JavaMethod method) {
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_HIDDEN_FRAME;
	}
}
