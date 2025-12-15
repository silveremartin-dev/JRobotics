/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor;

/**
 * Exception thrown when a sensor operation fails.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SensorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String sensorId;
    
    /**
     * Constructs a new sensor exception.
     * 
     * @param message the detail message
     */
    public SensorException(String message) {
        super(message);
        this.sensorId = null;
    }
    
    /**
     * Constructs a new sensor exception with sensor ID.
     * 
     * @param sensorId the sensor ID
     * @param message the detail message
     */
    public SensorException(String sensorId, String message) {
        super("[" + sensorId + "] " + message);
        this.sensorId = sensorId;
    }
    
    /**
     * Constructs a new sensor exception with cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public SensorException(String message, Throwable cause) {
        super(message, cause);
        this.sensorId = null;
    }
    
    /**
     * Constructs a new sensor exception with sensor ID and cause.
     * 
     * @param sensorId the sensor ID
     * @param message the detail message
     * @param cause the cause
     */
    public SensorException(String sensorId, String message, Throwable cause) {
        super("[" + sensorId + "] " + message, cause);
        this.sensorId = sensorId;
    }
    
    /**
     * Gets the sensor ID associated with this exception.
     * 
     * @return the sensor ID, or null if not available
     */
    public String getSensorId() {
        return sensorId;
    }
}
