package dev.xdark.ssvm.thread;

import lombok.val;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic implementation for a backtrace.
 *
 * @author xDark
 */
public final class SimpleBacktrace implements Backtrace {

	private final List<StackFrame> backtrace;

	private SimpleBacktrace(List<StackFrame> backtrace) {
		this.backtrace = backtrace;
	}

	public SimpleBacktrace() {
		this(new ArrayList<>());
	}

	@Override
	public StackFrame first() {
		val backtrace = this.backtrace;
		return backtrace.isEmpty() ? null : backtrace.get(0);
	}

	@Override
	public StackFrame last() {
		val backtrace = this.backtrace;
		return backtrace.isEmpty() ? null : backtrace.get(backtrace.size() - 1);
	}

	@Override
	public StackFrame get(int index) {
		return backtrace.get(index);
	}

	@Override
	public int count() {
		return backtrace.size();
	}

	@Override
	public void push(StackFrame frame) {
		backtrace.add(frame);
	}

	@Override
	public StackFrame pop() {
		val backtrace = this.backtrace;
		return backtrace.remove(backtrace.size() - 1);
	}

	@Override
	public Backtrace copy() {
		return new SimpleBacktrace(new ArrayList<>(backtrace));
	}

	@Override
	public Iterator<StackFrame> iterator() {
		return backtrace.iterator();
	}
}
