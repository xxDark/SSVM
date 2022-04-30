package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
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
		val throwException = new boolean[]{false};
		val load = (Predicate<ExecutionContext>) ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val name = helper.readUtf8(locals.load(1));
			val builtin = locals.load(2).asBoolean();
			val mgr = vm.getNativeLibraryManager();
			val result = mgr.load(name, builtin);
			val handle = result.getHandle();
			if (handle == 0L) {
				val throwULE = !throwException[0] || locals.load(3).asBoolean();
				if (throwULE) {
					helper.throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError, helper.newUtf8(result.getErrorMessage()));
				}
				return false;
			}
			val _this = locals.<InstanceValue>load(0);
			_this.setLong("handle", handle);
			_this.setInt("jniVersion", result.getJniVersion());
			return true;
		};
		InstanceJavaClass libraryClass = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader$NativeLibrary");
		InstanceJavaClass findEntryClass = libraryClass;
		InstanceJavaClass findBuiltinLibClass;
		if (libraryClass == null) {
			val librariesClass = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/loader/NativeLibraries");
			findEntryClass = librariesClass;
			findBuiltinLibClass = librariesClass;
			vmi.setInvoker(librariesClass, "load", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;Z)", ctx -> {
				ctx.setResult(load.test(ctx) ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			});
			vmi.setInvoker(librariesClass, "unload", "(Ljava/lang/String;ZJ)V", ctx -> {
				val helper = ctx.getHelper();
				val locals = ctx.getLocals();
				val name = helper.readUtf8(locals.load(0));
				val isBuiltin = locals.load(1).asBoolean();
				val handle = locals.load(2).asLong();
				vm.getNativeLibraryManager().unload(name, isBuiltin, handle);
				return Result.ABORT;
			});
		} else {
			if (!vmi.setInvoker(libraryClass, "load", "(Ljava/lang/String;Z)V", ctx -> {
				val _this = ctx.getLocals().<InstanceValue>load(0);
				_this.setBoolean("loaded", load.test(ctx));
				return Result.ABORT;
			})) {
				val newInvoker = (MethodInvoker) ctx -> {
					ctx.setResult(load.test(ctx) ? IntValue.ONE : IntValue.ZERO);
					return Result.ABORT;
				};
				if (!vmi.setInvoker(libraryClass, "load0", "(Ljava/lang/String;Z)Z", newInvoker)) {
					throwException[0] = true;
					if (!vmi.setInvoker(libraryClass, "load0", "(Ljava/lang/String;ZZ)Z", newInvoker)) {
						throw new IllegalArgumentException("Unable to locate NativeLibrary#load method");
					}
				}
			}
			findBuiltinLibClass = vm.getSymbols().java_lang_ClassLoader;
		}
		val find = (MethodInvoker) ctx -> {
			val helper = vm.getHelper();
			val _this = ctx.getLocals().<InstanceValue>load(0);
			val handle = _this.getLong("handle");
			val mgr = vm.getNativeLibraryManager();
			ctx.setResult(LongValue.of(mgr.find(handle, helper.readUtf8(ctx.getLocals().load(1)))));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(findEntryClass, "find", "(Ljava/lang/String;)J", find)) {
			if (!vmi.setInvoker(findEntryClass, "findEntry", "(Ljava/lang/String;)J", find)) {
				if (!vmi.setInvoker(findEntryClass, "findEntry0", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;)J", find)) {
					throw new IllegalArgumentException("Unable to locate NativeLibrary#findEntry method");
				}
			}
		}
		val findBuiltinLib = (MethodInvoker) ctx -> {
			val helper =ctx.getHelper();
			val name = helper.readUtf8(ctx.getLocals().load(0));
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().findBuiltinLibrary(name)));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(findBuiltinLibClass, "findBuiltinLib", "(Ljava/lang/String;)Ljava/lang/String;", findBuiltinLib)) {
			throw new IllegalArgumentException("Unable to locate findBuiltinLib method");
		}
	}
}
