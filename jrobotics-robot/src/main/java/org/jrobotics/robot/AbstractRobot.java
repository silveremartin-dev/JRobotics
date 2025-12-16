/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot;

import org.jrobotics.core.*;
import org.jrobotics.core.event.DefaultEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation of the {@link Robot} interface.
 * 
 * <p>Provides common functionality for all robot implementations including
 * component management, lifecycle coordination, and event handling.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractRobot implements Robot {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractRobot.class);
    
    private final String id;
    private final String name;
    private final Set<Capability> capabilities = new HashSet<>();
    private final Map<String, Component> components = new ConcurrentHashMap<>();
    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.CREATED);
    private final EventBus eventBus;
    
    /**
     * Constructs a new robot with the specified parameters.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    protected AbstractRobot(String id, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.eventBus = new DefaultEventBus();
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
    public Set<Capability> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }
    
    @Override
    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }
    
    /**
     * Adds a capability to this robot.
     * 
     * @param capability the capability to add
     */
    protected void addCapability(Capability capability) {
        capabilities.add(capability);
    }
    
    @Override
    public Set<Component> getComponents() {
        return Collections.unmodifiableSet(new HashSet<>(components.values()));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(String id, Class<T> type) {
        Component component = components.get(id);
        if (component != null && type.isInstance(component)) {
            return (T) component;
        }
        return null;
    }
    
    @Override
    public void addComponent(Component component) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot add components while robot is running");
        }
        if (components.containsKey(component.getId())) {
            throw new IllegalArgumentException("Component with ID already exists: " + component.getId());
        }
        components.put(component.getId(), component);
        component.setRobot(this);
        logger.info("[{}] Added component: {} ({})", System.currentTimeMillis(), 
                component.getName(), component.getId());
    }
    
    @Override
    public boolean removeComponent(String componentId) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot remove components while robot is running");
        }
        Component removed = components.remove(componentId);
        if (removed != null) {
            removed.setRobot(null);
            logger.info("[{}] Removed component: {}", System.currentTimeMillis(), componentId);
            return true;
        }
        return false;
    }
    
    @Override
    public LifecycleState getState() {
        return state.get();
    }
    
    protected void setState(LifecycleState newState) {
        LifecycleState oldState = state.getAndSet(newState);
        if (oldState != newState) {
            logger.debug("[{}] Robot {} state changed: {} -> {}", 
                    System.currentTimeMillis(), id, oldState, newState);
        }
    }
    
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
    
    @Override
    public boolean isRunning() {
        return state.get() == LifecycleState.RUNNING;
    }
    
    @Override
    public void initialize() throws LifecycleException {
        if (state.get() != LifecycleState.CREATED) {
            throw new LifecycleException("Cannot initialize: not in CREATED state");
        }
        setState(LifecycleState.INITIALIZING);
        logger.info("[{}] Initializing robot: {}", System.currentTimeMillis(), name);
        
        try {
            // Initialize all components
            for (Component component : components.values()) {
                component.initialize();
            }
            doInitialize();
            setState(LifecycleState.INITIALIZED);
            logger.info("[{}] Robot {} initialized successfully", System.currentTimeMillis(), name);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to initialize robot: " + name, e);
        }
    }
    
    @Override
    public void start() throws LifecycleException {
        if (!state.get().canStart()) {
            throw new LifecycleException("Cannot start: not in startable state");
        }
        setState(LifecycleState.STARTING);
        logger.info("[{}] Starting robot: {}", System.currentTimeMillis(), name);
        
        try {
            // Start all components
            for (Component component : components.values()) {
                component.start();
            }
            doStart();
            setState(LifecycleState.RUNNING);
            logger.info("[{}] Robot {} started successfully", System.currentTimeMillis(), name);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to start robot: " + name, e);
        }
    }
    
    @Override
    public void pause() throws LifecycleException {
        if (state.get() != LifecycleState.RUNNING) {
            throw new LifecycleException("Cannot pause: not running");
        }
        setState(LifecycleState.PAUSING);
        
        try {
            for (Component component : components.values()) {
                component.pause();
            }
            doPause();
            setState(LifecycleState.PAUSED);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to pause robot: " + name, e);
        }
    }
    
    @Override
    public void resume() throws LifecycleException {
        if (state.get() != LifecycleState.PAUSED) {
            throw new LifecycleException("Cannot resume: not paused");
        }
        setState(LifecycleState.RESUMING);
        
        try {
            for (Component component : components.values()) {
                component.resume();
            }
            doResume();
            setState(LifecycleState.RUNNING);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to resume robot: " + name, e);
        }
    }
    
    @Override
    public void stop() throws LifecycleException {
        LifecycleState current = state.get();
        if (current != LifecycleState.RUNNING && current != LifecycleState.PAUSED) {
            throw new LifecycleException("Cannot stop: not running or paused");
        }
        setState(LifecycleState.STOPPING);
        logger.info("[{}] Stopping robot: {}", System.currentTimeMillis(), name);
        
        try {
            doStop();
            for (Component component : components.values()) {
                component.stop();
            }
            setState(LifecycleState.STOPPED);
            logger.info("[{}] Robot {} stopped successfully", System.currentTimeMillis(), name);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to stop robot: " + name, e);
        }
    }
    
    @Override
    public void shutdown() throws LifecycleException {
        setState(LifecycleState.DESTROYING);
        logger.info("[{}] Shutting down robot: {}", System.currentTimeMillis(), name);
        
        try {
            doShutdown();
            for (Component component : components.values()) {
                component.shutdown();
            }
            eventBus.shutdown();
            setState(LifecycleState.DESTROYED);
            logger.info("[{}] Robot {} shut down successfully", System.currentTimeMillis(), name);
        } catch (Exception e) {
            setState(LifecycleState.ERROR);
            throw new LifecycleException("Failed to shutdown robot: " + name, e);
        }
    }
    
    // Template methods for subclasses
    protected void doInitialize() throws Exception {}
    protected void doStart() throws Exception {}
    protected void doPause() throws Exception {}
    protected void doResume() throws Exception {}
    protected void doStop() throws Exception {}
    protected void doShutdown() throws Exception {}
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s, state=%s, components=%d]",
                getClass().getSimpleName(), id, name, state.get(), components.size());
    }
}
