package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultExceptionOperations implements ExceptionOperations {
	private final MemoryManager memoryManager;
	private final Symbols symbols;
	private final VMOperations ops;

	@Override
	public InstanceValue newStackTraceElement(ExecutionContext<?> frame) {
		VMOperations ops = this.ops;
		InstanceClass jc = symbols.java_lang_StackTraceElement();
		ops.initialize(jc);
		InstanceValue value = memoryManager.newInstance(jc);
		JavaMethod method = frame.getMethod();
		InstanceClass owner = method.getOwner();
		ops.putReference(value, "declaringClass", "Ljava/lang/String;", ops.newUtf8(owner.getName()));
		ops.putReference(value, "methodName", "Ljava/lang/String;", ops.newUtf8(method.getName()));
		String sourceFile = owner.getNode().sourceFile;
		if (sourceFile != null) {
			ops.putReference(value, "fileName", "Ljava/lang/String;", ops.newUtf8(sourceFile));
		}
		ops.putInt(value, "lineNumber", frame.getLineNumber());
		// TODO FieldOperations must also accept JavaField directly
		JavaField field = jc.getField("declaringClassObject", "Ljava/lang/Class;");
		if (field != null) {
			memoryManager.writeValue(value, field.getOffset(), owner.getOop());
		}
		return value;
	}

	@Override
	public void throwException(ObjectValue value) {
		if (value.isNull()) {
			value = newException(symbols.java_lang_NullPointerException());
		}
		throw new VMException((InstanceValue) value);
	}

	@Override
	public InstanceValue newException(InstanceClass javaClass, String message, ObjectValue cause) {
		VMOperations ops = this.ops;
		ops.initialize(javaClass);
		InstanceValue instance = memoryManager.newInstance(javaClass);
		if (message != null) {
			ops.putReference(instance, "detailMessage", "Ljava/lang/String;", ops.newUtf8(message));
		}
		if (cause != null) {
			ops.putReference(instance, "cause", "Ljava/lang/Throwable;", cause);
		}
		return instance;
	}
}
