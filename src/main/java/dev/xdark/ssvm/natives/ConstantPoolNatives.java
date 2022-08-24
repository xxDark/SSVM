package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.constant.*;

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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass cpClass = vm.getSymbols().reflect_ConstantPool();
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(cpClass, "getSize0", "(Ljav/lang/Object;)I", ctx -> {
			JavaClass wrapper = getCpOop(ctx);
			if (wrapper instanceof InstanceJavaClass) {
				ClassFile cf = ((InstanceJavaClass) wrapper).getRawClassFile();
				ctx.setResult(cf.getPool().size());
			} else {
				ctx.setResult(0);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAt0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			String className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			VMHelper helper = vm.getHelper();
			JavaClass result = helper.findClass(ctx.getOwner().getClassLoader(), className, false);
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAtIfLoaded0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			String className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			JavaClass result = vm.getClassLoaders().getClassLoaderData(ctx.getOwner().getClassLoader()).getClass(className);
			if (result == null) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				ctx.setResult(result.getOop());
			}
			return Result.ABORT;
		});
		// getClassRefIndexAt0?
		// TODO all reflection stuff
		vmi.setInvoker(cpClass, "getIntAt0", "(Ljava/lang/Object;I)I", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpInt) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getLongAt0", "(Ljava/lang/Object;I)J", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpLong) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getFloatAt0", "(Ljava/lang/Object;I)F", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpFloat) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getDoubleAt0", "(Ljava/lang/Object;I)D", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpDouble) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getUTF8At0", "(Ljava/lang/Object;I)Ljava/lang/String;", ctx -> {
			InstanceJavaClass wrapper = getInstanceCpOop(ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(vm.getHelper().newUtf8(((CpUtf8) item).getText()));
			return Result.ABORT;
		});
	}

	private int cpRangeCheck(ExecutionContext ctx, ClassFile cf) {
		int index = ctx.getLocals().loadInt(2);
		if (index < 0 || index >= cf.getPool().size()) {
			VirtualMachine vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		return index;
	}

	private InstanceJavaClass getInstanceCpOop(ExecutionContext ctx) {
		JavaClass jc = getCpOop(ctx);
		if (!(jc instanceof InstanceJavaClass)) {
			VirtualMachine vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		return (InstanceJavaClass) jc;
	}

	private JavaClass getCpOop(ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		Value value = ctx.getLocals().loadReference(1);
		if (!(value instanceof JavaValue)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		Object wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		return (JavaClass) wrapper;
	}
}
