package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.MemoryData;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Properties;

/**
 * Initializes java/lang/System.
 *
 * @author xDark
 */
@UtilityClass
public class SystemNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass sys = symbols.java_lang_System();
		vmi.setInvoker(sys, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(LongValue.of(vm.getTimeManager().currentTimeMillis()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(LongValue.of(vm.getTimeManager().nanoTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			VMHelper helper = vm.getHelper();
			Locals locals = ctx.getLocals();
			ArrayValue src = helper.checkNotNullArray(locals.load(0));
			ArrayValue dst = helper.checkNotNullArray(locals.load(2));
			int srcPos = locals.load(1).asInt();
			int dstPos = locals.load(3).asInt();
			int length = locals.load(4).asInt();
			MemoryManager memoryManager = vm.getMemoryManager();
			JavaClass srcComponent = src.getJavaClass().getComponentType();
			JavaClass dstComponent = dst.getJavaClass().getComponentType();

			int scale = memoryManager.arrayIndexScale(srcComponent);
			helper.checkEquals(scale, memoryManager.arrayIndexScale(dstComponent));

			int start = memoryManager.arrayBaseOffset(srcComponent);
			helper.checkEquals(start, memoryManager.arrayBaseOffset(dstComponent));

			MemoryData srcData = src.getMemory().getData();
			int dataStartPos = start + srcPos * scale;
			MemoryData dstData = dst.getMemory().getData();
			srcData.copy(dataStartPos, dstData, start + (long) dstPos * scale, (long) length * scale);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(IntValue.of(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			InstanceValue value = ctx.getLocals().<InstanceValue>load(0);
			InstanceJavaClass jc = (InstanceJavaClass) value.getJavaClass();
			JavaMethod mn = jc.getVirtualMethod("setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			Properties properties = vm.getProperties();
			VMHelper helper = vm.getHelper();

			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				Object key = entry.getKey();
				Object property = entry.getValue();
				helper.invokeExact(jc, mn, new Value[0], new Value[]{
						value,
						helper.newUtf8(key.toString()),
						helper.newUtf8(property.toString())
				});
			}
			ctx.setResult(value);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setIn0", "(Ljava/io/InputStream;)V", ctx -> {
			Value stream = ctx.getLocals().load(0);
			sys.setFieldValue("in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			Value stream = ctx.getLocals().load(0);
			sys.setFieldValue("out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			Value stream = ctx.getLocals().load(0);
			sys.setFieldValue("err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ObjectValue name = ctx.getLocals().<ObjectValue>load(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(name);
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().mapLibraryName(helper.readUtf8(name))));
			return Result.ABORT;
		});
	}
}
