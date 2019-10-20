package app.saikat.CommonLogic.Threads;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import app.saikat.LogManagement.Logger;
import app.saikat.LogManagement.LoggerFactory;

import app.saikat.CommonLogic.Threads.ThreadPoolConfig.Config;
import app.saikat.DIManagement.Provides;

class ThreadPoolManagerImpl implements ThreadPoolManager {

    private final ThreadPoolConfig poolConfig;
    private final Map<String, ThreadPoolExecutor> threadPools;

    private Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);


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

    // @Override
    // public RunnableState cancel(Runnable runnable, boolean shouldInterrupt, String name)
    //         throws NoSuchThreadPoolException, NoSuchRunnableException {
    //     Tuple<RunnableHolder, RunnableState> info;
    //     ThreadPoolExecutor executor = getThreadPool(name);
    //     // executor.
    //     // executor.submit()

    //     synchronized (runnableList) {
    //         info = runnableList.stream()
    //                 .filter(entry -> entry.first.get()
    //                         .equals(runnable))
    //                 .findFirst()
    //                 .orElseThrow(() -> new NoSuchRunnableException(runnable, name));
    //     }

    //     synchronized (info) {
    //         switch (info.second) {
    //             case EXECUTING:
    //                 if (shouldInterrupt) info.first.getRunningOnThread().interrupt();
    //             case WAITING:
    //                 info.second = RunnableState.CANCELLED;
    //                 break;
    //             case CANCELLED:
    //             case COMPLETED:
    //                 break;
    //         }
    //     }

    //     return info.second;
    // }

    // @Override
    // public RunnableState getStateOf(Runnable runnable, String name) throws NoSuchThreadPoolException, NoSuchRunnableException {
    //     return null;
    // }

    @Override
    public boolean allocateThreadPoolIfNotPresent(int coreThreads, int maxThreads, long ttl, String name) {
        poolConfig.addToThreadPool(name, new Config(coreThreads, maxThreads, ttl));
        return false;
    }

    // private Runnable wrap(Tuple<RunnableHolder, RunnableState> info) {
    //     return new Runnable() {

    //         @Override
    //         public void run() {
    //             synchronized (info) {
    //                 // If completed or canceled, return inmmediately
    //                 if (info.second.isTerminal()) return;

    //                 info.second = RunnableState.EXECUTING;
    //                 info.first.setRunningOnThread(Thread.currentThread());
    //                 info.notifyAll();
    //             }

    //             try {
    //                 info.first.get()
    //                         .run();
    //             } finally {

    //                 synchronized (info) {

    //                     // If not canceled already, change the status to completed
    //                     if (!info.second.isTerminal()) info.second = RunnableState.COMPLETED;

    //                     info.first.convertToWeakRef();
    //                     info.first.setRunningOnThread(null);
    //                     info.notifyAll();
    //                 }
    //             }
    //         }
    //     };
    // }

    private ThreadPoolExecutor getThreadPool(String name) throws NoSuchThreadPoolException {
        if (!threadPools.containsKey(name))
            throw new NoSuchThreadPoolException(name);

        return threadPools.get(name);
    }

    // @Override
    // public void execute(Runnable runnable) {
    //     execute(runnable, "global");
    // }

    // @Override
    // public void execute(Runnable runnable, String name) {
    //     Tuple<RunnableHolder, RunnableState> info = Tuple.of(RunnableHolder.of(runnable), RunnableState.WAITING);

    //     synchronized (runnableList) {
    //         runnableList.add(info);
    //     }
    //     threadPools.get(name).execute(() -> {
    //         synchronized (info) {
    //             info.second = RunnableState.EXECUTING;
    //             info.notifyAll();
    //         }

    //         runnable.run();

    //         synchronized (info) {
    //             info.second = RunnableState.COMPLETE;
    //             info.first.convertToWeakRef();
    //             info.notifyAll();
    //         }
    //     });
    // }

    // @Override
    // public void waitFor(Runnable runnable) throws NoSuchElementException {
    //     Tuple<RunnableHolder, RunnableState> infoTuple;
    //     synchronized (runnableList) {
    //         infoTuple = runnableList.stream().filter(t -> t.first.get().equals(runnable)).findAny()
    //                 .orElseThrow(() -> new NoSuchElementException("Runnable {} not posted for execution"));
    //     }

    //     if (infoTuple.second.equals(RunnableState.COMPLETE))
    //         return;

    //     synchronized (infoTuple) {
    //         while (!infoTuple.second.equals(RunnableState.COMPLETE)) {
    //             try {
    //                 infoTuple.wait(1000);
    //                 logger.warn("Still executing {}", runnable);
    //             } catch (InterruptedException e) {
    //                 logger.error("Error: {}", e);
    //             }
    //         }
    //     }
    // }

    // @Override
    // public boolean waitFor(Runnable runnable, long millis) {
    //     Tuple<RunnableHolder, RunnableState> infoTuple;
    //     synchronized (runnableList) {
    //         infoTuple = runnableList.stream().filter(t -> t.first.get().equals(runnable)).findAny()
    //                 .orElseThrow(() -> new NoSuchElementException("Runnable {} not posted for execution"));
    //     }

    //     if (infoTuple.second.equals(RunnableState.COMPLETE))
    //         return true;

    //     synchronized (infoTuple) {
    //         long beginTime =
    //         while (!infoTuple.second.equals(RunnableState.COMPLETE)) {
    //             try {
    //                 infoTuple.wait(millis);
    //                 logger.warn("Still executing {}", runnable);
    //             } catch (InterruptedException e) {
    //                 logger.error("Error: {}", e);
    //             }
    //         }
    //     }
    // }

    // @Override
    // public void allocateThreadPoolIfNotPresent(int coreThreads, int maxThreads, String name) {

    // }

    // @Override
    // public RunnableState getStateOf(Runnable runnable) {
    //     return null;
    // }

    @Provides
    public static ThreadPoolManager getThreadPoolManager(ThreadPoolConfig config) {
        return new ThreadPoolManagerImpl(config);
    }
}
