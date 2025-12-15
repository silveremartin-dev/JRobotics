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
 * Base interface for all identifiable entities in the robotics system.
 * 
 * <p>Every major component (robots, sensors, actuators, processors) must have
 * a unique identifier that can be used for lookup and reference.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Identifiable {
    
    /**
     * Gets the unique identifier for this entity.
     * 
     * <p>Identifiers should be unique within their scope (e.g., within a robot
     * for components, globally for robots).</p>
     * 
     * @return the unique identifier, never null or empty
     */
    String getId();
}
