package app.saikat.ThreadManagement.StatsLoggerStaticTest;

import java.time.Instant;

import app.saikat.Annotations.ThreadManagement.Stats;

public class A {

	private static int counter = 0;

	@SuppressWarnings("unused")
	private static class A_Stat {
		private int counter;
		private long timestamp;

		public A_Stat(int counter) {
			this.counter = counter;
			this.timestamp = Instant.now().toEpochMilli();
		}
	}

	@Stats(rate = 1)
	private static A_Stat getAStatistics() {
		++counter;
		return new A_Stat(counter);
	}

	public static int getCounter() {
		return counter;
	}
}