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
 * Listener interface for receiving sensor data events.
 * 
 * @param <T> the type of sensor data
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
@FunctionalInterface
public interface SensorDataListener<T> {
    
    /**
     * Called when new sensor data is available.
     * 
     * @param sensorId the ID of the sensor that produced the data
     * @param data the sensor data
     * @param timestampNanos the timestamp in nanoseconds when the data was acquired
     */
    void onData(String sensorId, T data, long timestampNanos);
}
