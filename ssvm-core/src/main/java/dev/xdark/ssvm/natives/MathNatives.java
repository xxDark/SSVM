package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes math natives.
 *
 * @author xDark
 */
@UtilityClass
public class MathNatives {

	/**
	 * @param vm VM instance.
	 */
	public static void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_lang_StrictMath();
		vmi.setInvoker(jc, "sin", "(D)D", ctx -> {
			ctx.setResult(Math.sin(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cos", "(D)D", ctx -> {
			ctx.setResult(Math.cos(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tan", "(D)D", ctx -> {
			ctx.setResult(Math.tan(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "asin", "(D)D", ctx -> {
			ctx.setResult(Math.asin(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "acos", "(D)D", ctx -> {
			ctx.setResult(Math.acos(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan", "(D)D", ctx -> {
			ctx.setResult(Math.atan(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "exp", "(D)D", ctx -> {
			ctx.setResult(Math.exp(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log", "(D)D", ctx -> {
			ctx.setResult(Math.log(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log10", "(D)D", ctx -> {
			ctx.setResult(Math.log10(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sqrt", "(D)D", ctx -> {
			ctx.setResult(Math.sqrt(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cbrt", "(D)D", ctx -> {
			ctx.setResult(Math.cbrt(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "IEEEremainder", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(Math.IEEEremainder(locals.loadDouble(0), locals.loadDouble(2)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan2", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(Math.atan2(locals.loadDouble(0), locals.loadDouble(2)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "pow", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(Math.pow(locals.loadDouble(0), locals.loadDouble(2)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sinh", "(D)D", ctx -> {
			ctx.setResult(Math.sinh(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cosh", "(D)D", ctx -> {
			ctx.setResult(Math.cosh(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tanh", "(D)D", ctx -> {
			ctx.setResult(Math.tanh(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hypot", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(Math.hypot(locals.loadDouble(0), locals.loadDouble(2)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "expm1", "(D)D", ctx -> {
			ctx.setResult(Math.expm1(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log1p", "(D)D", ctx -> {
			ctx.setResult(Math.log1p(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
	}
}
