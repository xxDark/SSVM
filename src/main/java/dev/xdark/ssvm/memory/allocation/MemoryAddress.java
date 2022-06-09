package dev.xdark.ssvm.memory.allocation;

/**
 * Memory address key that may be used
 * instead of {@link Long} wrappers to reduce
 * GC pressure.
 *
 * @author xDark
 */
public final class MemoryAddress implements Comparable<MemoryAddress> {
	private long address;

	private MemoryAddress(long address) {
		this.address = address;
	}

	/**
	 * @return memory address.
	 */
	public long get() {
		return address;
	}

	/**
	 * Sets a memory address.
	 *
	 * @param address New address.
	 */
	public void set(long address) {
		this.address = address;
	}

	/**
	 * @return copy of this address.
	 */
	public MemoryAddress copy() {
		return new MemoryAddress(address);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof MemoryAddress && ((MemoryAddress) o).address == address;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(address);
	}

	@Override
	public int compareTo(MemoryAddress o) {
		return Long.compare(address, o.address);
	}

	/**
	 * @param address Memory address.
	 * @return new memory address.
	 */
	public static MemoryAddress of(long address) {
		return new MemoryAddress(address);
	}
}
