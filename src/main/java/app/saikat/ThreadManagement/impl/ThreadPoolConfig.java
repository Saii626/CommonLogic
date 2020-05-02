package app.saikat.ThreadManagement.impl;

import java.util.HashMap;
import java.util.Map;

import app.saikat.Annotations.DIManagement.Provides;
import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;
import app.saikat.ThreadManagement.interfaces.ThreadPoolManager;

public class ThreadPoolConfig {

	public static class Config {
		int coreThreads;
		int maxThreads;
		long ttl;

		public Config(int coreThreads, int maxThreads, long ttl) {
			this.coreThreads = coreThreads;
			this.maxThreads = maxThreads;
			this.ttl = ttl;
		}

		public int getCoreThreads() {
			return coreThreads;
		}

		public int getMaxThreads() {
			return maxThreads;
		}

		public long getTtl() {
			return ttl;
		}

		@Override
		public String toString() {
			return String.format("{coreThreads: %d, maxThreads: %d, ttl: %ds}", this.coreThreads, this.maxThreads, this.ttl);
		}
	}

	private Map<String, Config> threadPools;
	private int scheduledThreadsSize;

	public ThreadPoolConfig() {
		threadPools = new HashMap<>();
	}

	public Map<String, Config> getThreadPoolConfig() {
		return threadPools;
	}

	public int getScheduledThreadsSize() {
		return scheduledThreadsSize;
	}

	public void addToThreadPool(String name, Config config) {
		threadPools.put(name, config);
	}

	@Provides
	public static ThreadPoolConfig getThreadPoolConfig(ConfigurationManager configurationManager) {
		return configurationManager.<ThreadPoolConfig>get("thread_pools").orElseGet(() -> {
			ThreadPoolConfig config = new ThreadPoolConfig();
			config.addToThreadPool(ThreadPoolManager.GLOBAL_POOL, new Config(2, 4, 30));
			config.scheduledThreadsSize = 2;

			configurationManager.put("thread_pools", config);
			return config;
		});
	}
}
