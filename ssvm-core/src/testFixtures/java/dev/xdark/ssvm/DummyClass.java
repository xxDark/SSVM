package dev.xdark.ssvm;

public class DummyClass {
	public static int random() {
		return (int) (Math.random() * 100000);
	}

	public int randomInstance() {
		return (int) (Math.random() * 100000) + hashCode();
	}
}
