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

import java.util.Set;

/**
 * Main robot interface defining the core contract for all robot implementations.
 * 
 * <p>A robot is a composite entity that contains sensors, actuators, and processors,
 * all managed through a unified lifecycle. Robots can expose their capabilities
 * for discovery and provide access to their components.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Robot robot = new WheeledRobot("MyBot");
 * robot.initialize();
 * robot.start();
 * 
 * // Get sensor data
 * double distance = robot.getSensor("front").read();
 * 
 * // Control actuators
 * robot.getActuator("leftWheel").execute(new MotorCommand(0.5));
 * 
 * robot.stop();
 * robot.shutdown();
 * }</pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 * @see Component
 * @see Capability
 * @see Lifecycle
 */
public interface Robot extends Lifecycle, Identifiable {
    
    /**
     * Gets the human-readable name of this robot.
     * 
     * @return the robot's name, never null
     */
    String getName();
    
    /**
     * Gets all capabilities of this robot.
     * 
     * <p>Capabilities represent what the robot can do, such as locomotion,
     * vision, manipulation, etc.</p>
     * 
     * @return an unmodifiable set of capabilities, never null
     */
    Set<Capability> getCapabilities();
    
    /**
     * Checks if this robot has a specific capability.
     * 
     * @param capability the capability to check
     * @return true if the robot has the capability, false otherwise
     */
    boolean hasCapability(Capability capability);
    
    /**
     * Gets all components attached to this robot.
     * 
     * @return an unmodifiable set of components, never null
     */
    Set<Component> getComponents();
    
    /**
     * Gets a component by its identifier.
     * 
     * @param <T> the component type
     * @param id the component identifier
     * @param type the expected component class
     * @return the component, or null if not found
     */
    <T extends Component> T getComponent(String id, Class<T> type);
    
    /**
     * Adds a component to this robot.
     * 
     * @param component the component to add
     * @throws IllegalStateException if the robot is running
     * @throws IllegalArgumentException if a component with the same ID exists
     */
    void addComponent(Component component);
    
    /**
     * Removes a component from this robot.
     * 
     * @param componentId the ID of the component to remove
     * @return true if the component was removed, false if not found
     * @throws IllegalStateException if the robot is running
     */
    boolean removeComponent(String componentId);
    
    /**
     * Gets the current state of this robot.
     * 
     * @return the current lifecycle state
     */
    LifecycleState getState();
    
    /**
     * Gets the event bus for this robot.
     * 
     * <p>The event bus allows components to communicate asynchronously.</p>
     * 
     * @return the event bus instance
     */
    EventBus getEventBus();
}
