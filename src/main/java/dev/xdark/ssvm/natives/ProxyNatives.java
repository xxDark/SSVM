package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
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
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_lang_reflect_Proxy();
		vmi.setInvoker(jc, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BII)Ljava/lang/Class;", ctx -> {
			// Simply invoke defineClass in a loader.
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ObjectValue loader = locals.<ObjectValue>load(0);
			Value name = locals.load(1);
			ArrayValue bytes = helper.checkNotNull(locals.<ObjectValue>load(2));
			Value off = locals.load(3);
			Value len = locals.load(4);
			InstanceJavaClass result;
			if (loader.isNull()) {
				ClassParseResult parsed = vm.getClassDefiner().parseClass(helper.readUtf8(name), helper.toJavaBytes(bytes), off.asInt(), len.asInt(), "JVM_DefineClass");
				if (parsed == null) {
					helper.throwException(symbols.java_lang_InternalError(), "Invalid bytecode");
				}
				ObjectValue nullValue = vm.getMemoryManager().nullValue();
				result = helper.newInstanceClass(nullValue, nullValue, parsed.getClassReader(), parsed.getNode());
			} else {
				result = ((JavaValue<InstanceJavaClass>) helper.invokeVirtual("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;", new Value[]{
					loader,
					name,
					bytes,
					off,
					len
				}).getResult()).getValue();
			}
			vm.getClassLoaders().getClassLoaderData(loader).forceLinkClass(result);
			result.link();
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
	}
}
