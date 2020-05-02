package app.saikat.ThreadManagement.impl;

import java.time.Instant;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;

public class CustomThreadFactory implements ThreadFactory {

	// Stores factory instance specific data
	private AtomicInteger id = new AtomicInteger(0);
	private String poolName;
	
	// Shared data across all factories
	private static Logger logger = LogManager.getLogger(CustomThreadFactory.class);
	private static AtomicInteger aliveThreadCount = new AtomicInteger(0);

	@SuppressWarnings("unused")				// Converted to string for printing. Uses reflection to figure out fields
	public static class ThreadStat {
		private int noOfAliveThreads;

		public ThreadStat(int noOfAliveThreads) {
			this.noOfAliveThreads = noOfAliveThreads;
		}
	}

	@Stats
	private static ThreadStat threadStat() {
		return new ThreadStat(aliveThreadCount.get());
	}

	public CustomThreadFactory(String poolName) {
		this.poolName = poolName;
	}

	@Override
	public Thread newThread(Runnable r) {

		Thread t = new Thread(() -> {
			logger.debug("Starting thread {} at {}. Total {} input_threads alive", Thread.currentThread().getName(), Instant.now().toEpochMilli(), aliveThreadCount.incrementAndGet());
			id.incrementAndGet();

			r.run();

			logger.debug("Stopping thread {} at {}. Total {} input_threads alive", Thread.currentThread().getName(), Instant.now().toEpochMilli(), aliveThreadCount.decrementAndGet());
		});
		t.setName(String.format("%s_worker_%d", poolName, id.get()));

		return t;
	}

}