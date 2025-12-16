/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread pool factory for robotics real-time tasks.
 * 
 * <p>
 * Provides specialized thread pools for different task types:
 * </p>
 * <ul>
 * <li><b>Sensor pool</b> - High priority for sensor reading</li>
 * <li><b>Control pool</b> - Real-time for control loops</li>
 * <li><b>Background pool</b> - Low priority for logging, metrics</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public final class RobotExecutors {

    private static final AtomicInteger threadCounter = new AtomicInteger(0);

    private RobotExecutors() {
    }

    /**
     * Creates a sensor reading thread pool.
     * High priority, fixed size.
     */
    public static ExecutorService sensorPool(int size) {
        return Executors.newFixedThreadPool(size, r -> {
            Thread t = new Thread(r, "jrobotics-sensor-" + threadCounter.getAndIncrement());
            t.setPriority(Thread.MAX_PRIORITY - 1);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Creates a control loop thread pool.
     * Maximum priority, single-threaded for determinism.
     */
    public static ScheduledExecutorService controlPool() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "jrobotics-control");
            t.setPriority(Thread.MAX_PRIORITY);
            t.setDaemon(false); // Keep alive
            return t;
        });
    }

    /**
     * Creates a background task pool.
     * Low priority, cached.
     */
    public static ExecutorService backgroundPool() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "jrobotics-bg-" + threadCounter.getAndIncrement());
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Creates a work-stealing pool for parallel algorithms.
     */
    public static ExecutorService parallelPool() {
        return new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                pool -> {
                    ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    t.setName("jrobotics-parallel-" + t.getPoolIndex());
                    return t;
                },
                null,
                true // asyncMode for tasks that don't need results
        );
    }

    /**
     * Creates a scheduled executor for periodic sensor polling.
     */
    public static ScheduledExecutorService scheduledPool(int size) {
        return Executors.newScheduledThreadPool(size, r -> {
            Thread t = new Thread(r, "jrobotics-scheduled-" + threadCounter.getAndIncrement());
            t.setPriority(Thread.NORM_PRIORITY + 2);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Gracefully shuts down an executor.
     */
    public static void shutdown(ExecutorService executor, long timeoutMs) {
        if (executor == null)
            return;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
