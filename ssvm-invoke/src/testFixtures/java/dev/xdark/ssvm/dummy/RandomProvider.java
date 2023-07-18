package dev.xdark.ssvm.dummy;

/**
 * Dummy class for random values
 */
public class RandomProvider {
	public static int random() {
		return (int) (Math.random() * 100000);
	}

	public int randomInstance() {
		return (int) (Math.random() * 100000) + hashCode();
	}
}
