package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Initializes reflect/NativeMethodAccessorImpl.
 *
 * @author xDark
 */
@UtilityClass
public class MethodAccessorNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeMethodAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeMethodAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeMethodAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "invoke0", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getPublicOperations();
			InstanceValue m = locals.loadReference(0);
			int slot = ops.getInt(m, "slot");
			InstanceJavaClass declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) ops.getReference(m, "clazz", "Ljava/lang/Class;")).getValue();
			VMHelper helper = vm.getHelper();
			JavaMethod mn = declaringClass.getMethodBySlot(slot);
			if (mn == null) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			ObjectValue instance = locals.loadReference(1);
			boolean isStatic = (mn.getAccess() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			Value values = locals.loadReference(2);
			Value[] args;
			Type[] types = mn.getArgumentTypes();
			if (!values.isNull()) {
				ArrayValue passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
				args = Util.convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			} else {
				helper.checkEquals(types.length, 0);
				args = new Value[0];
			}
			Locals table;
			String name = mn.getName();
			String desc = mn.getDesc();
			int offset;
			if (isStatic) {
				offset = 0;
				table = vm.getThreadStorage().newLocals(mn);
			} else {
				mn = vm.getPublicLinkResolver().resolveVirtualMethod(instance, name, desc);
				offset = 1;
				table = vm.getThreadStorage().newLocals(mn);
				table.set(0, instance);
			}
			table.copyFrom(args, offset, 0, args.length);
			ExecutionContext executed = helper.invoke(mn, table);
			Value result = executed.getResult();
			if (result.isVoid()) {
				result = vm.getMemoryManager().nullValue(); // void
			} else {
				result = helper.boxGeneric(result, executed.getMethod().getReturnType());
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}
