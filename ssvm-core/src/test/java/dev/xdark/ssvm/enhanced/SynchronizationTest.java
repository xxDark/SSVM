package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

public class SynchronizationTest {

	@Test
	public void doTest() {
		TestUtil.test(SynchronizationTest.class, true);
	}

	@VMTest
	private static void testLock1() {
		Object o = new Object();
		synchronized (o) {
			if (!Thread.holdsLock(o)) {
				throw new IllegalStateException();
			}
		}
	}

	@VMTest
	private static synchronized void testLock2() {
		if (!Thread.holdsLock(SynchronizationTest.class)) {
			throw new IllegalStateException();
		}
	}

	@VMTest
	private static void testNestedLocking() {
		Object lock = new Object();
		synchronized (lock) {
			innerLock(lock);
		}
	}

	@VMTest
	private static void testLockWithException() {
		Object o = new Object();
		try {
			synchronized (o) {
				throw new IllegalStateException();
			}
		} catch (IllegalStateException ignored) {
		}
	}

	@VMTest
	private static void testLockStackUnwind() {
		Object o = new Object();
		try {
			synchronized (o) {
				consume(o, throwException(o));
			}
		} catch (IllegalStateException ignored) {
		}
	}

	private static Object throwException(Object o) {
		throw new IllegalStateException();
	}

	private static void consume(Object v1, Object v2) {

	}

	private static void innerLock(Object lock) {
		synchronized (lock) {
			//noinspection unused
			int x = 5;
		}
	}
}
