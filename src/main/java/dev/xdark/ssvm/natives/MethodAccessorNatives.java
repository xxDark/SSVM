package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.Helper;
import dev.xdark.ssvm.util.Operations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ReflectionSink;
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
		InstanceClass accessor = (InstanceClass) vm.findBootstrapClass("jdk/internal/reflect/NativeMethodAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceClass) vm.findBootstrapClass("sun/reflect/NativeMethodAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeMethodAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "invoke0", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			Operations ops = vm.getOperations();
			InstanceValue m = locals.loadReference(0);
			int slot = ops.getInt(m, "slot");
			InstanceClass declaringClass = (InstanceClass) ((JavaValue<JavaClass>) ops.getReference(m, "clazz", "Ljava/lang/Class;")).getValue();
			Helper helper = vm.getHelper();
			JavaMethod mn = declaringClass.getMethodBySlot(slot);
			if (mn == null) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			ObjectValue instance = locals.loadReference(1);
			boolean isStatic = (mn.getModifiers() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
			}
			ObjectValue values = locals.loadReference(2);
			JavaClass[] types = mn.getArgumentTypes();
			ArrayValue passedArgs = null;
			if (!values.isNull()) {
				passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
			} else {
				helper.checkEquals(types.length, 0);
			}
			Locals args;
			String name = mn.getName();
			String desc = mn.getDesc();
			int offset;
			if (isStatic) {
				offset = 0;
				args = vm.getThreadStorage().newLocals(mn);
			} else {
				mn = vm.getLinkResolver().resolveVirtualMethod(instance, name, desc);
				offset = 1;
				args = vm.getThreadStorage().newLocals(mn);
				args.setReference(0, instance);
			}
			if (passedArgs != null) {
				Util.copyReflectionArguments(vm, types, passedArgs, args, offset);
			}
			ReflectionSink sink = new ReflectionSink();
			helper.invoke(mn, args, sink);
			ObjectValue result;
			if (!sink.isSet()) {
				result = vm.getMemoryManager().nullValue(); // void
			} else {
				result = boxSink(vm, sink, mn.getReturnType());
			}

			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	private static ObjectValue boxSink(VirtualMachine vm, ReflectionSink sink, JavaClass type) {
		Helper helper = vm.getHelper();
		switch (type.getSort()) {
			case Type.LONG:
				return helper.boxLong(sink.longValue);
			case Type.DOUBLE:
				return helper.boxDouble(Double.longBitsToDouble(sink.longValue));
			case Type.INT:
				return helper.boxInt(sink.intValue);
			case Type.FLOAT:
				return helper.boxFloat(Float.intBitsToFloat(sink.intValue));
			case Type.CHAR:
				return helper.boxChar((char) sink.intValue);
			case Type.SHORT:
				return helper.boxShort((short) sink.intValue);
			case Type.BYTE:
				return helper.boxByte((byte) sink.intValue);
			case Type.BOOLEAN:
				return helper.boxBoolean(sink.intValue != 0);
			default:
				ObjectValue ref = sink.referenceValue;
				if (ref == null) {
					throw new PanicException("Expected reference");
				}
				return ref;
		}
	}
}
