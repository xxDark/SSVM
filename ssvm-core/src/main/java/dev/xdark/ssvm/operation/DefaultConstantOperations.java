package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultConstantOperations implements ConstantOperations {

	private final MemoryManager memoryManager;
	private final ThreadManager threadManager;
	private final StringPool stringPool;
	private final VMOperations ops;

	@Override
	public ObjectValue referenceValue(Object value) {
		convert:
		{
			if (value instanceof String) {
				return stringPool.intern((String) value);
			}
			if (value instanceof Type) {
				Type type = (Type) value;
				ExecutionContext<?> ctx = threadManager.currentOsThread().getBacktrace().peek();
				if (ctx == null) {
					ObjectValue loader = memoryManager.nullValue();
					switch (type.getSort()) {
						case Type.OBJECT:
						case Type.ARRAY:
							return ops.findClass(loader, type.getInternalName(), false).getOop();
						case Type.METHOD:
							return ops.methodType(loader, type);
						default:
							break convert;
					}
				} else {
					InstanceClass owner = ctx.getOwner();
					switch (type.getSort()) {
						case Type.OBJECT:
						case Type.ARRAY:
							return ops.findClass(owner, type, false).getOop();
						case Type.METHOD:
							return ops.methodType(owner, type);
						default:
							break convert;
					}
				}
			}
			if (value instanceof Handle) {
				ExecutionContext<?> ctx = threadManager.currentOsThread().getBacktrace().peek();
				Assertions.notNull(ctx, "cannot be called without call frame");
				return ops.linkMethodHandleConstant(ctx.getMethod().getOwner(), (Handle) value);
			}
		}
		throw new PanicException("Unsupported constant " + value + " " + value.getClass());
	}
}
