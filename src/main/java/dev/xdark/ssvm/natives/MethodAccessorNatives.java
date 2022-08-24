package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.AbstractValueSink;
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
			ObjectValue values = locals.loadReference(2);
			Type[] types = mn.getArgumentTypes();
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
				mn = vm.getPublicLinkResolver().resolveVirtualMethod(instance, name, desc);
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

	private static ObjectValue boxSink(VirtualMachine vm, ReflectionSink sink, Type type) {
		VMHelper helper = vm.getHelper();
		switch (type.getSort()) {
			case Type.LONG:
				return helper.boxLong(sink.l_value);
			case Type.DOUBLE:
				return helper.boxDouble(Double.longBitsToDouble(sink.l_value));
			case Type.INT:
				return helper.boxInt(sink.i_value);
			case Type.FLOAT:
				return helper.boxFloat(Float.intBitsToFloat(sink.i_value));
			case Type.CHAR:
				return helper.boxChar((char) sink.i_value);
			case Type.SHORT:
				return helper.boxShort((short) sink.i_value);
			case Type.BYTE:
				return helper.boxByte((byte) sink.i_value);
			case Type.BOOLEAN:
				return helper.boxBoolean(sink.i_value != 0);
			default:
				ObjectValue ref = sink.r_value;
				if (ref == null) {
					throw new PanicException("Expected reference");
				}
				return ref;
		}
	}

	private static final class ReflectionSink extends AbstractValueSink {
		long l_value;
		int i_value;
		ObjectValue r_value;

		@Override
		public void acceptReference(ObjectValue value) {
			check();
			r_value = value;
		}

		@Override
		public void acceptLong(long value) {
			check();
			l_value = value;
		}

		@Override
		public void acceptDouble(double value) {
			acceptLong(Double.doubleToRawLongBits(value));
		}

		@Override
		public void acceptInt(int value) {
			check();
			i_value = value;
		}

		@Override
		public void acceptFloat(float value) {
			acceptInt(Float.floatToRawIntBits(value));
		}
	}
}
