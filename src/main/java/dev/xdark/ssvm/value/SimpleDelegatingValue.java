package dev.xdark.ssvm.value;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple delegating value.
 *
 * @author xDark
 */
public class SimpleDelegatingValue<V extends Value> extends DelegatingValue<V> {

	@Getter
	@Setter
	private V delegate;

	public SimpleDelegatingValue(V delegate) {
		this.delegate = delegate;
	}
}
