package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Provides additional functionality for
 * the VM and simplifies some things.
 *
 * @author xDark
 */
public final class VMHelper {

	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMHelper(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Invokes static method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeStatic(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
		}
		var method = javaClass.getMethod(name, desc);
		if (method == null) {
			throw new IllegalStateException("No such method: " + name + desc + " in " + javaClass.getInternalName());
		}
		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			throw new IllegalStateException("Method is not static");
		}
		var ctx = createContext(javaClass, method);
		contextPrepare(ctx, stack, locals, 0);
		javaClass.getVM().execute(ctx, true);
		return ctx;
	}

	/**
	 * Invokes virtual method.
	 *
	 * @param javaClass
	 * 		Class to search method in.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param stack
	 * 		Execution stack.
	 * @param locals
	 * 		Local variable table.
	 *
	 * @return invocation result.
	 */
	public ExecutionContext invokeVirtual(InstanceJavaClass javaClass, String name, String desc, Value[] stack, Value[] locals) {
		if (vm != javaClass.getVM()) {
			throw new IllegalStateException("Wrong helper!");
		}
		MethodNode method;
		do {
			method = javaClass.getMethod(name, desc);
			javaClass = javaClass.getSuperClass();
		} while (method == null && javaClass != null);
		if (method == null) {
			throw new IllegalStateException("No such method: " + name + desc + " in " + javaClass.getInternalName());
		}
		if ((method.access & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalStateException("Method is static");
		}
		var ctx = createContext(javaClass, method);
		contextPrepare(ctx, stack, locals, 0);
		javaClass.getVM().execute(ctx, true);
		return ctx;
	}

	/**
	 * Creates VM vales from constant.
	 *
	 * @return VM value.
	 *
	 * @throws IllegalStateException
	 * 		If constant value cannot be created.
	 */
	public Value valueFromLdc(Object cst) {
		var vm = this.vm;
		if (cst instanceof Long) return new LongValue((Long) cst);
		if (cst instanceof Double) return new DoubleValue((Double) cst);
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte)
			return new IntValue(((Number) cst).intValue());
		if (cst instanceof Character) return new IntValue((Character) cst);
		if (cst instanceof Float) return new DoubleValue((Float) cst);
		if (cst instanceof Boolean) return new IntValue((Boolean) cst ? 1 : 0);
		throw new UnsupportedOperationException("TODO: " + cst);
	}

	private static void contextPrepare(ExecutionContext ctx, Value[] stack, Value[] locals, int localIndex) {
		var lvt = ctx.getLocals();
		for (var local : locals) {
			lvt.set(localIndex++, local);
			if (local.isWide()) {
				localIndex++;
			}
		}
		var $stack = ctx.getStack();
		for (var value : stack) {
			$stack.pushGeneric(value);
		}
	}

	private static ExecutionContext createContext(InstanceJavaClass jc, MethodNode mn) {
		return new ExecutionContext(
				jc.getVM(),
				jc,
				mn,
				new Stack(mn.maxStack),
				new Locals(mn.maxStack)
		);
	}
}
