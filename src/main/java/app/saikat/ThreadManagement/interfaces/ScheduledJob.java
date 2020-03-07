package app.saikat.ThreadManagement.interfaces;

import java.lang.reflect.Method;
import java.time.Duration;

public interface ScheduledJob {

	/**
	 * 
	 * @return number of times the job was executed
	 */
	int getTotalExecutionCount();

	/**
	 * 
	 * @return number of times job executed without throwing error
	 */
	int getToTalErrorFreeExecutionCount();

	/**
	 * 
	 * @return total duration of execution of this job
	 */
	Duration totalRunDuration();

	/**
	 * 
	 * @return the schedule of this job
	 */
	Schedule getSchedule();

	/**
	 * Underlying method that is invoked
	 * @return
	 */
	Method getJob();

}