/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.range;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UltrasonicSensor}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class UltrasonicSensorTest {
    
    private UltrasonicSensor sensor;
    
    @BeforeEach
    void setUp() {
        sensor = new UltrasonicSensor("sonar-test", "Test Sonar");
    }
    
    @Test
    void testSensorCreation() {
        assertEquals("sonar-test", sensor.getId());
        assertEquals("Test Sonar", sensor.getName());
        assertEquals("m", sensor.getUnit());
    }
    
    @Test
    void testLifecycle() throws Exception {
        assertFalse(sensor.isRunning());
        
        sensor.initialize();
        sensor.start();
        
        assertTrue(sensor.isRunning());
        
        sensor.stop();
        sensor.shutdown();
        
        assertFalse(sensor.isRunning());
    }
    
    @Test
    void testReadWhileRunning() throws Exception {
        sensor.initialize();
        sensor.start();
        
        Optional<Double> reading = sensor.read();
        
        assertTrue(reading.isPresent());
        assertTrue(reading.get() >= sensor.getMinValue());
        assertTrue(reading.get() <= sensor.getMaxValue());
        
        sensor.stop();
        sensor.shutdown();
    }
    
    @Test
    void testSimulatedDistance() {
        sensor.setSimulatedDistance(2.5);
        
        sensor.initialize();
        sensor.start();
        
        Optional<Double> reading = sensor.read();
        assertTrue(reading.isPresent());
        // Should be close to 2.5m with some noise
        assertTrue(Math.abs(reading.get() - 2.5) < 0.5);
        
        sensor.stop();
        sensor.shutdown();
    }
    
    @Test
    void testTimeOfFlightConversion() {
        // Sound travels ~343 m/s at room temperature
        // Distance = (time * 343 / 1e6) / 2  (round trip)
        // For 10000 microseconds: d = (10000 * 343 / 1e6) / 2 = 1.715m
        double distance = UltrasonicSensor.timeOfFlightToDistance(10000);
        assertEquals(1.715, distance, 0.01);
    }
    
    @Test
    void testMinMaxValues() {
        assertTrue(sensor.getMinValue() > 0);
        assertTrue(sensor.getMaxValue() > sensor.getMinValue());
    }
    
    @Test
    void testResolution() {
        assertTrue(sensor.getResolution() > 0);
        assertTrue(sensor.getResolution() < 0.1); // Should be reasonably precise
    }
}
