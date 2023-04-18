package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultExceptionOperations implements ExceptionOperations {
	private final MemoryManager memoryManager;
	private final ThreadManager threadManager;
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
		JavaMethod m = javaClass.getMethod("<init>", "()V");
		Locals locals = threadManager.currentThreadStorage().newLocals(m);
		locals.setReference(0, instance);
		ops.invokeVoid(m, locals);
		if (message != null) {
			ops.putReference(instance, "detailMessage", "Ljava/lang/String;", ops.newUtf8(message));
		}
		if (cause != null) {
			ops.putReference(instance, "cause", "Ljava/lang/Throwable;", cause);
		}
		return instance;
	}

	@Override
	public Exception toJavaException(InstanceValue oop) {
		VMOperations ops = this.ops;
		String msg = ops.readUtf8(ops.getReference(oop, "detailMessage", "Ljava/lang/String;"));
		Exception exception = new Exception(msg);
		ObjectValue backtrace = ops.getReference(oop, "backtrace", "Ljava/lang/Object;");
		if (!backtrace.isNull()) {
			ArrayValue arrayValue = (ArrayValue) backtrace;
			StackTraceElement[] stackTrace = IntStream.range(0, arrayValue.getLength())
				.mapToObj(i -> {
					InstanceValue value = (InstanceValue) arrayValue.getReference(i);
					String declaringClass = ops.readUtf8(ops.getReference(value, "declaringClass", "Ljava/lang/String;"));
					String methodName = ops.readUtf8(ops.getReference(value, "methodName", "Ljava/lang/String;"));
					String fileName = ops.readUtf8(ops.getReference(value, "fileName", "Ljava/lang/String;"));
					int line = ops.getInt(value, "lineNumber");
					return new StackTraceElement(declaringClass, methodName, fileName, line);
				})
				.toArray(StackTraceElement[]::new);
			Collections.reverse(Arrays.asList(stackTrace));
			exception.setStackTrace(stackTrace);
		}
		ObjectValue cause = ops.getReference(oop, "cause", "Ljava/lang/Throwable;");
		if (!cause.isNull() && cause != oop) {
			exception.initCause(toJavaException((InstanceValue) cause));
		}
		ObjectValue suppressedExceptions = ops.getReference(oop, "suppressedExceptions", "Ljava/util/List;");
		if (!suppressedExceptions.isNull()) {
			InstanceClass cl = (InstanceClass) ops.findClass(memoryManager.nullValue(), "java/util/ArrayList", false);
			if (cl == suppressedExceptions.getJavaClass()) {
				InstanceValue value = (InstanceValue) suppressedExceptions;
				int size = ops.getInt(value, "size");
				ArrayValue array = (ArrayValue) ops.getReference(value, "elementData", "[Ljava/lang/Object;");
				for (int i = 0; i < size; i++) {
					InstanceValue ref = (InstanceValue) array.getReference(i);
					exception.addSuppressed(ref == oop ? exception : toJavaException(ref));
				}
			}
		}
		return exception;
	}
}
