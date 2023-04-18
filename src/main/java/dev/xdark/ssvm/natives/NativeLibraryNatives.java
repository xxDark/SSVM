package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.nt.LibraryLoadResult;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

/**
 * Initializes java/lang/ClassLoader$NativeLibrary.
 *
 * @author xDark
 */
@UtilityClass
public class NativeLibraryNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		boolean[] throwException = new boolean[]{false};
		Predicate<ExecutionContext<?>> load = ctx -> {
			VMHelper helper = vm.getHelper();
			Locals locals = ctx.getLocals();
			String name = helper.readUtf8(locals.loadReference(1));
			boolean builtin = locals.loadInt(2) != 0;
			NativeLibraryManager mgr = vm.getNativeLibraryManager();
			LibraryLoadResult result = mgr.load(name, builtin);
			long handle = result.getHandle();
			if (handle == 0L) {
				boolean throwULE = !throwException[0] || locals.loadInt(3) != 0;
				if (throwULE) {
					helper.throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError(), helper.newUtf8(result.getErrorMessage()));
				}
				return false;
			}
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getPublicOperations();
			InstanceJavaClass jc = _this.getJavaClass();
			ops.putLong(_this, jc, "handle", handle);
			ops.putInt(_this, jc, "jniVersion", result.getJniVersion());
			return true;
		};
		InstanceJavaClass libraryClass = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader$NativeLibrary");
		InstanceJavaClass findEntryClass = libraryClass;
		InstanceJavaClass findBuiltinLibClass;
		if (libraryClass == null) {
			InstanceJavaClass librariesClass = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/loader/NativeLibraries");
			findEntryClass = librariesClass;
			findBuiltinLibClass = librariesClass;
			MethodInvoker doLoad = ctx -> {
				ctx.setResult(load.test(ctx) ? 1 : 0);
				return Result.ABORT;
			};
			if (!vmi.setInvoker(librariesClass, "load", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;Z)Z", doLoad)) {
				if (!vmi.setInvoker(librariesClass, "load", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;ZZ)Z", doLoad)) {
					if (!vmi.setInvoker(librariesClass, "load", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;ZZZ)Z", doLoad)) {
						throw new IllegalArgumentException("Unable to locate NativeLibraries#load method");
					}
				}
			}
			vmi.setInvoker(librariesClass, "unload", "(Ljava/lang/String;ZJ)V", ctx -> {
				VMHelper helper = ctx.getHelper();
				Locals locals = ctx.getLocals();
				String name = helper.readUtf8(locals.loadReference(0));
				boolean isBuiltin = locals.loadInt(1) != 0;
				long handle = locals.loadLong(2);
				vm.getNativeLibraryManager().unload(name, isBuiltin, handle);
				return Result.ABORT;
			});
		} else {
			if (!vmi.setInvoker(libraryClass, "load", "(Ljava/lang/String;Z)V", ctx -> {
				InstanceValue _this = ctx.getLocals().loadReference(0);
				ctx.getOperations().putBoolean(_this, _this.getJavaClass(), "loaded", load.test(ctx));
				return Result.ABORT;
			})) {
				MethodInvoker newInvoker = ctx -> {
					ctx.setResult(load.test(ctx) ? 1 : 0);
					return Result.ABORT;
				};
				if (!vmi.setInvoker(libraryClass, "load0", "(Ljava/lang/String;Z)Z", newInvoker)) {
					throwException[0] = true;
					if (!vmi.setInvoker(libraryClass, "load0", "(Ljava/lang/String;ZZ)Z", newInvoker)) {
						throw new IllegalArgumentException("Unable to locate NativeLibrary#load method");
					}
				}
			}
			findBuiltinLibClass = vm.getSymbols().java_lang_ClassLoader();
		}
		MethodInvoker find = ctx -> {
			VMHelper helper = vm.getHelper();
			InstanceValue _this = ctx.getLocals().loadReference(0);
			long handle = vm.getPublicOperations().getLong(_this, _this.getJavaClass(), "handle");
			NativeLibraryManager mgr = vm.getNativeLibraryManager();
			ctx.setResult(mgr.find(handle, helper.readUtf8(ctx.getLocals().loadReference(1))));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(findEntryClass, "find", "(Ljava/lang/String;)J", find)) {
			if (!vmi.setInvoker(findEntryClass, "findEntry", "(Ljava/lang/String;)J", find)) {
				if (!vmi.setInvoker(findEntryClass, "findEntry0", "(Ljdk/internal/loader/NativeLibraries$NativeLibraryImpl;Ljava/lang/String;)J", find)) {
					throw new IllegalArgumentException("Unable to locate NativeLibrary#findEntry method");
				}
			}
		}
		MethodInvoker findBuiltinLib = ctx -> {
			VMHelper helper = ctx.getHelper();
			String name = helper.readUtf8(ctx.getLocals().loadReference(0));
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().findBuiltinLibrary(name)));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(findBuiltinLibClass, "findBuiltinLib", "(Ljava/lang/String;)Ljava/lang/String;", findBuiltinLib)) {
			throw new IllegalArgumentException("Unable to locate findBuiltinLib method");
		}
	}
}
