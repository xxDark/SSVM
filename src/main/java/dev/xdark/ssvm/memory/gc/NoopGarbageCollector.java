package dev.xdark.ssvm.memory.gc;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Garbage collector that does not do anything.
 *
 * @author xDark
 */
public final class NoopGarbageCollector implements GarbageCollector {
	private static final GCHandle NOOP_HANDLE = new GCHandle() {
		@Override
		public GCHandle retain() {
			return this;
		}

		@Override
		public boolean release() {
			return false;
		}
	};

	@Override
	public int reservedHeaderSize() {
		return 0;
	}

	@Override
	public GCHandle makeHandle(ObjectValue value) {
		return NOOP_HANDLE;
	}

	@Override
	public boolean invoke() {
		return true;
	}
}
