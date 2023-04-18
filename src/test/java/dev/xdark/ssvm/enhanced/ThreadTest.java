package dev.xdark.ssvm.enhanced;

import org.junit.jupiter.api.Test;

public class ThreadTest {

	@Test
	public void doTest() {
		TestUtil.test(ThreadTest.class, true);
	}

	@VMTest
	private static void testThreadStart() throws InterruptedException {
		Thread thread = new Thread(() -> {
			System.out.println("thread started");
			try {
				Thread.sleep(50L);
			} catch (InterruptedException ignored) {
			}
			System.out.println("thread slept");
		});
		thread.start();
		thread.join();
		System.out.println("thread finished");
	}
}
