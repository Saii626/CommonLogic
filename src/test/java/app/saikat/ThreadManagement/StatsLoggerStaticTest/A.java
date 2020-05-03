package app.saikat.ThreadManagement.StatsLoggerStaticTest;

import java.time.Instant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;

public class A {

	private static int counter = 0;

	@Stats(rate = 1)
	private static void printStats(Logger statsLogger) {
		++counter;
		statsLogger.printf(Level.INFO, "A{counter:%d, currTime:%d}", counter, Instant.now().toEpochMilli());
	}

	public static int getCounter() {
		return counter;
	}
}