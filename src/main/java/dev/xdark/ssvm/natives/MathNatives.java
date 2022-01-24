package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.DoubleValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes math natives.
 *
 * @author xDark
 */
@UtilityClass
public class MathNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public static void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_lang_StrictMath;
		vmi.setInvoker(jc, "sin", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sin(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cos", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cos(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tan", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.tan(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "asin", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.asin(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "acos", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.acos(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.atan(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "exp", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.exp(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log10", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log10(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sqrt", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sqrt(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cbrt", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cbrt(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "IEEEremainder", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.IEEEremainder(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan2", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.atan2(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "pow", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.pow(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sinh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sinh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cosh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cosh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tanh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.tanh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hypot", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.hypot(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "expm1", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.expm1(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log1p", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log1p(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
	}
}
