package dev.xdark.ssvm.value;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * Instance value delegate wrapper.
 *
 * @author xDark
 */
public class DelegatingInstanceValue<V extends InstanceValue> extends DelegatingObjectValue<V> implements InstanceValue {

	public DelegatingInstanceValue(V delegate) {
		super(delegate);
	}

	@Delegate(types = InstanceValue.class)
	@Override
	public V getDelegate() {
		return super.getDelegate();
	}
}
