package app.saikat.ThreadManagement.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomThreadFactory implements ThreadFactory {

	// Stores factory instance specific data
	private AtomicInteger id = new AtomicInteger(0);
	private String poolName;
	
	// Shared data across all factories
	private static Logger logger = LogManager.getLogger(CustomThreadFactory.class);
	private static AtomicInteger aliveThreadCount = new AtomicInteger(0);
	private static List<Thread> allThreads = Collections.synchronizedList(new ArrayList<>());

	public CustomThreadFactory(String poolName) {
		this.poolName = poolName;
	}

	@Override
	public Thread newThread(Runnable r) {

		Thread t = new Thread(() -> {
			logger.debug("Starting thread {} at {}. Total {} input_threads alive", Thread.currentThread().getName(), Instant.now().toEpochMilli(), aliveThreadCount.incrementAndGet());

			synchronized(allThreads) {
				allThreads.add(Thread.currentThread());
			}

			r.run();

			synchronized(allThreads) {
				allThreads.remove(Thread.currentThread());
			}

			logger.debug("Stopping thread {} at {}. Total {} input_threads alive", Thread.currentThread().getName(), Instant.now().toEpochMilli(), aliveThreadCount.decrementAndGet());
		});
		t.setName(String.format("%s_worker_%d", poolName, id.get()));

		return t;
	}

}