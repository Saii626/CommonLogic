package app.saikat.ThreadManagement.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import app.saikat.Annotations.DIManagement.Provides;
import app.saikat.DIManagement.Impl.Repository.Repository;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.ThreadManagement.AnnotationSupport.ScheduleBeanManager;
import app.saikat.ThreadManagement.interfaces.Scheduler;

class SchedulerImpl implements Scheduler {

	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private final Map<DIBean<?>, Set<Tuple<TaskProvider<?, ?>, ScheduledFuture<?>>>> tasksMap = new ConcurrentHashMap<>();

	public SchedulerImpl(Repository repository, ThreadPoolConfig poolConfig) {
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(poolConfig.getScheduledThreadsSize(),
				new CustomThreadFactory("scheduler"));

		ScheduleBeanManager scheduleBeanManager = repository.getBeanManagerOfType(ScheduleBeanManager.class);

		scheduleBeanManager.getToBeScheduledTasks()
				.parallelStream()
				.forEach(t -> t.setSchedulerAndExecute(this));
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, double interval) {
		return scheduledThreadPoolExecutor.scheduleAtFixedRate(command, 1, Math.round(1 / interval), TimeUnit.SECONDS);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, double interval) {
		return scheduledThreadPoolExecutor.scheduleWithFixedDelay(command, 1, Math.round(1 / interval),
				TimeUnit.SECONDS);
	}

	@Override
	public Map<DIBean<?>, Set<Tuple<TaskProvider<?, ?>, ScheduledFuture<?>>>> getTasksMap() {
		return tasksMap;
	}

	@Provides
	public static Scheduler getScheduler(Repository repository, ThreadPoolConfig threadPoolConfig) {
		return new SchedulerImpl(repository, threadPoolConfig);
	}
}