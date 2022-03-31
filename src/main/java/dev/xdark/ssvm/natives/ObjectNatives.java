package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Object.
 *
 * @author xDark
 */
@UtilityClass
public class ObjectNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val object = symbols.java_lang_Object;
		vmi.setInvoker(object, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(object, "<init>", "()V", ctx -> {
			ctx.getLocals().<InstanceValue>load(0).initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "getClass", "()Ljava/lang/Class;", ctx -> {
			ctx.setResult(ctx.getLocals().<ObjectValue>load(0).getJavaClass().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notify", "()V", ctx -> {
			ctx.getLocals().<ObjectValue>load(0).vmNotify();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notifyAll", "()V", ctx -> {
			ctx.getLocals().<ObjectValue>load(0).vmNotifyAll();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "wait", "(J)V", ctx -> {
			val locals = ctx.getLocals();
			try {
				locals.<ObjectValue>load(0).vmWait(locals.load(1).asLong());
			} catch (InterruptedException ex) {
				vm.getHelper().throwException(symbols.java_lang_InterruptedException);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(object, "hashCode", "()I", ctx -> {
			ctx.setResult(IntValue.of(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(object, "clone", "()Ljava/lang/Object;", ctx -> {
			val _this = ctx.getLocals().<ObjectValue>load(0);
			val type = _this.getJavaClass();
			val helper = vm.getHelper();
			val memoryManager = vm.getMemoryManager();
			ObjectValue clone;
			if (type instanceof ArrayJavaClass) {
				val arr = (ArrayValue) _this;
				clone = memoryManager.newArray((ArrayJavaClass) type, arr.getLength());
			} else {
				clone = memoryManager.newInstance((InstanceJavaClass) type);
			}
			val originalOffset = memoryManager.valueBaseOffset(_this);
			val offset = memoryManager.valueBaseOffset(clone);
			helper.checkEquals(originalOffset, offset);
			val copyTo = clone.getMemory().getData();
			val copyFrom = _this.getMemory().getData();
			copyFrom.copy(
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
