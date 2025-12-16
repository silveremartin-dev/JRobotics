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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Extended Kalman Filter.
 */
class ExtendedKalmanFilterTest {
    
    private ExtendedKalmanFilter ekf;
    
    @BeforeEach
    void setUp() {
        ekf = ExtendedKalmanFilter.createRobotPoseEKF();
    }
    
    @Test
    void testInitialState() {
        double[] state = ekf.getState();
        
        assertEquals(3, state.length, "State should be 3D (x, y, theta)");
        assertEquals(0.0, state[0], 0.001);
        assertEquals(0.0, state[1], 0.001);
        assertEquals(0.0, state[2], 0.001);
    }
    
    @Test
    void testPredictionWithVelocity() {
        ekf.setState(0, 0, 0);
        
        // Move forward at 1 m/s for 1 second
        ekf.predict(new double[]{1.0, 0.0}, 1.0);
        
        double[] state = ekf.getState();
        assertEquals(1.0, state[0], 0.1, "X should be ~1.0");
        assertEquals(0.0, state[1], 0.1, "Y should be ~0.0");
    }
    
    @Test
    void testPredictionWithTurn() {
        ekf.setState(0, 0, 0);
        
        // Turn at 0.5 rad/s for 1 second
        ekf.predict(new double[]{0.0, 0.5}, 1.0);
        
        double[] state = ekf.getState();
        assertEquals(0.5, state[2], 0.1, "Theta should be ~0.5 rad");
    }
    
    @Test
    void testMeasurementUpdate() {
        ekf.setState(0, 0, 0);
        
        // Provide GPS measurement
        ekf.update(new double[]{1.0, 0.5});
        
        double[] state = ekf.getState();
        // State should move towards measurement
        assertTrue(state[0] > 0, "X should increase towards measurement");
        assertTrue(state[1] > 0, "Y should increase towards measurement");
    }
    
    @Test
    void testCovarianceReducesWithMeasurement() {
        ekf.setState(0, 0, 0);
        
        double[] covBefore = ekf.getCovariance();
        double traceBefore = covBefore[0] + covBefore[4]; // P[0,0] + P[1,1]
        
        // Multiple measurements should reduce uncertainty
        for (int i = 0; i < 10; i++) {
            ekf.update(new double[]{1.0, 0.5});
        }
        
        double[] covAfter = ekf.getCovariance();
        double traceAfter = covAfter[0] + covAfter[4];
        
        assertTrue(traceAfter < traceBefore, 
            "Covariance should decrease with measurements");
    }
    
    @Test
    void testCircularMotion() {
        ekf.setState(0, 0, 0);
        
        // Circular motion: v=1, omega=0.5
        for (int i = 0; i < 100; i++) {
            ekf.predict(new double[]{1.0, 0.5}, 0.1);
        }
        
        double[] state = ekf.getState();
        // After ~10 seconds with v=1, omega=0.5, robot has made partial turns
        // Just verify it's moved significantly
        double dist = Math.sqrt(state[0] * state[0] + state[1] * state[1]);
        assertTrue(dist > 0.5, "Robot should have moved");
    }
}
