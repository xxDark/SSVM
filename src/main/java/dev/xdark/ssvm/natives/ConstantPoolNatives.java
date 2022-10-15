package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
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
		InstanceClass cpClass = vm.getSymbols().reflect_ConstantPool();
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(cpClass, "getSize0", "(Ljav/lang/Object;)I", ctx -> {
			JavaClass wrapper = getCpOop(vm, ctx);
			if (wrapper instanceof InstanceClass) {
				ClassFile cf = ((InstanceClass) wrapper).getRawClassFile();
				ctx.setResult(cf.getPool().size());
			} else {
				ctx.setResult(0);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAt0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			String className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			VMOperations ops = vm.getOperations();
			JavaClass result = ops.findClass(ctx.getMethod().getOwner().getClassLoader(), className, false);
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAtIfLoaded0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			String className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			JavaClass result = vm.getClassLoaders().getClassLoaderData(ctx.getMethod().getOwner().getClassLoader()).getClass(className);
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
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpInt) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getLongAt0", "(Ljava/lang/Object;I)J", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpLong) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getFloatAt0", "(Ljava/lang/Object;I)F", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpFloat) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getDoubleAt0", "(Ljava/lang/Object;I)D", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(((CpDouble) item).getValue());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getUTF8At0", "(Ljava/lang/Object;I)Ljava/lang/String;", ctx -> {
			InstanceClass wrapper = getInstanceCpOop(vm, ctx);
			ClassFile cf = wrapper.getRawClassFile();
			int index = cpRangeCheck(vm, ctx, cf);
			ConstPoolEntry item = cf.getCp(index);
			ctx.setResult(vm.getOperations().newUtf8(((CpUtf8) item).getText()));
			return Result.ABORT;
		});
	}

	private int cpRangeCheck(VirtualMachine vm, ExecutionContext<?> ctx, ClassFile cf) {
		int index = ctx.getLocals().loadInt(2);
		if (index < 0 || index >= cf.getPool().size()) {
			vm.getOperations().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		return index;
	}

	private InstanceClass getInstanceCpOop(VirtualMachine vm, ExecutionContext<?> ctx) {
		JavaClass jc = getCpOop(vm, ctx);
		if (!(jc instanceof InstanceClass)) {
			vm.getOperations().throwException(vm.getSymbols().java_lang_IllegalArgumentException());
		}
		return (InstanceClass) jc;
	}

	private JavaClass getCpOop(VirtualMachine vm, ExecutionContext<?> ctx) {
		InstanceValue value = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(1));
		return vm.getClassStorage().lookup(value);
	}
}
