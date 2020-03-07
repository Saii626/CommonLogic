package app.saikat.ThreadManagement.interfaces;

import java.lang.reflect.Method;
import java.util.Set;

public interface Scheduler {

	ScheduledJob scheduleJob(Method method, int interval, SchedulerIntervalType intervalType);

	ScheduledJob scheduleJob(Runnable runnable, int interval, SchedulerIntervalType intervalType);

	Set<ScheduledJob> allJobs();

	void stopJob(ScheduledJob job, boolean waitForComplete, boolean force);

}