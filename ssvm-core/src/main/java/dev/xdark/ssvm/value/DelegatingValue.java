package dev.xdark.ssvm.value;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Value delegate wrapper.
 *
 * @author xDark
 */
@AllArgsConstructor
public abstract class DelegatingValue<V extends Value> implements Value {

	/**
	 * @return delegating value.
	 */
	@Delegate(types = Value.class)
	public abstract V getDelegate();

	/**
	 * Sets delegate value.
	 *
	 * @param delegate New value.
	 */
	public abstract void setDelegate(V delegate);

	@Override
	public boolean equals(Object obj) {
		return obj == this || getDelegate().equals(obj);
	}

	@Override
	public int hashCode() {
		return getDelegate().hashCode();
	}
}
