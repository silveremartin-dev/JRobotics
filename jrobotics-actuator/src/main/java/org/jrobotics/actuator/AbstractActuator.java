/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator;

import org.jrobotics.core.AbstractComponent;
import org.jrobotics.core.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base implementation of the {@link Actuator} interface.
 * 
 * <p>Provides common functionality for all actuators including lifecycle
 * management, async execution, and emergency stop.</p>
 * 
 * @param <C> the type of command this actuator accepts
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractActuator<C> extends AbstractComponent implements Actuator<C> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractActuator.class);
    
    private final double minValue;
    private final double maxValue;
    private volatile double currentValue;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean emergencyStopped = new AtomicBoolean(false);
    
    private final ExecutorService executor;
    
    /**
     * Constructs a new actuator with the specified parameters.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param minValue the minimum output value
     * @param maxValue the maximum output value
     */
    protected AbstractActuator(String id, String name, double minValue, double maxValue) {
        super(id, name, ComponentType.ACTUATOR);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = 0;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Actuator-" + id);
            t.setDaemon(true);
            return t;
        });
    }
    
    @Override
    public void execute(C command) throws ActuatorException {
        if (!isRunning()) {
            throw new ActuatorException(getId(), "Actuator is not running");
        }
        if (emergencyStopped.get()) {
            throw new ActuatorException(getId(), "Emergency stop active");
        }
        if (!isEnabled()) {
            logger.debug("[{}] Actuator disabled, ignoring command", System.currentTimeMillis());
            return;
        }
        
        try {
            active.set(true);
            logger.debug("[{}] Executing command on actuator {}: {}", 
                    System.currentTimeMillis(), getId(), command);
            doExecute(command);
        } catch (Exception e) {
            logger.error("[{}] Execution failed for actuator {}: {}", 
                    System.currentTimeMillis(), getId(), e.getMessage());
            throw new ActuatorException(getId(), "Execution failed: " + e.getMessage(), e);
        } finally {
            active.set(false);
        }
    }
    
    @Override
    public void executeAsync(C command, ActuatorCallback callback) throws ActuatorException {
        if (!isRunning()) {
            throw new ActuatorException(getId(), "Actuator is not running");
        }
        if (emergencyStopped.get()) {
            throw new ActuatorException(getId(), "Emergency stop active");
        }
        
        executor.submit(() -> {
            try {
                execute(command);
                if (callback != null) {
                    callback.onComplete(getId(), true, null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onComplete(getId(), false, e.getMessage());
                }
            }
        });
    }
    
    /**
     * Template method for executing actuator commands.
     * 
     * <p>Subclasses must implement this to perform actual command execution.</p>
     * 
     * @param command the command to execute
     * @throws Exception if execution fails
     */
    protected abstract void doExecute(C command) throws Exception;
    
    @Override
    public void emergencyStop() {
        logger.warn("[{}] Emergency stop activated for actuator {}", 
                System.currentTimeMillis(), getId());
        emergencyStopped.set(true);
        active.set(false);
        doEmergencyStop();
    }
    
    /**
     * Template method for emergency stop implementation.
     * 
     * <p>Subclasses should override this to implement hardware-specific
     * emergency stop behavior.</p>
     */
    protected void doEmergencyStop() {
        // Default implementation does nothing extra
    }
    
    /**
     * Resets the emergency stop state.
     * 
     * <p>This should only be called after ensuring it is safe to resume operation.</p>
     */
    public void resetEmergencyStop() {
        emergencyStopped.set(false);
        logger.info("[{}] Emergency stop reset for actuator {}", 
                System.currentTimeMillis(), getId());
    }
    
    /**
     * Checks if emergency stop is active.
     * 
     * @return true if emergency stopped
     */
    public boolean isEmergencyStopped() {
        return emergencyStopped.get();
    }
    
    @Override
    public boolean hasFeedback() {
        return false; // Override in subclasses that have feedback
    }
    
    @Override
    public Object getFeedback() {
        return null; // Override in subclasses that have feedback
    }
    
    @Override
    public double getMinValue() {
        return minValue;
    }
    
    @Override
    public double getMaxValue() {
        return maxValue;
    }
    
    @Override
    public double getCurrentValue() {
        return currentValue;
    }
    
    /**
     * Sets the current output value.
     * 
     * <p>The value is clamped to the valid range.</p>
     * 
     * @param value the new value
     */
    protected void setCurrentValue(double value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
    }
    
    @Override
    public boolean isActive() {
        return active.get();
    }
    
    @Override
    protected void doStop() throws Exception {
        emergencyStop();
        super.doStop();
    }
    
    @Override
    protected void doShutdown() throws Exception {
        executor.shutdownNow();
        super.doShutdown();
    }
}
