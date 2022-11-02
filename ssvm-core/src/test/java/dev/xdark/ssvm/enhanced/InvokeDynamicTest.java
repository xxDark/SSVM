package dev.xdark.ssvm.enhanced;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InvokeDynamicTest {

	@Test
	public void doTest() {
		TestUtil.test(InvokeDynamicTest.class, true);
	}

	@dev.xdark.ssvm.VMTest
	private static void testVariableCapture() {
		int[] box = {0};
		int v = ThreadLocalRandom.current().nextInt();
		Runnable r = () -> box[0] = v;
		r.run();
		if (box[0] != v) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testStream() {
		List<Integer> list = IntStream.range(0, 6)
			.filter(x -> true)
			.map(x -> x)
			.boxed().collect(Collectors.toList());
		if (!list.equals(Arrays.asList(0, 1, 2, 3, 4, 5))) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testMethodRef() {
		int v = ThreadLocalRandom.current().nextInt(64) + 100;
		AtomicInteger counter = new AtomicInteger();
		IntStream.range(0, v).forEach(counter::addAndGet);
		int x = 0;
		for (int i = 0; i < v; x += i++)
			;
		if (x != counter.get()) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testMethodRef2() {
		int v = ThreadLocalRandom.current().nextInt(8) + 8;
		ArrayList<String> list = IntStream.range(0, v)
			.mapToObj(Integer::toString)
			.collect(Collectors.toCollection(ArrayList::new));
		List<String> copy = Arrays.asList(new String[v]);
		while (v-- != 0) {
			copy.set(v, Integer.toString(v));
		}
		if (!list.equals(copy)) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testMethodRef3() {
		int v = ThreadLocalRandom.current().nextInt(8) + 8;
		String[] array = IntStream.range(0, v)
			.mapToObj(Integer::toHexString)
			.toArray(String[]::new);
		String[] copy = new String[v];
		while (v-- != 0) {
			copy[v] = Integer.toHexString(v);
		}
		if (!Arrays.equals(array, copy)) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testInnerLambda() {
		int v = ThreadLocalRandom.current().nextInt(20) + 8;
		int[] box = {0};
		Runnable r = () -> {
			Runnable r1 = () -> {
				box[0] += v;
			};
			for (int i = 0; i < v; i++) {
				r1.run();
			}
		};
		r.run();
		int x = 0;
		for (int i = 0; i < v; i++) {
			x += v;
		}
		if (box[0] != x) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testInnerLambda2() {
		int v = ThreadLocalRandom.current().nextInt(20) + 8;
		int[] box = {0};
		Runnable r = () -> {
			IntConsumer c = value -> box[0] += value;
			for (int i = 0; i < v; i++) {
				c.accept(i);
			}
		};
		r.run();
		int x = 0;
		for (int i = 0; i < v; i++) {
			x += i;
		}
		if (box[0] != x) {
			throw new IllegalStateException();
		}
	}

	private static long tmp1, tmp2;

	@dev.xdark.ssvm.VMTest
	private static void testLongCapture() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		long v1 = rng.nextLong();
		long v2 = rng.nextLong();
		tmp1 = v1;
		tmp2 = v2;
		Runnable r = (Runnable) () -> {
			if (v1 != tmp1 || v2 != tmp2) {
				throw new IllegalStateException();
			}
		};
		r.run();
	}

	private static long result = -1L;

	@dev.xdark.ssvm.VMTest
	private static void testBox() {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		long v = r.nextLong(0L, Long.MAX_VALUE);
		((LongConsumer) InvokeDynamicTest::setResult).accept(v);
		if (result != v) {
			throw new IllegalStateException();
		}
		v = r.nextLong(0L, Long.MAX_VALUE);
		((Consumer<Long>) InvokeDynamicTest::setResult2).accept(v);
		if (result != v) {
			throw new IllegalStateException();
		}
	}

	@dev.xdark.ssvm.VMTest
	private static void testArray() {
		IntStream.range(1, 100).mapToObj(Integer::valueOf).toArray(Integer[]::new);
	}

	@VMTest
	private static void testAltMetafactory() {
		Comparator<ChronoLocalDate> DATE_ORDER =
			(Comparator<ChronoLocalDate> & Serializable) (date1, date2) -> {
				return Long.compare(date1.toEpochDay(), date2.toEpochDay());
			};
		Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> DATE_TIME_ORDER =
			(Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> & Serializable) (dateTime1, dateTime2) -> {
				int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
				if (cmp == 0) {
					cmp = Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
				}
				return cmp;
			};
		Comparator<ChronoZonedDateTime<?>> INSTANT_ORDER =
			(Comparator<ChronoZonedDateTime<?>> & Serializable) (dateTime1, dateTime2) -> {
				int cmp = Long.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond());
				if (cmp == 0) {
					cmp = Long.compare(dateTime1.toLocalTime().getNano(), dateTime2.toLocalTime().getNano());
				}
				return cmp;
			};

	}

	private static void setResult(Long value) {
		result = value;
	}

	private static void setResult2(long value) {
		result = value;
	}
}
