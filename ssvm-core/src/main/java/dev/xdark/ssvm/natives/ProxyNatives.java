package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ClassDefinitionOption;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/reflect/Proxy.
 *
 * @author xDark
 */
@UtilityClass
public class ProxyNatives {

	/**
	 * Initializes java/lang/reflect/Proxy.
	 *
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_lang_reflect_Proxy();
		vmi.setInvoker(jc, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BII)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			ObjectValue loader = locals.loadReference(0);
			ObjectValue name = locals.loadReference(1);
			ArrayValue bytes = ops.checkNotNull(locals.loadReference(2));
			int off = locals.loadInt(3);
			int len = locals.loadInt(4);
			ParsedClassData parsed = vm.getClassDefiner().parseClass(ops.readUtf8(name), ops.toJavaBytes(bytes), off, len, "JVM_DefineClass");
			if (parsed == null) {
				ops.throwException(symbols.java_lang_InternalError(), "Invalid bytecode");
			}
			ObjectValue nullValue = vm.getMemoryManager().nullValue();
			InstanceClass result = ops.defineClass(loader, parsed, nullValue, "JVM_DefineClass", ClassDefinitionOption.ANONYMOUS);
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
	}
}
