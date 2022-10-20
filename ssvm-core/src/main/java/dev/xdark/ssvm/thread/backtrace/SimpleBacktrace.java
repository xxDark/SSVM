package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.ExecutionRequest;
import dev.xdark.ssvm.util.CloseableUtil;
import dev.xdark.ssvm.value.sink.ValueSink;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Simple backtrace.
 *
 * @author xDark
 */
public final class SimpleBacktrace implements Backtrace {

	private static final int RESERVED_FRAMES = 12;
	private final List<ExecutionContext<?>> frames;
	private int frame;

	public SimpleBacktrace(int frameCount) {
		frames = Arrays.asList(new ExecutionContext[Math.max(frameCount, RESERVED_FRAMES + 4)]);
	}

	@Override
	public <R extends ValueSink> ExecutionContext<R> push(ExecutionRequest<R> request) {
		int frameIndex = this.frame;
		List<ExecutionContext<?>> frames = this.frames;
		if (frameIndex == frames.size() - RESERVED_FRAMES) {
			// TODO stack overflow
		}
		SimpleExecutionContext<R> ctx = (SimpleExecutionContext<R>) frames.get(frameIndex);
		if (ctx == null) {
			ctx = new SimpleExecutionContext<>();
			frames.set(frameIndex, ctx);
		}
		ctx.init(request.getMethod(), request.getStack(), request.getLocals(), request.getResultSink());
		this.frame = frameIndex + 1;
		return ctx;
	}

	@Override
	public ExecutionContext<?> peek() {
		int frame = this.frame;
		return frame == 0 ? null : frames.get(frame - 1);
	}

	@Override
	public ExecutionContext<?> at(int index) {
		return frames.get(frame - 1 - index);
	}

	@Override
	public void pop() {
		CloseableUtil.close(frames.get(--frame));
	}

	@Override
	public int depth() {
		return frame;
	}

	@Override
	public Iterator<ExecutionContext<?>> iterator() {
		return frames.subList(0, frame).iterator();
	}
}
