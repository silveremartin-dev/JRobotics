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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple 1D Kalman filter implementation.
 * 
 * <p>Implements the standard Kalman filter equations for a single state variable.
 * Useful for filtering noisy sensor readings.</p>
 * 
 * <p>Reference: Welch & Bishop, "An Introduction to the Kalman Filter"</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class KalmanFilter1D {
    
    private static final Logger logger = LoggerFactory.getLogger(KalmanFilter1D.class);
    
    private double x;  // State estimate
    private double p;  // Estimate covariance
    private final double q;  // Process noise covariance
    private final double r;  // Measurement noise covariance
    
    /**
     * Constructs a 1D Kalman filter.
     * 
     * @param initialEstimate the initial state estimate
     * @param initialCovariance the initial estimate covariance
     * @param processNoise the process noise (Q)
     * @param measurementNoise the measurement noise (R)
     */
    public KalmanFilter1D(double initialEstimate, double initialCovariance,
                          double processNoise, double measurementNoise) {
        this.x = initialEstimate;
        this.p = initialCovariance;
        this.q = processNoise;
        this.r = measurementNoise;
    }
    
    /**
     * Creates a filter with default parameters.
     * 
     * @return the filter
     */
    public static KalmanFilter1D standard() {
        return new KalmanFilter1D(0, 1, 0.01, 0.1);
    }
    
    /**
     * Prediction step.
     * 
     * @param controlInput optional control input (can be 0)
     */
    public void predict(double controlInput) {
        // State prediction: x = x + u
        x = x + controlInput;
        
        // Covariance prediction: P = P + Q
        p = p + q;
    }
    
    /**
     * Update step with new measurement.
     * 
     * @param measurement the measurement value
     */
    public void update(double measurement) {
        // Kalman gain: K = P / (P + R)
        double k = p / (p + r);
        
        // State update: x = x + K * (z - x)
        x = x + k * (measurement - x);
        
        // Covariance update: P = (1 - K) * P
        p = (1 - k) * p;
        
        logger.trace("Kalman update: x={}, P={}, K={}", x, p, k);
    }
    
    /**
     * Combined predict and update.
     * 
     * @param measurement the new measurement
     * @return the filtered estimate
     */
    public double filter(double measurement) {
        predict(0);
        update(measurement);
        return x;
    }
    
    /**
     * Gets the current state estimate.
     * 
     * @return the estimate
     */
    public double getEstimate() {
        return x;
    }
    
    /**
     * Gets the current covariance.
     * 
     * @return the covariance
     */
    public double getCovariance() {
        return p;
    }
    
    /**
     * Resets the filter.
     * 
     * @param estimate the new initial estimate
     * @param covariance the new initial covariance
     */
    public void reset(double estimate, double covariance) {
        this.x = estimate;
        this.p = covariance;
    }
}
