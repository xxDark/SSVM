package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
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
			VMOperations ops = vm.getOperations();
			InstanceValue m = locals.loadReference(0);
			int slot = ops.getInt(m, "slot");
			InstanceClass declaringClass = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(m, "clazz", "Ljava/lang/Class;")));
			JavaMethod mn = declaringClass.getMethodBySlot(slot);
			if (mn == null) {
				ops.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
				return Result.ABORT;
			}
			ObjectValue instance = locals.loadReference(1);
			boolean isStatic = (mn.getModifiers() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				ops.throwException(vm.getSymbols().java_lang_IllegalArgumentException());
				return Result.ABORT;
			}
			ObjectValue values = locals.loadReference(2);
			JavaClass[] types = mn.getArgumentTypes();
			ArrayValue passedArgs = null;
			if (!values.isNull()) {
				passedArgs = (ArrayValue) values;
				ops.checkEquals(passedArgs.getLength(), types.length);
			} else {
				ops.checkEquals(types.length, 0);
			}
			Locals args;
			String name = mn.getName();
			String desc = mn.getDesc();
			int offset;
			if (isStatic) {
				offset = 0;
				args = vm.getThreadStorage().newLocals(mn);
			} else {
				mn = vm.getRuntimeResolver().resolveVirtualMethod(instance, name, desc);
				offset = 1;
				args = vm.getThreadStorage().newLocals(mn);
				args.setReference(0, instance);
			}
			if (passedArgs != null) {
				Util.copyReflectionArguments(vm, types, passedArgs, args, offset);
			}
			ReflectionSink sink = new ReflectionSink();
			ops.invoke(mn, args, sink);
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
		VMOperations ops = vm.getOperations();
		switch (type.getSort()) {
			case Type.LONG:
				return ops.boxLong(sink.longValue);
			case Type.DOUBLE:
				return ops.boxDouble(Double.longBitsToDouble(sink.longValue));
			case Type.INT:
				return ops.boxInt(sink.intValue);
			case Type.FLOAT:
				return ops.boxFloat(Float.intBitsToFloat(sink.intValue));
			case Type.CHAR:
				return ops.boxChar((char) sink.intValue);
			case Type.SHORT:
				return ops.boxShort((short) sink.intValue);
			case Type.BYTE:
				return ops.boxByte((byte) sink.intValue);
			case Type.BOOLEAN:
				return ops.boxBoolean(sink.intValue != 0);
			default:
				ObjectValue ref = sink.referenceValue;
				if (ref == null) {
					throw new PanicException("Expected reference");
				}
				return ref;
		}
	}
}
