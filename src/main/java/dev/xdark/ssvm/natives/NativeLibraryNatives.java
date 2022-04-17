package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.function.Predicate;

/**
 * Initializes java/lang/ClassLoader$NativeLibrary.
 *
 * @author xDark
 */
@UtilityClass
public class NativeLibraryNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val library = symbols.java_lang_ClassLoader$NativeLibrary;
		val load = (Predicate<ExecutionContext>) ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val name = helper.readUtf8(locals.load(1));
			val builtin = locals.load(2).asBoolean();
			val mgr = vm.getNativeLibraryManager();
			val handle = mgr.load(name, builtin);
			if (handle == 0L) {
				return false;
			}
			val _this = locals.<InstanceValue>load(0);
			_this.setLong("handle", handle);
			_this.setInt("jniVersion", mgr.getJniVersion());
			return true;
		};
		if (!vmi.setInvoker(library, "load", "(Ljava/lang/String;Z)V", ctx -> {
			val _this = ctx.getLocals().<InstanceValue>load(0);
			_this.setBoolean("loaded", load.test(ctx));
			return Result.ABORT;
		})) {
			if (!vmi.setInvoker(library, "load0", "(Ljava/lang/String;Z)Z", ctx -> {
				ctx.setResult(load.test(ctx) ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			})) {
				throw new IllegalArgumentException("Unable to locate NativeLibrary#load method");
			}
		}
		val find = (MethodInvoker) ctx -> {
			val helper = vm.getHelper();
			val _this = ctx.getLocals().<InstanceValue>load(0);
			val handle = _this.getLong("handle");
			val mgr = vm.getNativeLibraryManager();
			ctx.setResult(LongValue.of(mgr.find(handle, helper.readUtf8(ctx.getLocals().load(1)))));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(library, "find", "(Ljava/lang/String;)J", find)) {
			if (!vmi.setInvoker(library, "findEntry", "(Ljava/lang/String;)J", find)) {
				throw new IllegalArgumentException("Unable to locate NativeLibrary#findEntry method");
			}
		}
	}
}
