package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.ArrayJavaClass;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Object.
 *
 * @author xDark
 */
@UtilityClass
public class ObjectNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass object = symbols.java_lang_Object();
		vmi.setInvoker(object, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(object, "<init>", "()V", MethodInvoker.noop());
		vmi.setInvoker(object, "getClass", "()Ljava/lang/Class;", ctx -> {
			ctx.setResult(ctx.getLocals().loadReference(0).getJavaClass().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notify", "()V", ctx -> {
			Mutex mutex = ctx.getMemoryManager().getMutex(ctx.getLocals().loadReference(0));
			if (!mutex.isHeldByCurrentThread()) {
				vm.getHelper().throwException(symbols.java_lang_IllegalMonitorStateException());
			}
			mutex.doNotify();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notifyAll", "()V", ctx -> {
			Mutex mutex = ctx.getMemoryManager().getMutex(ctx.getLocals().loadReference(0));
			if (!mutex.isHeldByCurrentThread()) {
				vm.getHelper().throwException(symbols.java_lang_IllegalMonitorStateException());
			}
			mutex.doNotifyAll();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "wait", "(J)V", ctx -> {
			Locals locals = ctx.getLocals();
			Mutex mutex = ctx.getMemoryManager().getMutex(ctx.getLocals().loadReference(0));
			if (!mutex.isHeldByCurrentThread()) {
				vm.getHelper().throwException(symbols.java_lang_IllegalMonitorStateException());
			}
			try {
				long time = locals.loadLong(1);
				if (time == 0L) {
					time = Long.MAX_VALUE;
				}
				mutex.doWait(time);
			} catch (InterruptedException ex) {
				vm.getHelper().throwException(symbols.java_lang_InterruptedException());
			}
			ctx.pollSafePointAndSuspend();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "hashCode", "()I", ctx -> {
			ctx.setResult(ctx.getLocals().loadReference(0).hashCode());
			return Result.ABORT;
		});
		vmi.setInvoker(object, "clone", "()Ljava/lang/Object;", ctx -> {
			ObjectValue _this = ctx.getLocals().loadReference(0);
			JavaClass type = _this.getJavaClass();
			VMHelper helper = vm.getHelper();
			MemoryManager memoryManager = vm.getMemoryManager();
			ObjectValue clone;
			if (type instanceof ArrayJavaClass) {
				ArrayValue arr = (ArrayValue) _this;
				clone = memoryManager.newArray((ArrayJavaClass) type, arr.getLength());
			} else {
				clone = memoryManager.newInstance((InstanceJavaClass) type);
			}
			int originalOffset = memoryManager.valueBaseOffset(_this);
			int offset = memoryManager.valueBaseOffset(clone);
			helper.checkEquals(originalOffset, offset);
			MemoryData copyTo = clone.getMemory().getData();
			MemoryData copyFrom = _this.getMemory().getData();
			copyFrom.write(
				offset,
				copyTo,
				offset,
				copyFrom.length() - offset
			);
			ctx.setResult(clone);
			return Result.ABORT;
		});
	}
}
