package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * Initializes java/lang/System.
 *
 * @author xDark
 */
@UtilityClass
public class SystemNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass sys = symbols.java_lang_System();
		vmi.setInvoker(sys, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(vm.getTimeManager().currentTimeMillis());
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(vm.getTimeManager().nanoTime());
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			VMHelper helper = vm.getHelper();
			Locals locals = ctx.getLocals();
			ArrayValue src = helper.checkNotNull(locals.loadReference(0));
			ArrayValue dst = helper.checkNotNull(locals.loadReference(2));
			int srcPos = locals.loadInt(1);
			int dstPos = locals.loadInt(3);
			int length = locals.loadInt(4);
			MemoryManager memoryManager = vm.getMemoryManager();
			JavaClass srcComponent = src.getJavaClass().getComponentType();
			JavaClass dstComponent = dst.getJavaClass().getComponentType();

			int scale = memoryManager.sizeOfType(srcComponent);
			helper.checkEquals(scale, memoryManager.sizeOfType(dstComponent));

			int start = memoryManager.arrayBaseOffset(srcComponent);
			helper.checkEquals(start, memoryManager.arrayBaseOffset(dstComponent));

			MemoryData srcData = src.getMemory().getData();
			long dataStartPos = start + srcPos * (long) scale;
			MemoryData dstData = dst.getMemory().getData();
			srcData.copy(dataStartPos, dstData, start + (long) dstPos * scale, (long) length * scale);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(ctx.getLocals().loadReference(0).hashCode());
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			InstanceValue value = ctx.getLocals().loadReference(0);
			InstanceJavaClass jc = value.getJavaClass();
			JavaMethod mn = vm.getPublicLinkResolver().resolveVirtualMethod(jc, value.getJavaClass(), "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			Map<String, String> properties = vm.getProperties();
			VMHelper helper = vm.getHelper();

			ThreadStorage ts = vm.getThreadStorage();
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				String key = entry.getKey();
				String property = entry.getValue();
				Locals locals = ts.newLocals(mn);
				locals.set(0, value);
				locals.set(1, helper.newUtf8(key));
				locals.set(2, helper.newUtf8(property));
				helper.invoke(mn, locals);
			}
			ctx.setResult(value);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setIn0", "(Ljava/io/InputStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			ctx.getOperations().putReference(sys, "in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			ctx.getOperations().putReference(sys, "out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			ctx.getOperations().putReference(sys, "err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ObjectValue name = ctx.getLocals().loadReference(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(name);
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().mapLibraryName(helper.readUtf8(name))));
			return Result.ABORT;
		});
	}
}
