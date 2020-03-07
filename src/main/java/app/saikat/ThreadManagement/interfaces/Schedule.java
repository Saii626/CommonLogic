package app.saikat.ThreadManagement.interfaces;

public @interface Schedule {

	/**
	 * Duration in millis to wait after completion of current job and begining of next job.
	 * @return duration in millis
	 */
	long intervalDuration() default -1l;

	/**
	 * Way to calculate the duration. Options are between begining of job times (fixed-rate),
	 * or between end of current job and begining of next
	 * @return scheduler interval type
	 */
	SchedulerIntervalType intervalType() default SchedulerIntervalType.INTERVAL_BETWEEN_END_BEGINING;

}