package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes java/lang/Throwable.
 *
 * @author xDark
 */
@UtilityClass
public class ThrowableNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass throwable = symbols.java_lang_Throwable();
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			InstanceValue exception = ctx.getLocals().loadReference(0);
			VMOperations ops = vm.getOperations();
			Backtrace vmBacktrace = vm.currentOSThread().getBacktrace();
			// HotSpot allocates internal oop containing all the data,
			// see BacktraceBuilder. We don't need that, allocate StackTraceElement[]
			// immediately.
			List<InstanceValue> bt = new ArrayList<>(vmBacktrace.depth());
			for (ExecutionContext<?> frame : vmBacktrace) {
				if (Modifier.isHiddenFrame(frame.getMethod().getModifiers())) {
					continue;
				}
				bt.add(ops.newStackTraceElement(frame));
			}
			ArrayValue stacktrace = ops.toVMReferences(bt.toArray(new ObjectValue[0]));
			vm.getOperations().putReference(exception, "backtrace", "Ljava/lang/Object;", stacktrace);
			JavaField depth = symbols.java_lang_Throwable().getField("depth", "I");
			if (depth != null) {
				exception.getData().writeInt(depth.getOffset(), bt.size());
			}
			ctx.setResult(exception);
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceDepth", "()I", ctx -> {
			VMOperations ops = vm.getOperations();
			ArrayValue bt = ops.checkNotNull(ops.getReference((ObjectValue) ctx.getLocals().loadReference(0), "backtrace", "Ljava/lang/Object;"));
			ctx.setResult(bt.getLength());
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceElement", "(I)Ljava/lang/StackTraceElement;", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			ArrayValue bt = ops.checkNotNull(ops.getReference(_this, "backtrace", "Ljava/lang/Object;"));
			int idx = locals.loadInt(1);
			int len = bt.getLength();
			ops.arrayRangeCheck(idx, len);
			ObjectValue element = bt.getReference(len - idx - 1);
			ctx.setResult(element);
			return Result.ABORT;
		});
	}
}
