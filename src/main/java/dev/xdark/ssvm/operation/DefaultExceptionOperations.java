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
	private final ClassOperations classOperations;
	private final FieldOperations fieldOperations;
	private final StringOperations stringOperations;

	@Override
	public InstanceValue newStackTraceElement(ExecutionContext<?> frame) {
		InstanceClass jc = symbols.java_lang_StackTraceElement();
		classOperations.initialize(jc);
		InstanceValue value = memoryManager.newInstance(jc);
		FieldOperations fops = fieldOperations;
		StringOperations sops = stringOperations;
		JavaMethod method = frame.getMethod();
		InstanceClass owner = method.getOwner();
		fops.putReference(value, "declaringClass", "Ljava/lang/String;", sops.newUtf8(owner.getName()));
		fops.putReference(value, "methodName", "Ljava/lang/String;", sops.newUtf8(method.getName()));
		String sourceFile = owner.getNode().sourceFile;
		if (sourceFile != null) {
			fops.putReference(value, "fileName", "Ljava/lang/String;", sops.newUtf8(sourceFile));
		}
		fops.putInt(value, "lineNumber", frame.getLineNumber());
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
		classOperations.initialize(javaClass);
		InstanceValue instance = memoryManager.newInstance(javaClass);
		if (message != null) {
			fieldOperations.putReference(instance, "detailMessage", "Ljava/lang/String;", stringOperations.newUtf8(message));
		}
		if (cause != null) {
			fieldOperations.putReference(instance, "cause", "Ljava/lang/Throwable;", cause);
		}
		return instance;
	}
}
