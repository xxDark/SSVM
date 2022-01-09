package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
final class NativeJava {

	/**
	 * Sets up VM instance.
	 *
	 * @param vm
	 * 		VM to setup.
	 */
	static void vmInit(VirtualMachine vm) throws Exception {
		var vmi = vm.getVmInterface();
		setInstructions(vmi);
		// java/lang/Class.registerNatives()V
		var jlc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
		vmi.setInvoker(jlc, "registerNatives", "()V", ctx -> {
			return Result.ABORT;
		});
		// java/lang/Class.getPrimitiveClass(Ljava/lang/String;)Ljava/lang/Class;
		String[] primitiveNames = {
				"long",
				"double",
				"int",
				"float",
				"char",
				"short",
				"byte",
				"boolean",
				"void"
		};
		Arrays.sort(primitiveNames);
		var descriptors = new String[primitiveNames.length];
		for (int i = 0, j = primitiveNames.length; i < j; i++) {
			switch (primitiveNames[i]) {
				case "long":
					descriptors[i] = "J";
					break;
				case "int":
					descriptors[i] = "I";
					break;
				case "double":
					descriptors[i] = "D";
					break;
				case "float":
					descriptors[i] = "F";
					break;
				case "char":
					descriptors[i] = "C";
					break;
				case "short":
					descriptors[i] = "S";
					break;
				case "byte":
					descriptors[i] = "B";
					break;
				case "boolean":
					descriptors[i] = "Z";
					break;
				case "void":
					descriptors[i] = "V";
					break;
				default:
					throw new AssertionError();
			}
		}
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {

			throw new UnsupportedOperationException();
		});
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vmi
	 * 		VM interface.
	 */
	private static void setInstructions(VMInterface vmi) {
		vmi.setProcessor(ACONST_NULL, new ConstantProcessor(NullValue.INSTANCE));
		// ICONST_M1..INCONST_5
		for (int x = ICONST_M1; x <= ICONST_5; x++) {
			vmi.setProcessor(x, new ConstantIntProcessor(x - ICONST_3));
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

		// TODO arrays

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
		vmi.setProcessor(DREM, new BiFloatProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(FREM, new BiDoubleProcessor((v1, v2) -> v1 % v2));

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

		// JSR/RET?

		vmi.setProcessor(TABLESWITCH, new TableSwitchProcessor());
		vmi.setProcessor(LOOKUPSWITCH, new LookupSwitchProcessor());

		vmi.setProcessor(IRETURN, new ReturnIntProcessor());
		vmi.setProcessor(LRETURN, new ReturnLongProcessor());
		vmi.setProcessor(FRETURN, new ReturnFloatProcessor());
		vmi.setProcessor(DRETURN, new ReturnDoubleProcessor());
		vmi.setProcessor(ARETURN, new ReturnValueProcessor());
		vmi.setProcessor(RETURN, new ReturnVoidProcessor());

		// TODO

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(Value::isNull));
	}
}
