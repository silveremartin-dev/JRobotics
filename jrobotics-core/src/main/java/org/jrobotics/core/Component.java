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
 * Base interface for all robot components (sensors, actuators, processors).
 * 
 * <p>
 * Components are the building blocks of a robot. Each component has an
 * identifier, a type, and follows the standard lifecycle pattern.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Component extends Lifecycle, Identifiable {

    /**
     * Gets the type of this component.
     * 
     * @return the component type, never null
     */
    ComponentType getType();

    /**
     * Gets the human-readable name of this component.
     * 
     * @return the component name, never null
     */
    String getName();

    /**
     * Gets the current state of this component.
     * 
     * @return the current lifecycle state
     */
    LifecycleState getState();

    /**
     * Gets the robot this component is attached to.
     * 
     * @return the parent robot, or null if not attached
     */
    Robot getRobot();

    /**
     * Sets the parent robot for this component.
     * 
     * <p>
     * This is typically called by the robot when the component is added.
     * </p>
     * 
     * @param robot the parent robot
     */
    void setRobot(Robot robot);

    /**
     * Checks if this component is enabled.
     * 
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Enables or disables this component.
     * 
     * <p>
     * Disabled components should not produce data or consume commands.
     * </p>
     * 
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);
}
