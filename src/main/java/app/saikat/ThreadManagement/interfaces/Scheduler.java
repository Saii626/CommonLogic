package app.saikat.ThreadManagement.interfaces;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.ThreadManagement.impl.TaskProvider;

public interface Scheduler {

	/**
	 * Schedules a runnable with fixed rate
	 * @param command the command to run
	 * @param interval the time in sec to wait before invoking the runnable angain
	 * @return a scheduled future object
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable command, double interval);

	/**
	 * Schedules a runnable with fixed delay
	 * @param command the command to run
	 * @param interval the time in sec to wait after an invokation before
	 *  invoking the runnable angain
	 * @return a scheduled future object
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, double interval);

	/**
	 * Gets the taskmap stored by scheduler for @Stat and @Schedule beans
	 * @return taskpmap of scheduler for @Stat and @Schedule beans
	 */
	Map<DIBean<?>, Set<Tuple<TaskProvider<?, ?>, ScheduledFuture<?>>>> getTasksMap();

}