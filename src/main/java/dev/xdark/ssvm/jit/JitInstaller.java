package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.JavaMethod;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.function.Consumer;

/**
 * Installs "JIT" compiled code.
 *
 * @author xdark
 */
@UtilityClass
public class JitInstaller {

	/**
	 * Installs JITted code.
	 *
	 * @param method
	 * 		Method to "JIT".
	 * @param definer
	 * 		Class definer.
	 * @param jitClass
	 * 		Class containing JIT code.
	 *
	 * @throws ReflectiveOperationException
	 * 		If resulting class cannot be instantiated.
	 */
	public void install(JavaMethod method, ClassDefiner definer, JitClass jitClass) throws ReflectiveOperationException {
		val c = definer.define(jitClass);
		val constants = jitClass.getConstants();
		if (!constants.isEmpty()) {
			val field = c.getDeclaredField("constants");
			field.setAccessible(true);
			field.set(null, constants.toArray());
		}
		val cons = (Consumer<ExecutionContext>) c.getConstructor().newInstance();
		val vm = method.getOwner().getVM();
		vm.getInterface().setInvoker(method, ctx -> {
			cons.accept(ctx);
			return Result.ABORT;
		});
	}

	/**
	 * Class definer.
	 *
	 * @author xDark
	 */
	public interface ClassDefiner {

		/**
		 * Defines class.
		 *
		 * @param jitClass
		 * 		Class containing JIT code.
		 *
		 * @return defined class.
		 */
		Class<?> define(JitClass jitClass);
	}
}
