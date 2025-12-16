/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.fusion;

/**
 * Interface for sensor fusion algorithms.
 * 
 * <p>Sensor fusion combines data from multiple sensors to produce
 * more accurate and reliable estimates.</p>
 * 
 * @param <T> the fused output type
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface SensorFusion<T> {
    
    /**
     * Updates the fusion with a new measurement.
     * 
     * @param sensorId the sensor ID
     * @param measurement the measurement value
     * @param timestamp the measurement timestamp
     */
    void update(String sensorId, double[] measurement, long timestamp);
    
    /**
     * Gets the current fused estimate.
     * 
     * @return the fused estimate
     */
    T getEstimate();
    
    /**
     * Gets the estimation uncertainty/covariance.
     * 
     * @return the uncertainty
     */
    double[] getUncertainty();
    
    /**
     * Resets the fusion state.
     */
    void reset();
    
    /**
     * Predicts the state forward in time.
     * 
     * @param dt the time step in seconds
     */
    void predict(double dt);
}
