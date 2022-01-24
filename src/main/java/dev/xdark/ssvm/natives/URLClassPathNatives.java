package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes sun/misc/URLClassPath.
 *
 * @author xDark
 */
@UtilityClass
public class URLClassPathNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val ucp = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/URLClassPath");
		if (ucp != null) {
			// static jobjectArray get_lookup_cache_urls(JNIEnv *env, jobject loader, TRAPS) {return NULL;}
			vmi.setInvoker(ucp, "getLookupCacheURLs", "(Ljava/lang/ClassLoader;)[Ljava/net/URL;", ctx -> {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			});
		}
	}
}
