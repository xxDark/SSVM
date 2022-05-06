package dev.xdark.ssvm.value;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * Object value delegate wrapper.
 *
 * @author xDark
 */
public class DelegatingObjectValue<V extends ObjectValue> extends DelegatingValue<V> implements ObjectValue {

	@Delegate(types = ObjectValue.class)
	@Getter
	@Setter
	private V delegate;

	public DelegatingObjectValue(V delegate) {
		this.delegate = delegate;
	}
}
