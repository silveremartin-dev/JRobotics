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

/**
 * Exception thrown when an actuator operation fails.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ActuatorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String actuatorId;
    
    /**
     * Constructs a new actuator exception.
     * 
     * @param message the detail message
     */
    public ActuatorException(String message) {
        super(message);
        this.actuatorId = null;
    }
    
    /**
     * Constructs a new actuator exception with actuator ID.
     * 
     * @param actuatorId the actuator ID
     * @param message the detail message
     */
    public ActuatorException(String actuatorId, String message) {
        super("[" + actuatorId + "] " + message);
        this.actuatorId = actuatorId;
    }
    
    /**
     * Constructs a new actuator exception with cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ActuatorException(String message, Throwable cause) {
        super(message, cause);
        this.actuatorId = null;
    }
    
    /**
     * Constructs a new actuator exception with actuator ID and cause.
     * 
     * @param actuatorId the actuator ID
     * @param message the detail message
     * @param cause the cause
     */
    public ActuatorException(String actuatorId, String message, Throwable cause) {
        super("[" + actuatorId + "] " + message, cause);
        this.actuatorId = actuatorId;
    }
    
    /**
     * Gets the actuator ID associated with this exception.
     * 
     * @return the actuator ID, or null if not available
     */
    public String getActuatorId() {
        return actuatorId;
    }
}
