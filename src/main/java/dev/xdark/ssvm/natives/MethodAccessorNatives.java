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
	 * @param vm
	 * 		VM instance.
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
			InstanceValue m = locals.<InstanceValue>load(0);
			int slot = m.getInt("slot");
			InstanceJavaClass declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) m.getValue("clazz", "Ljava/lang/Class;")).getValue();
			VMHelper helper = vm.getHelper();
			JavaMethod mn = helper.getMethodBySlot(declaringClass, slot);
			if (mn == null) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			Value instance = locals.load(1);
			boolean isStatic = (mn.getAccess() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			Value values = locals.load(2);
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
			if (!isStatic) {
				Value[] prev = args;
				args = new Value[args.length + 1];
				args[0] = instance;
				System.arraycopy(prev, 0, args, 1, prev.length);
			}
			String name = mn.getName();
			String desc = mn.getDesc();
			ExecutionContext executed;
			if (isStatic) {
				executed = helper.invokeStatic(declaringClass, name, desc, new Value[0], args);
			} else {
				executed = helper.invokeVirtual(name, desc, new Value[0], args);
			}
			Value result = executed.getResult();
			if (result.isVoid()) result = NullValue.INSTANCE; // void
			else {
				result = helper.boxGeneric(result, executed.getMethod().getReturnType());
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}
