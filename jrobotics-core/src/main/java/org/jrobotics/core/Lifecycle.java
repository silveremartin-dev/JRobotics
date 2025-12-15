/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core;

/**
 * Lifecycle management interface for components that require initialization
 * and cleanup phases.
 * 
 * <p>
 * Components implementing this interface follow a defined lifecycle:
 * </p>
 * 
 * <pre>
 * CREATED → INITIALIZED → STARTED → RUNNING ↔ PAUSED → STOPPED → DESTROYED
 * </pre>
 * 
 * <p>
 * Lifecycle methods should be called in order:
 * </p>
 * <ol>
 * <li>{@link #initialize()} - Prepare resources, validate configuration</li>
 * <li>{@link #start()} - Begin operation</li>
 * <li>{@link #pause()} / {@link #resume()} - Optional pause/resume</li>
 * <li>{@link #stop()} - Stop operation gracefully</li>
 * <li>{@link #shutdown()} - Release all resources</li>
 * </ol>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Lifecycle {

    /**
     * Initializes this component.
     * 
     * <p>
     * This method should prepare all necessary resources and validate
     * configuration. It is called once before {@link #start()}.
     * </p>
     * 
     * @throws LifecycleException if initialization fails
     */
    void initialize() throws LifecycleException;

    /**
     * Starts this component's operation.
     * 
     * <p>
     * After this method returns, the component should be fully operational.
     * </p>
     * 
     * @throws LifecycleException    if start fails
     * @throws IllegalStateException if not initialized
     */
    void start() throws LifecycleException;

    /**
     * Pauses this component's operation.
     * 
     * <p>
     * The component should stop processing but retain its state for
     * quick resumption.
     * </p>
     * 
     * @throws LifecycleException    if pause fails
     * @throws IllegalStateException if not running
     */
    void pause() throws LifecycleException;

    /**
     * Resumes this component's operation after a pause.
     * 
     * @throws LifecycleException    if resume fails
     * @throws IllegalStateException if not paused
     */
    void resume() throws LifecycleException;

    /**
     * Stops this component's operation.
     * 
     * <p>
     * After this method returns, the component should no longer be
     * processing. Resources may still be held until {@link #shutdown()}.
     * </p>
     * 
     * @throws LifecycleException if stop fails
     */
    void stop() throws LifecycleException;

    /**
     * Shuts down this component and releases all resources.
     * 
     * <p>
     * After this method returns, the component cannot be restarted
     * without creating a new instance.
     * </p>
     * 
     * @throws LifecycleException if shutdown fails
     */
    void shutdown() throws LifecycleException;

    /**
     * Checks if this component is currently running.
     * 
     * @return true if running (started and not stopped/paused)
     */
    boolean isRunning();
}
