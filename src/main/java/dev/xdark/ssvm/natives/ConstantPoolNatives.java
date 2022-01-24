package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import me.coley.cafedude.ClassFile;
import me.coley.cafedude.constant.*;

/**
 * Initializes xx/reflect/ConstantPool
 *
 * @author xDark
 */
@UtilityClass
public class ConstantPoolNatives {

	/**
	 * Sets up xx/reflect/ConstantPool
	 *
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val cpClass = vm.getSymbols().reflect_ConstantPool;
		val vmi = vm.getInterface();
		vmi.setInvoker(cpClass, "getSize0", "(Ljav/lang/Object;)I", ctx -> {
			val wrapper = getCpOop(ctx);
			if (wrapper instanceof InstanceJavaClass) {
				val cf = ((InstanceJavaClass) wrapper).getRawClassFile();
				ctx.setResult(new IntValue(cf.getPool().size()));
			} else {
				ctx.setResult(IntValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAt0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			val className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			val helper = vm.getHelper();
			val result = helper.findClass(ctx.getOwner().getClassLoader(), className, false);
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, className);
			}
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAtIfLoaded0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			val className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			val result = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), className, false);
			if (result == null) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				ctx.setResult(result.getOop());
			}
			return Result.ABORT;
		});
		// getClassRefIndexAt0?
		// TODO all reflection stuff
		vmi.setInvoker(cpClass, "getIntAt0", "(Ljava/lang/Object;I)I", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new IntValue(((CpInt) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getLongAt0", "(Ljava/lang/Object;I)J", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new LongValue(((CpLong) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getFloatAt0", "(Ljava/lang/Object;I)F", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new FloatValue(((CpFloat) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getDoubleAt0", "(Ljava/lang/Object;I)D", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new DoubleValue(((CpDouble) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getUTF8At0", "(Ljava/lang/Object;I)Ljava/lang/String;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(vm.getHelper().newUtf8(((CpUtf8) item).getText()));
			return Result.ABORT;
		});
	}

	private int cpRangeCheck(ExecutionContext ctx, ClassFile cf) {
		val index = ctx.getLocals().load(2).asInt();
		if (index < 0 || index >= cf.getPool().size()) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return index;
	}

	private InstanceJavaClass getInstanceCpOop(ExecutionContext ctx) {
		val jc = getCpOop(ctx);
		if (!(jc instanceof InstanceJavaClass)) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (InstanceJavaClass) jc;
	}

	private JavaClass getCpOop(ExecutionContext ctx) {
		val vm = ctx.getVM();
		val value = ctx.getLocals().load(1);
		if (!(value instanceof JavaValue)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		val wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (JavaClass) wrapper;
	}
}
