package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadStorage;
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
		Symbols symbols = vm.getSymbols();
		InstanceClass sys = symbols.java_lang_System();
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
			VMOperations ops = vm.getOperations();
			Locals locals = ctx.getLocals();
			ArrayValue src = ops.checkNotNull(locals.loadReference(0));
			ArrayValue dst = ops.checkNotNull(locals.loadReference(2));
			int srcPos = locals.loadInt(1);
			int dstPos = locals.loadInt(3);
			int length = locals.loadInt(4);
			MemoryManager memoryManager = vm.getMemoryManager();
			JavaClass srcComponent = src.getJavaClass().getComponentType();
			JavaClass dstComponent = dst.getJavaClass().getComponentType();

			long scale = memoryManager.sizeOfType(srcComponent);
			ops.checkEquals(scale, memoryManager.sizeOfType(dstComponent));

			int start = memoryManager.arrayBaseOffset(srcComponent);
			ops.checkEquals(start, memoryManager.arrayBaseOffset(dstComponent));

			MemoryData srcData = src.getMemory().getData();
			long dataStartPos = start + srcPos * (long) scale;
			MemoryData dstData = dst.getMemory().getData();
			srcData.write(dataStartPos, dstData, start + (long) dstPos * scale, (long) length * scale);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(ctx.getLocals().loadReference(0).hashCode());
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			InstanceValue value = ctx.getLocals().loadReference(0);
			InstanceClass jc = value.getJavaClass();
			JavaMethod mn = vm.getLinkResolver().resolveVirtualMethod(jc, value.getJavaClass(), "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			Map<String, String> properties = vm.getProperties();
			VMOperations ops = vm.getOperations();

			ThreadStorage ts = vm.getThreadStorage();
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				String key = entry.getKey();
				String property = entry.getValue();
				Locals locals = ts.newLocals(mn);
				locals.setReference(0, value);
				locals.setReference(1, ops.newUtf8(key));
				locals.setReference(2, ops.newUtf8(property));
				ops.invokeVoid(mn, locals);
			}
			ctx.setResult(value);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setIn0", "(Ljava/io/InputStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			vm.getOperations().putReference(sys, "in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			vm.getOperations().putReference(sys, "out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			ObjectValue stream = ctx.getLocals().loadReference(0);
			vm.getOperations().putReference(sys, "err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ObjectValue name = ctx.getLocals().loadReference(0);
			VMOperations ops = vm.getOperations();
			ops.checkNotNull(name);
			ctx.setResult(ops.newUtf8(vm.getNativeLibraryManager().mapLibraryName(ops.readUtf8(name))));
			return Result.ABORT;
		});
	}
}
