package dev.xdark.ssvm.value;

/**
 * Simple delegating value.
 *
 * @author xDark
 */
public class SimpleDelegatingValue<V extends Value> extends DelegatingValue<V> {

	private V delegate;

	public SimpleDelegatingValue(V delegate) {
		this.delegate = delegate;
	}

	@Override
	public V getDelegate() {
		return delegate;
	}

	@Override
	public void setDelegate(V delegate) {
		this.delegate = delegate;
	}
}
