package app.saikat.ThreadManagement.impl;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;

public class CustomThreadFactory implements ThreadFactory {

	// Stores factory instance specific data
	private String poolName;
	private AtomicInteger id = new AtomicInteger(0);
	private AtomicInteger aliveThreadCount = new AtomicInteger(0);

	// Shared data across all factories
	private static final Logger logger = LogManager.getLogger(CustomThreadFactory.class);
	private static final Set<WeakReference<CustomThreadFactory>> allInstancesOfThreadFactory = Collections
			.synchronizedSet(new HashSet<>());

	@Stats
	private static void threadStat(Logger statsLogger) {
		synchronized (allInstancesOfThreadFactory) {
			Iterator<WeakReference<CustomThreadFactory>> iterator = allInstancesOfThreadFactory.iterator();

			while (iterator.hasNext()) {
				CustomThreadFactory factoryInstance = iterator.next()
						.get();

				if (factoryInstance == null) {
					iterator.remove();
				} else {
					statsLogger.printf(Level.INFO, "ThreadPool{name=%s, aliveThreads=%d, currId:%d}",
							factoryInstance.poolName, factoryInstance.id.get(), factoryInstance.aliveThreadCount.get());
				}
			}

		}
	}

	public CustomThreadFactory(String poolName) {
		this.poolName = poolName;
		allInstancesOfThreadFactory.add(new WeakReference<>(this));
	}

	@Override
	public Thread newThread(Runnable r) {

		Thread t = new Thread(() -> {
			logger.debug("Starting thread {} at {}. Total {} input_threads alive", Thread.currentThread()
					.getName(),
					Instant.now()
							.toEpochMilli(),
					aliveThreadCount.incrementAndGet());

			r.run();

			logger.debug("Stopping thread {} at {}. Total {} input_threads alive", Thread.currentThread()
					.getName(),
					Instant.now()
							.toEpochMilli(),
					aliveThreadCount.decrementAndGet());
		});
		t.setName(String.format("%s_worker_%d", poolName, id.incrementAndGet()));

		return t;
	}

}