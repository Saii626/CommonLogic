package app.saikat.CommonLogic.Threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import app.saikat.LogManagement.Logger;
import app.saikat.LogManagement.LoggerFactory;


public interface ThreadPoolManager {

    /**
     * Name of the global thread pool. If no threadpool name is provided while calling
     * any method of this class, this name is used
     */
    String GLOBAL_POOL = "global";

    Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    /**
     * Executes the runnable in a threadPool named GLOBAL_POOL
     * @param runnable the runnable to execute
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

    // /**
    //  * Cancels a previously posted runnable in GLOBAL_POOL. Prevents the execution of this runnable if
    //  * it is in the queue. If the runnable is executing, it will be interrupted if shouldInterrupt
    //  * is used
    //  * @param runnable the runnable to cancel
    //  * @param shouldInterrupt if the thread executing the method should be interrupted
    //  * @return the state of the runnable after trying to cancel
    //  * @throws NoSuchRunnableException if no such runnable was posted in GLOBAL_POOL
    //  */
    // default RunnableState cancel(Runnable runnable, boolean shouldInterrupt) throws NoSuchRunnableException {
    //     try {
    //         return cancel(runnable, shouldInterrupt, GLOBAL_POOL);
    //     } catch (NoSuchThreadPoolException e) {
    //         logger.error("Error: {}", e);
    //         return null;
    //     }
    // }

    // /**
    //  * Gets the current state of runnable posted in GLOBAL_POOL
    //  * @param runnable the runnable whose state is queried
    //  * @return the state of runnable
    //  * @throws NoSuchRunnableException if no such runnable was posted in GLOBAL_POOL
    //  */
    // default RunnableState getStateOf(Runnable runnable) throws NoSuchRunnableException {
    //     try {
    //         return getStateOf(runnable, GLOBAL_POOL);
    //     } catch (NoSuchThreadPoolException e) {
    //         logger.error("Error: {}", e);
    //         return null;
    //     }
    // }

    // /**
    //  * Waits synchronously for a previously posted runnable in GLOBAL_POOL to complete or get canceled,
    //  * whichever happens first
    //  * @param runnable the previously posted runnable
    //  * @return the state of runnable after runnable completed, canceled or time expired, whichever occurs first
    //  * @throws InterruptedException if the thread waiting was interrupted
    //  * @throws NoSuchRunnableException if no such runnable was posted in GLOBAL_POOL
    //  */
    // default RunnableState waitFor(Runnable runnable) throws InterruptedException, NoSuchRunnableException {
    //     try {
    //         return waitFor(runnable, GLOBAL_POOL);
    //     } catch (NoSuchThreadPoolException e) {
    //         logger.error("Error: {}", e);
    //         return null;
    //     }
    // }

    // /**
    //  * Waits synchronously for a previously posted runnable in GLOBAL_POOL to complete or get canceled,
    //  * for specified amount of milisecond
    //  * @param runnable the previously posted runnable
    //  * @param millis max no of millisecond to wait for the runnable to reach terminal state
    //  * @return the state of runnable after runnable completed, canceled or time expired, whichever occurs first
    //  * @throws InterruptedException if the thread waiting was interrupted
    //  * @throws NoSuchRunnableException if no such runnable was posted in GLOBAL_POOL
    //  */
    // default RunnableState waitFor(Runnable runnable, long millis) throws InterruptedException, NoSuchRunnableException {
    //     try {
    //         return waitFor(runnable, millis, GLOBAL_POOL);
    //     } catch (NoSuchThreadPoolException e) {
    //         logger.error("Error: {}", e);
    //         return null;
    //     }
    // }

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

    // /**
    //  * Cancels a previously posted runnable in specified thread. Prevents the execution of this runnable if
    //  * it is in the queue. If the runnable is executing, it will be interrupted if shouldInterrupt
    //  * is used
    //  * @param runnable the runnable to cancel
    //  * @param shouldInterrupt if the thread executing the method should be interrupted
    //  * @param name name of the threadpool where this runnable was posted
    //  * @return the state of the runnable after trying to cancel
    //  * @throws NoSuchThreadPoolException if no threadpool of specified name exists
    //  * @throws NoSuchRunnableException if no such runnable was posted in specified pool
    //  */
    // RunnableState cancel(Runnable runnable, boolean shouldInterrupt, String name) throws NoSuchThreadPoolException, NoSuchRunnableException;

    // /**
    //  * Gets the current state of runnable posted in GLOBAL_POOL
    //  * @param runnable the runnable whose state is queried
    //  * @param name name of the threadpool where this runnable was posted
    //  * @return the state of runnable
    //  * @throws NoSuchRunnableException if no such runnable was posted in specified pool
    //  * @throws NoSuchThreadPoolException if no threadpool of specified name exists
    //  */

    // RunnableState getStateOf(Runnable runnable, String name) throws NoSuchThreadPoolException, NoSuchRunnableException;

    // /**
    //  * Waits synchronously for a previously posted runnable in specified threadpool to complete or get canceled,
    //  * whichever happens first
    //  * @param runnable the previously posted runnable
    //  * @param name name of the threadpool where this runnable was posted
    //  * @return the state of runnable after runnable completed, canceled or time expired, whichever occurs first
    //  * @throws InterruptedException if the thread waiting was interrupted
    //  * @throws NoSuchRunnableException if no such runnable was posted in specified pool
    //  * @throws NoSuchThreadPoolException if no threadpool of specified name exists
    //  */
    // default RunnableState waitFor(Runnable runnable, String name)
    //         throws InterruptedException, NoSuchThreadPoolException, NoSuchRunnableException {
    //     RunnableState state = getStateOf(runnable, name);
    //     synchronized (state) {
    //         if (RunnableState.TERMINAL.contains(state))
    //             return state;

    //         logger.debug("Thread {} waiting for {} to complete", Thread.currentThread().getName(), runnable);
    //         while (!RunnableState.TERMINAL.contains(state) && !Thread.interrupted()) {
    //             state.wait(2000);
    //             logger.warn("Thread {} still waiting for {} to complete", Thread.currentThread().getName(), runnable);
    //         }
    //         return state;
    //     }
    // }

    // /**
    //  * Waits synchronously for a previously posted runnable in specified threadpool to complete or get canceled,
    //  * for specified amount of milisecond
    //  * @param runnable the previously posted runnable
    //  * @param millis max no of millisecond to wait for the runnable to reach terminal state
    //  * @param name name of the threadpool where this runnable was posted
    //  * @return the state of runnable after runnable completed, canceled or time expired, whichever occurs first
    //  * @throws InterruptedException if the thread waiting was interrupted
    //  * @throws NoSuchRunnableException if no such runnable was posted in specified pool
    //  * @throws NoSuchThreadPoolException if no threadpool of specified name exists
    //  */
    // default RunnableState waitFor(Runnable runnable, long millis, String name)
    //         throws InterruptedException, NoSuchThreadPoolException, NoSuchRunnableException {
    //     RunnableState state = getStateOf(runnable, name);
    //     synchronized (state) {
    //         if (RunnableState.TERMINAL.contains(state))
    //             return state;

    //         logger.debug("Thread {} waiting for {} to complete", Thread.currentThread().getName(), runnable);

    //         long startTime = System.currentTimeMillis();
    //         long timeLeft = millis;
    //         while (!RunnableState.TERMINAL.contains(state) && timeLeft > 0 && !Thread.interrupted()) {
    //             state.wait(timeLeft);
    //             timeLeft -= System.currentTimeMillis() - startTime;
    //             logger.warn("Thread {} still waiting for {} to complete", Thread.currentThread().getName(), runnable);
    //         }
    //         return state;
    //     }
    // }

    /**
     * Creates a new thread pool with a name if not already present. Use execute(runnable, name) to execute
     * a runnable in this pool. Note that global threadpool is simply a threadpool with name "global"
     * @param coreThreads no of always alive threads
     * @param maxThreads no of max threads that can be spawned
     * @param ttl time in seconds to wait after being idle, before killing non core threads
     * @param name name of the private pool. name cannot be empty
     * @return true if new threadpool was created with supplied configuration, false if a threadpool with such
     *         name already exists
     */
    boolean allocateThreadPoolIfNotPresent(int coreThreads, int maxThreads, long ttl, String name);

}
