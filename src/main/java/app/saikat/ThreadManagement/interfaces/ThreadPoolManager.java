package app.saikat.ThreadManagement.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.ThreadManagement.exceptions.NoSuchThreadPoolException;



public interface ThreadPoolManager {

	/**
	 * Name of the global thread pool. If no threadpool name is provided while calling
	 * any method of this class, this name is used
	 */
	String GLOBAL_POOL = "global";

	Logger logger = LogManager.getLogger(ThreadPoolManager.class);

	/**
	 * Executes the runnable in a threadPool named GLOBAL_POOL
	 * @param runnable the runnable to execute
	 * @return a Future object which can be used to obtain the result
	 */
	default Future<?> execute(Runnable runnable) {
		try {
			return execute(runnable, GLOBAL_POOL);
		} catch (NoSuchThreadPoolException e) {
			logger.error("Error: {}", e);
			return null;
		}
	}

	default <T> Future<T> execute(Runnable runnable, T result) {
		try {
			return execute(runnable, result, GLOBAL_POOL);
		} catch (NoSuchThreadPoolException e) {
			logger.error("Error: {}", e);
			return null;
		}
	}

	/**
	 * Executes the callable in a threadPool named GLOBAL_POOL
	 * @param <T> Type of result returned from executing the callable
	 * @param callable the method to execute
	 * @return a Future object which can be used to obtain the result
	 */
	default <T> Future<T> execute(Callable<T> callable) {
		try {
			return execute(callable, GLOBAL_POOL);
		} catch (NoSuchThreadPoolException e) {
			logger.error("Error: {}", e);
			return null;
		}
	}

	/**
	 * Executes the runnable in specified threadpool
	 * @param runnable the runnable to execute
	 * @param name the name of the threadpool
	 * @return a Future object. Can be used to wait for completion or cancel this runnable
	 * @throws NoSuchThreadPoolException if no threadpool of specified name exists
	 */
	Future<?> execute(Runnable runnable, String name) throws NoSuchThreadPoolException;

	/**
	 * Executes the runnable in specified threadpool
	 * @param <T> type of result
	 * @param runnable the runnable to execute
	 * @param result result which will be used of future.get()
	 * @param name the name of the threadpool
	 * @return a Future object. Can be used to wait for completion or cancel this runnable
	 * @throws NoSuchThreadPoolException if no threadpool of specified name exists
	 */
	<T> Future<T> execute(Runnable runnable, T result, String name) throws NoSuchThreadPoolException;

	/**
	 * Executes the callable in specified threadpool
	 * @param <T> Type of result returned from executing the callable
	 * @param callable the method to execute
	 * @param name the name of the threadpool
	 * @return a Future object which can be used to obtain the result
	 * @throws NoSuchThreadPoolException if no threadpool of specified name exists
	 */
	<T> Future<T> execute(Callable<T> callable, String name) throws NoSuchThreadPoolException;

	/**
	 * Creates a new thread pool with a name if not already present. Use execute(runnable, name) to execute
	 * a runnable in this pool. Note that global threadpool is simply a threadpool with name "global"
	 * @param coreThreads no of always alive threads
	 * @param maxThreads no of max threads that can be spawned
	 * @param ttl time in seconds to wait after being idle, before killing non core threads
	 * @param name name of the private pool. name cannot be empty
	 * @return true if new threadpool was created with supplied configuration, false if a threadpool with such
	 *		 name already exists
	 */
	boolean allocateThreadPoolIfNotPresent(int coreThreads, int maxThreads, long ttl, String name);

}
