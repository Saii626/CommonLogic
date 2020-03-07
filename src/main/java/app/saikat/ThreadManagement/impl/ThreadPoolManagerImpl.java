package app.saikat.ThreadManagement.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.DIManagement.Provides;
import app.saikat.ThreadManagement.exceptions.NoSuchThreadPoolException;
import app.saikat.ThreadManagement.impl.ThreadPoolConfig.Config;
import app.saikat.ThreadManagement.interfaces.ThreadPoolManager;

// import app.saikat.DIManagement.Provides;

class ThreadPoolManagerImpl implements ThreadPoolManager {

	private final ThreadPoolConfig poolConfig;
	private final Map<String, ThreadPoolExecutor> threadPools;

	private Logger logger = LogManager.getLogger(ThreadPoolManager.class);


	public ThreadPoolManagerImpl(ThreadPoolConfig threadPoolConfig) {

		threadPools = new HashMap<>();
		poolConfig = threadPoolConfig;

		poolConfig.getThreadPoolConfig()
				.forEach((name, config) -> {
					logger.debug("Creating threadpool {} with config: {}", name, config);

					ThreadPoolExecutor executor = new ThreadPoolExecutor(config.getCoreThreads(),
							config.getMaxThreads(), config.getTtl(), TimeUnit.SECONDS, new SynchronousQueue<>(), new CustomThreadFactory(name));
					threadPools.put(name, executor);
				});

		Runtime.getRuntime()
				.addShutdownHook(new Thread(() -> threadPools.forEach((name, executor) -> executor.shutdown())));
	}

	@Override
	public Future<?> execute(Runnable runnable, String name) throws NoSuchThreadPoolException {
		ThreadPoolExecutor executor = getThreadPool(name);
		return executor.submit(runnable);
	}

	@Override
	public <T> Future<T> execute(Runnable runnable, T result, String name) throws NoSuchThreadPoolException {
		ThreadPoolExecutor executor = getThreadPool(name);
		return executor.submit(runnable, result);
	}

	@Override
	public <T> Future<T> execute(Callable<T> callable, String name) throws NoSuchThreadPoolException {
		ThreadPoolExecutor executor = getThreadPool(name);
		return executor.submit(callable);
	}

	@Override
	public boolean allocateThreadPoolIfNotPresent(int coreThreads, int maxThreads, long ttl, String name) {
		poolConfig.addToThreadPool(name, new Config(coreThreads, maxThreads, ttl));
		return false;
	}

	private ThreadPoolExecutor getThreadPool(String name) throws NoSuchThreadPoolException {
		if (!threadPools.containsKey(name))
			throw new NoSuchThreadPoolException(name);

		return threadPools.get(name);
	}

	@Provides
	public static ThreadPoolManager getThreadPoolManager(ThreadPoolConfig config) {
		return new ThreadPoolManagerImpl(config);
	}
}
