package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

import java.util.Arrays;

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
}
