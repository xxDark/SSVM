package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;

import java.util.*;

/**
 * Basic implementation for a backtrace.
 *
 * @author xDark
 */
public final class SimpleBacktrace implements Backtrace {

	private final List<ExecutionContext> backtrace;

	private SimpleBacktrace(List<ExecutionContext> backtrace) {
		this.backtrace = backtrace;
	}

	public SimpleBacktrace() {
		this(new ArrayList<>());
	}

	@Override
	public ExecutionContext first() {
		var backtrace = this.backtrace;
		return backtrace.isEmpty() ? null : backtrace.get(0);
	}

	@Override
	public ExecutionContext last() {
		var backtrace = this.backtrace;
		return backtrace.isEmpty() ? null : backtrace.get(backtrace.size() - 1);
	}

	@Override
	public ExecutionContext get(int index) {
		return backtrace.get(index);
	}

	@Override
	public int count() {
		return backtrace.size();
	}

	@Override
	public void push(ExecutionContext ctx) {
		backtrace.add(ctx);
	}

	@Override
	public ExecutionContext pop() {
		var backtrace = this.backtrace;
		return backtrace.remove(backtrace.size() - 1);
	}

	@Override
	public Backtrace copy() {
		return new SimpleBacktrace(new ArrayList<>(backtrace));
	}

	@Override
	public Iterator<ExecutionContext> iterator() {
		return backtrace.iterator();
	}
}
