package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.UnsafeUtil;
import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
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
		Class<?> c = definer.define(jitClass);
		List<Object> constants = jitClass.getConstants();
		if (!constants.isEmpty()) {
			Field field = c.getDeclaredField("constants");
			try {
				Class.forName(c.getName(), true, c.getClassLoader());
			} catch(ClassNotFoundException ignored) {
			}
			Unsafe u = UnsafeUtil.get();
			u.putObject(u.staticFieldBase(field), u.staticFieldOffset(field), constants.toArray());
		}
		Consumer<ExecutionContext> cons = (Consumer<ExecutionContext>) c.getConstructor().newInstance();
		VirtualMachine vm = method.getOwner().getVM();
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