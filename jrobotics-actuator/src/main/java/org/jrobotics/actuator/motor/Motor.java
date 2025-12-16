/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.motor;

import org.jrobotics.actuator.AbstractActuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DC motor actuator implementation.
 * 
 * <p>Supports speed control from -1.0 (full reverse) to 1.0 (full forward),
 * with optional encoder feedback for closed-loop control.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Motor extends AbstractActuator<MotorCommand> {
    
    private static final Logger logger = LoggerFactory.getLogger(Motor.class);
    
    private final boolean simulationMode;
    private volatile double targetSpeed = 0;
    private volatile long encoderCount = 0;
    
    // Encoder configuration
    private final int encoderTicksPerRevolution;
    private final double wheelDiameter; // in meters
    
    /**
     * Constructs a new motor actuator.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public Motor(String id, String name) {
        this(id, name, true);
    }
    
    /**
     * Constructs a new motor actuator.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, simulates motor behavior
     */
    public Motor(String id, String name, boolean simulationMode) {
        this(id, name, simulationMode, 360, 0.065);
    }
    
    /**
     * Constructs a new motor actuator with encoder settings.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, simulates motor behavior
     * @param encoderTicksPerRevolution encoder resolution
     * @param wheelDiameter wheel diameter in meters
     */
    public Motor(String id, String name, boolean simulationMode,
                 int encoderTicksPerRevolution, double wheelDiameter) {
        super(id, name, -1.0, 1.0);
        this.simulationMode = simulationMode;
        this.encoderTicksPerRevolution = encoderTicksPerRevolution;
        this.wheelDiameter = wheelDiameter;
    }
    
    @Override
    protected void doExecute(MotorCommand command) throws Exception {
        this.targetSpeed = command.speed();
        setCurrentValue(command.speed());
        
        if (simulationMode) {
            logger.debug("[{}] Motor {} set to speed {} (simulated)",
                    System.currentTimeMillis(), getId(), command.speed());
            
            // Simulate encoder ticks based on speed
            if (!command.isStop() && command.durationMs() > 0) {
                simulateMovement(command.speed(), command.durationMs());
            }
        } else {
            // Hardware mode: use GPIO/PWM for motor control if available
            // Currently falls back to simulated behavior with warning
            logger.warn("[{}] Motor {} hardware mode - HAL not configured, simulating",
                    System.currentTimeMillis(), getId());
            if (!command.isStop() && command.durationMs() > 0) {
                simulateMovement(command.speed(), command.durationMs());
            }
        }
    }
    
    /**
     * Simulates motor movement and encoder updates.
     * 
     * @param speed the speed (-1.0 to 1.0)
     * @param durationMs the duration in milliseconds
     */
    private void simulateMovement(double speed, long durationMs) {
        // Simulate at a typical motor RPM
        double maxRpm = 100.0;
        double rpm = speed * maxRpm;
        double revolutions = rpm * (durationMs / 60000.0);
        long ticks = (long) (revolutions * encoderTicksPerRevolution);
        encoderCount += ticks;
    }
    
    @Override
    protected void doEmergencyStop() {
        targetSpeed = 0;
        setCurrentValue(0);
        logger.warn("[{}] Motor {} emergency stopped", System.currentTimeMillis(), getId());
    }
    
    @Override
    public boolean hasFeedback() {
        return true;
    }
    
    @Override
    public Object getFeedback() {
        return new MotorFeedback(targetSpeed, getCurrentValue(), encoderCount, getDistanceTraveled());
    }
    
    /**
     * Gets the current target speed.
     * 
     * @return the target speed
     */
    public double getTargetSpeed() {
        return targetSpeed;
    }
    
    /**
     * Gets the encoder count.
     * 
     * @return the encoder count
     */
    public long getEncoderCount() {
        return encoderCount;
    }
    
    /**
     * Resets the encoder count to zero.
     */
    public void resetEncoder() {
        encoderCount = 0;
        logger.debug("[{}] Motor {} encoder reset", System.currentTimeMillis(), getId());
    }
    
    /**
     * Gets the distance traveled based on encoder count.
     * 
     * @return the distance in meters
     */
    public double getDistanceTraveled() {
        double circumference = Math.PI * wheelDiameter;
        double revolutions = (double) encoderCount / encoderTicksPerRevolution;
        return revolutions * circumference;
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
     * Motor feedback data.
     */
    public record MotorFeedback(
        double targetSpeed,
        double actualSpeed,
        long encoderCount,
        double distanceTraveled
    ) {}
}
