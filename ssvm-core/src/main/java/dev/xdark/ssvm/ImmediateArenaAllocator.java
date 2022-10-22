package dev.xdark.ssvm;

import dev.xdark.jlinker.Arena;
import dev.xdark.jlinker.ArenaAllocator;

import java.util.ArrayDeque;
import java.util.Deque;

@Deprecated
final class ImmediateArenaAllocator<T> implements ArenaAllocator<T> {
	@Override
	public Arena<T> push() {
		return new Arena<T>() {
			private final Deque<T> deque = new ArrayDeque<>();

			@Override
			public void push(T t) {
				deque.push(t);
			}

			@Override
			public T poll() {
				return deque.poll();
			}

			@Override
			public void close() {
			}
		};
	}
}
