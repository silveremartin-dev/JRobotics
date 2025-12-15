/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.servo;

import org.jrobotics.actuator.AbstractActuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servo motor actuator implementation.
 * 
 * <p>Servos are position-controlled actuators that can move to specific
 * angles within their range (typically 0-180° or 0-270°).</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Servo extends AbstractActuator<ServoCommand> {
    
    private static final Logger logger = LoggerFactory.getLogger(Servo.class);
    
    private final boolean simulationMode;
    private volatile double currentAngle = 90.0;
    private volatile double targetAngle = 90.0;
    private volatile boolean holding = true;
    
    // Servo configuration
    private final double minPulseMs;  // Minimum pulse width (0°)
    private final double maxPulseMs;  // Maximum pulse width (max angle)
    private final double maxAngle;    // Maximum servo angle
    
    /**
     * Constructs a new servo with standard 0-180° range.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public Servo(String id, String name) {
        this(id, name, true);
    }
    
    /**
     * Constructs a new servo with standard 0-180° range.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, simulates servo behavior
     */
    public Servo(String id, String name, boolean simulationMode) {
        this(id, name, simulationMode, 0.5, 2.5, 180.0);
    }
    
    /**
     * Constructs a new servo with custom configuration.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, simulates servo behavior
     * @param minPulseMs minimum pulse width in milliseconds
     * @param maxPulseMs maximum pulse width in milliseconds
     * @param maxAngle maximum servo angle in degrees
     */
    public Servo(String id, String name, boolean simulationMode,
                 double minPulseMs, double maxPulseMs, double maxAngle) {
        super(id, name, 0, maxAngle);
        this.simulationMode = simulationMode;
        this.minPulseMs = minPulseMs;
        this.maxPulseMs = maxPulseMs;
        this.maxAngle = maxAngle;
    }
    
    @Override
    protected void doExecute(ServoCommand command) throws Exception {
        double clampedAngle = Math.max(0, Math.min(maxAngle, command.angle()));
        this.targetAngle = clampedAngle;
        this.holding = command.hold();
        
        if (simulationMode) {
            // Simulate servo movement
            simulateMovement(clampedAngle, command.speed());
            logger.debug("[{}] Servo {} moved to angle {} degrees (simulated)",
                    System.currentTimeMillis(), getId(), clampedAngle);
        } else {
            // TODO: Implement actual PWM control
            throw new UnsupportedOperationException("Hardware mode not yet implemented");
        }
    }
    
    /**
     * Simulates servo movement to target angle.
     * 
     * @param targetAngle the target angle
     * @param speed the movement speed (0.0-1.0)
     */
    private void simulateMovement(double targetAngle, double speed) {
        // Simulate instant movement (in real implementation, would be gradual)
        this.currentAngle = targetAngle;
        setCurrentValue(targetAngle);
    }
    
    @Override
    protected void doEmergencyStop() {
        // Servos don't have a traditional "stop" - they hold position
        holding = false;
        logger.warn("[{}] Servo {} released (emergency)", System.currentTimeMillis(), getId());
    }
    
    @Override
    public boolean hasFeedback() {
        return true;
    }
    
    @Override
    public Object getFeedback() {
        return new ServoFeedback(currentAngle, targetAngle, holding, angleToPulse(currentAngle));
    }
    
    /**
     * Gets the current angle.
     * 
     * @return the current angle in degrees
     */
    public double getCurrentAngle() {
        return currentAngle;
    }
    
    /**
     * Gets the target angle.
     * 
     * @return the target angle in degrees
     */
    public double getTargetAngle() {
        return targetAngle;
    }
    
    /**
     * Checks if the servo is holding position.
     * 
     * @return true if holding
     */
    public boolean isHolding() {
        return holding;
    }
    
    /**
     * Converts angle to pulse width.
     * 
     * @param angle the angle in degrees
     * @return the pulse width in milliseconds
     */
    public double angleToPulse(double angle) {
        double ratio = angle / maxAngle;
        return minPulseMs + ratio * (maxPulseMs - minPulseMs);
    }
    
    /**
     * Converts pulse width to angle.
     * 
     * @param pulseMs the pulse width in milliseconds
     * @return the angle in degrees
     */
    public double pulseToAngle(double pulseMs) {
        double ratio = (pulseMs - minPulseMs) / (maxPulseMs - minPulseMs);
        return ratio * maxAngle;
    }
    
    /**
     * Gets the maximum angle for this servo.
     * 
     * @return the maximum angle in degrees
     */
    public double getMaxAngle() {
        return maxAngle;
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
     * Servo feedback data.
     */
    public record ServoFeedback(
        double currentAngle,
        double targetAngle,
        boolean holding,
        double currentPulseMs
    ) {}
}
