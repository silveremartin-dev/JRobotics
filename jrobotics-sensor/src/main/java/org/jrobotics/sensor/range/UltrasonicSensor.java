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

import org.jrobotics.sensor.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ultrasonic distance sensor implementation.
 * 
 * <p>Ultrasonic sensors measure distance by emitting sound waves and
 * measuring the time it takes for the echo to return.</p>
 * 
 * <p>Reference: HC-SR04 ultrasonic sensor datasheet.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class UltrasonicSensor extends AbstractSensor<Double> {
    
    private static final Logger logger = LoggerFactory.getLogger(UltrasonicSensor.class);
    
    // Speed of sound at 20°C in m/s
    private static final double SPEED_OF_SOUND = 343.0;
    
    // Default min/max values in meters (based on HC-SR04)
    private static final double DEFAULT_MIN_RANGE = 0.02;  // 2 cm
    private static final double DEFAULT_MAX_RANGE = 4.0;   // 4 m
    private static final double DEFAULT_RESOLUTION = 0.003; // 3 mm
    
    private volatile double simulatedDistance = 1.0; // For testing without hardware
    private final boolean simulationMode;
    
    /**
     * Constructs a new ultrasonic sensor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public UltrasonicSensor(String id, String name) {
        this(id, name, true); // Default to simulation mode
    }
    
    /**
     * Constructs a new ultrasonic sensor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, returns simulated data
     */
    public UltrasonicSensor(String id, String name, boolean simulationMode) {
        super(id, name, "m", DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE, DEFAULT_RESOLUTION);
        this.simulationMode = simulationMode;
        setSampleRate(40.0); // Typical ultrasonic sensor rate
    }
    
    @Override
    protected Double doRead(long timeoutMs) throws Exception {
        if (simulationMode) {
            // Add some noise to simulated readings
            double noise = (Math.random() - 0.5) * 0.01;
            double reading = Math.max(DEFAULT_MIN_RANGE, 
                    Math.min(DEFAULT_MAX_RANGE, simulatedDistance + noise));
            
            logger.trace("[{}] Ultrasonic {} simulated read: {} m", 
                    System.currentTimeMillis(), getId(), reading);
            return reading;
        }
        
        // Hardware mode: use GPIO for trigger/echo if HAL available
        // Currently falls back to simulated reading with warning
        logger.warn("[{}] Ultrasonic {} hardware mode - HAL not configured, using simulated value",
                System.currentTimeMillis(), getId());
        return simulatedDistance;
    }
    
    /**
     * Sets the simulated distance for testing.
     * 
     * <p>Only has effect in simulation mode.</p>
     * 
     * @param distance the distance in meters
     */
    public void setSimulatedDistance(double distance) {
        this.simulatedDistance = Math.max(DEFAULT_MIN_RANGE, 
                Math.min(DEFAULT_MAX_RANGE, distance));
    }
    
    /**
     * Checks if running in simulation mode.
     * 
     * @return true if simulating
     */
    public boolean isSimulationMode() {
        return simulationMode;
    }
    
    /**
     * Converts time-of-flight to distance.
     * 
     * @param timeOfFlightMicros the time in microseconds for echo to return
     * @return the distance in meters
     */
    public static double timeOfFlightToDistance(long timeOfFlightMicros) {
        // Distance = (time * speed) / 2 (divide by 2 for round trip)
        return (timeOfFlightMicros * SPEED_OF_SOUND / 1_000_000.0) / 2.0;
    }
}
