package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;

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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val sys = symbols.java_lang_System;
		vmi.setInvoker(sys, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(new LongValue(System.currentTimeMillis()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(new LongValue(System.nanoTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val src = helper.checkNotNullArray(locals.load(0));
			val dst = helper.checkNotNullArray(locals.load(2));
			int srcPos = locals.load(1).asInt();
			int dstPos = locals.load(3).asInt();
			int length = locals.load(4).asInt();
			val memoryManager = vm.getMemoryManager();
			val srcComponent = src.getJavaClass().getComponentType();
			val dstComponent = dst.getJavaClass().getComponentType();

			int scale = memoryManager.arrayIndexScale(srcComponent);
			helper.checkEquals(scale, memoryManager.arrayIndexScale(dstComponent));

			int start = memoryManager.arrayBaseOffset(srcComponent);
			helper.checkEquals(start, memoryManager.arrayBaseOffset(dstComponent));

			val srcData = src.getMemory().getData().slice();
			int dataStartPos = start + srcPos * scale;
			srcData.position(dataStartPos).limit(dataStartPos + length * scale);
			val dstData = dst.getMemory().getData().slice();
			dstData.position(start + dstPos * scale);
			dstData.put(srcData);

			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			val value = ctx.getLocals().<InstanceValue>load(0);
			val jc = (InstanceJavaClass) value.getJavaClass();
			val mn = jc.getVirtualMethod("setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			val properties = vm.getProperties();
			val helper = vm.getHelper();

			for (val entry : properties.entrySet()) {
				val key = entry.getKey();
				val property = entry.getValue();
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
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			val name = ctx.getLocals().<ObjectValue>load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(name);
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().mapLibraryName(helper.readUtf8(name))));
			return Result.ABORT;
		});
	}
}
