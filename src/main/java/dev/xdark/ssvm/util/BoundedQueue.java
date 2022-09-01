package dev.xdark.ssvm.util;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xDark
 */
public final class BoundedQueue<E> extends AbstractQueue<E> {
	private final Queue<E> queue;
	private final AtomicInteger size;
	private final int maxSize;

	public BoundedQueue(Queue<E> queue, int maxSize) {
		this.queue = queue;
		this.maxSize = maxSize;
		size = new AtomicInteger(queue.size());
	}

	@Override
	public void clear() {
		queue.clear();
		size.set(0);
	}

	@Override
	public Iterator<E> iterator() {
		return new IteratorDecorator<>(queue.iterator());
	}

	@Override
	public int size() {
		return size.get();
	}

	@Override
	public boolean offer(E e) {
		Objects.requireNonNull(e);
		AtomicInteger size = this.size;
		int newSize = size.incrementAndGet();
		if (newSize >= maxSize) {
			size.decrementAndGet();
			return false;
		}
		queue.offer(e);
		return true;
	}

	@Override
	public E poll() {
		E element = queue.poll();
		if (element != null) {
			size.decrementAndGet();
		}
		return element;
	}

	@Override
	public E peek() {
		return queue.peek();
	}

	private static final class IteratorDecorator<E> implements Iterator<E> {
		private final Iterator<E> iterator;

		IteratorDecorator(Iterator<E> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public E next() {
			return iterator.next();
		}
	}
}
