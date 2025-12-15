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

import org.jrobotics.core.Component;

/**
 * Base interface for all actuators in the robotics system.
 * 
 * <p>Actuators are components that perform physical actions such as
 * moving motors, rotating servos, or controlling other output devices.</p>
 * 
 * @param <C> the type of command this actuator accepts
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Actuator<C> extends Component {
    
    /**
     * Executes an actuator command.
     * 
     * @param command the command to execute
     * @throws ActuatorException if execution fails
     */
    void execute(C command) throws ActuatorException;
    
    /**
     * Executes an actuator command asynchronously.
     * 
     * @param command the command to execute
     * @param callback optional callback when execution completes
     * @throws ActuatorException if execution fails to start
     */
    void executeAsync(C command, ActuatorCallback callback) throws ActuatorException;
    
    /**
     * Stops the actuator immediately.
     * 
     * <p>This is an emergency stop that should halt any motion
     * as quickly as possible.</p>
     */
    void emergencyStop();
    
    /**
     * Checks if the actuator supports feedback.
     * 
     * @return true if feedback is available
     */
    boolean hasFeedback();
    
    /**
     * Gets the current position/state feedback.
     * 
     * @return the current feedback value, or null if not available
     */
    Object getFeedback();
    
    /**
     * Gets the actuator's minimum output value.
     * 
     * @return the minimum value
     */
    double getMinValue();
    
    /**
     * Gets the actuator's maximum output value.
     * 
     * @return the maximum value
     */
    double getMaxValue();
    
    /**
     * Gets the actuator's current output value.
     * 
     * @return the current value
     */
    double getCurrentValue();
    
    /**
     * Checks if the actuator is currently moving/active.
     * 
     * @return true if active
     */
    boolean isActive();
}
