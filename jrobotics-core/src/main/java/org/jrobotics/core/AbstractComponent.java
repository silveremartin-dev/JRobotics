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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation of the {@link Component} interface.
 * 
 * <p>
 * Provides common functionality for all components including lifecycle
 * management, state tracking, and logging.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractComponent implements Component {

    /** Logger for this component. */
    protected final Logger logger;

    private final String id;
    private final String name;
    private final ComponentType type;

    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.CREATED);
    private volatile Robot robot;
    private volatile boolean enabled = true;

    /**
     * Constructs a new component with the specified parameters.
     * 
     * @param id   the unique identifier
     * @param name the human-readable name
     * @param type the component type
     * @throws NullPointerException if any parameter is null
     */
    protected AbstractComponent(String id, String name, ComponentType type) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ComponentType getType() {
        return type;
    }

    @Override
    public LifecycleState getState() {
        return state.get();
    }

    /**
     * Sets the lifecycle state.
     * 
     * @param newState the new state
     */
    protected void setState(LifecycleState newState) {
        LifecycleState oldState = state.getAndSet(newState);
        if (oldState != newState) {
            logger.debug("[{}] State changed: {} -> {}", id, oldState, newState);
        }
    }

    @Override
    public Robot getRobot() {
        return robot;
    }

    @Override
    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.debug("[{}] Enabled: {}", id, enabled);
    }

    @Override
    public boolean isRunning() {
        return state.get() == LifecycleState.RUNNING;
    }

    @Override
    public void initialize() throws LifecycleException {
        if (state.get() != LifecycleState.CREATED) {
            throw new LifecycleException("Cannot initialize: not in CREATED state", state.get());
        }
        setState(LifecycleState.INITIALIZING);
        logger.info("[{}] Initializing component: {}", id, name);

        try {
            doInitialize();
            setState(LifecycleState.INITIALIZED);
            logger.info("[{}] Component initialized successfully", id);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to initialize component: " + id, e, state.get());
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (!state.get().canStart()) {
            throw new LifecycleException("Cannot start: not in startable state", state.get());
        }
        setState(LifecycleState.STARTING);
        logger.info("[{}] Starting component: {}", id, name);

        try {
            doStart();
            setState(LifecycleState.RUNNING);
            logger.info("[{}] Component started successfully", id);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to start component: " + id, e, state.get());
        }
    }

    @Override
    public void pause() throws LifecycleException {
        if (state.get() != LifecycleState.RUNNING) {
            throw new LifecycleException("Cannot pause: not running", state.get());
        }
        setState(LifecycleState.PAUSING);
        logger.debug("[{}] Pausing component", id);

        try {
            doPause();
            setState(LifecycleState.PAUSED);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to pause component: " + id, e, state.get());
        }
    }

    @Override
    public void resume() throws LifecycleException {
        if (state.get() != LifecycleState.PAUSED) {
            throw new LifecycleException("Cannot resume: not paused", state.get());
        }
        setState(LifecycleState.RESUMING);
        logger.debug("[{}] Resuming component", id);

        try {
            doResume();
            setState(LifecycleState.RUNNING);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to resume component: " + id, e, state.get());
        }
    }

    @Override
    public void stop() throws LifecycleException {
        LifecycleState currentState = state.get();
        if (currentState != LifecycleState.RUNNING && currentState != LifecycleState.PAUSED) {
            throw new LifecycleException("Cannot stop: not running or paused", currentState);
        }
        setState(LifecycleState.STOPPING);
        logger.info("[{}] Stopping component", id);

        try {
            doStop();
            setState(LifecycleState.STOPPED);
            logger.info("[{}] Component stopped successfully", id);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to stop component: " + id, e, state.get());
        }
    }

    @Override
    public void shutdown() throws LifecycleException {
        setState(LifecycleState.DESTROYING);
        logger.info("[{}] Shutting down component", id);

        try {
            doShutdown();
            setState(LifecycleState.DESTROYED);
            logger.info("[{}] Component shut down successfully", id);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to shutdown component: " + id, e, state.get());
        }
    }

    /**
     * Template method for initialization logic.
     * 
     * <p>
     * Subclasses should override this to perform their initialization.
     * </p>
     * 
     * @throws Exception if initialization fails
     */
    protected void doInitialize() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Template method for start logic.
     * 
     * @throws Exception if start fails
     */
    protected void doStart() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Template method for pause logic.
     * 
     * @throws Exception if pause fails
     */
    protected void doPause() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Template method for resume logic.
     * 
     * @throws Exception if resume fails
     */
    protected void doResume() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Template method for stop logic.
     * 
     * @throws Exception if stop fails
     */
    protected void doStop() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Template method for shutdown logic.
     * 
     * @throws Exception if shutdown fails
     */
    protected void doShutdown() throws Exception {
        // Default implementation does nothing
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s, type=%s, state=%s]",
                getClass().getSimpleName(), id, name, type, state.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractComponent that = (AbstractComponent) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
