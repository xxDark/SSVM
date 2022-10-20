package dev.xdark.ssvm.value;

import lombok.experimental.Delegate;

/**
 * Array value delegate wrapper.
 *
 * @author xDark
 */
public class DelegatingArrayValue<V extends ArrayValue> extends DelegatingObjectValue<V> implements ArrayValue {

	public DelegatingArrayValue(V delegate) {
		super(delegate);
	}

	@Delegate(types = ArrayValue.class)
	@Override
	public V getDelegate() {
		return super.getDelegate();
	}
}
