package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.UnsafeUtil;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.MethodNode;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Installs compiled code.
 *
 * @author xDark
 */
@UtilityClass
public class CodeInstaller {

	/**
	 * Installs compiled code.
	 *
	 * @param method   Method to install compiled code for.
	 * @param definer  Class definer.
	 * @param jitClass Class containing compiled code.
	 * @throws ReflectiveOperationException If resulting class cannot be instantiated.
	 */
	public void install(JavaMethod method, ClassDefiner definer, CompiledData jitClass) throws ReflectiveOperationException {
		Class<?> c = definer.define(jitClass);
		List<Object> constants = jitClass.getConstants();
		if (!constants.isEmpty()) {
			Field field = c.getDeclaredField(JitCompiler.CONSTANTS_FIELD);
			try {
				Class.forName(c.getName(), true, c.getClassLoader());
			} catch (ClassNotFoundException ignored) {
			}
			Object[] constantsArray = constants.toArray();
			if (JitCompiler.CAN_USE_STATIC_FINAL) {
				Unsafe unsafe = UnsafeUtil.get();
				unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), constantsArray);
			} else {
				field.setAccessible(true);
				field.set(null, constantsArray);
			}
		}
		AbstractInvoker invoker = (AbstractInvoker) c.getConstructor().newInstance();
		VirtualMachine vm = method.getOwner().getVM();
		MethodNode node = method.getNode();
		node.access |= Modifier.ACC_COMPILED;
		vm.getInterface().setInvoker(method, ctx -> {
			invoker.execute(ctx);
			ctx.pollSafePointAndSuspend();
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
		 * @param jitClass Class containing compiled code.
		 * @return defined class.
		 */
		Class<?> define(CompiledData jitClass);
	}
}
