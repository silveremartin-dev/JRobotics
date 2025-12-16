/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.wheeled;

import org.jrobotics.core.Capability;
import org.jrobotics.robot.AbstractRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a two-wheeled differential drive robot.
 * 
 * <p>Differential drive robots are steered by varying the relative speed
 * of the left and right wheels.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class DifferentialDriveRobot extends AbstractRobot {
    
    private static final Logger logger = LoggerFactory.getLogger(DifferentialDriveRobot.class);
    
    private final double wheelBase;      // Distance between wheels in meters
    private final double wheelRadius;    // Wheel radius in meters
    
    // Current state
    private volatile double x = 0;       // X position in meters
    private volatile double y = 0;       // Y position in meters
    private volatile double theta = 0;   // Heading in radians
    private volatile double linearVelocity = 0;  // m/s
    private volatile double angularVelocity = 0; // rad/s
    
    /**
     * Constructs a new differential drive robot.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public DifferentialDriveRobot(String id, String name) {
        this(id, name, 0.2, 0.033); // Default: 20cm wheelbase, 3.3cm wheel radius
    }
    
    /**
     * Constructs a new differential drive robot with custom dimensions.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param wheelBase the distance between wheels in meters
     * @param wheelRadius the wheel radius in meters
     */
    public DifferentialDriveRobot(String id, String name, double wheelBase, double wheelRadius) {
        super(id, name);
        this.wheelBase = wheelBase;
        this.wheelRadius = wheelRadius;
        
        // Register capabilities
        addCapability(Capability.WHEELED_LOCOMOTION);
    }
    
    /**
     * Sets the wheel speeds using differential drive kinematics.
     * 
     * @param leftSpeed left wheel speed (-1.0 to 1.0)
     * @param rightSpeed right wheel speed (-1.0 to 1.0)
     * @param maxSpeed maximum linear speed in m/s
     */
    public void setWheelSpeeds(double leftSpeed, double rightSpeed, double maxSpeed) {
        // Convert normalized speeds to actual wheel velocities
        double leftVel = leftSpeed * maxSpeed;
        double rightVel = rightSpeed * maxSpeed;
        
        // Calculate linear and angular velocity using differential drive kinematics
        // Reference: Siegwart & Nourbakhsh, "Introduction to Autonomous Mobile Robots"
        linearVelocity = (leftVel + rightVel) / 2.0;
        angularVelocity = (rightVel - leftVel) / wheelBase;
        
        logger.debug("[{}] Robot {} wheel speeds: L={}, R={}, v={}, ω={}",
                System.currentTimeMillis(), getName(), leftSpeed, rightSpeed,
                linearVelocity, angularVelocity);
    }
    
    /**
     * Sets the robot velocity directly.
     * 
     * @param linear linear velocity in m/s
     * @param angular angular velocity in rad/s
     */
    public void setVelocity(double linear, double angular) {
        this.linearVelocity = linear;
        this.angularVelocity = angular;
    }
    
    /**
     * Updates the robot's position based on velocity (dead reckoning).
     * 
     * <p>Uses Euler integration. For more accuracy, consider Runge-Kutta.</p>
     * 
     * @param dt time step in seconds
     */
    public void updateOdometry(double dt) {
        // Simple Euler integration
        x += linearVelocity * Math.cos(theta) * dt;
        y += linearVelocity * Math.sin(theta) * dt;
        theta += angularVelocity * dt;
        
        // Normalize theta to [-π, π]
        while (theta > Math.PI) theta -= 2 * Math.PI;
        while (theta < -Math.PI) theta += 2 * Math.PI;
    }
    
    /**
     * Stops the robot.
     */
    public void stop() {
        linearVelocity = 0;
        angularVelocity = 0;
    }
    
    /**
     * Resets the odometry to origin.
     */
    public void resetOdometry() {
        x = 0;
        y = 0;
        theta = 0;
    }
    
    /**
     * Sets the robot's pose directly.
     * 
     * @param x X position in meters
     * @param y Y position in meters
     * @param theta heading in radians
     */
    public void setPose(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getTheta() { return theta; }
    public double getLinearVelocity() { return linearVelocity; }
    public double getAngularVelocity() { return angularVelocity; }
    public double getWheelBase() { return wheelBase; }
    public double getWheelRadius() { return wheelRadius; }
    
    /**
     * Calculates the required wheel speeds for a given velocity command.
     * 
     * @param linear linear velocity in m/s
     * @param angular angular velocity in rad/s
     * @param maxWheelSpeed maximum wheel angular velocity in rad/s
     * @return array of [leftSpeed, rightSpeed] normalized to -1.0 to 1.0
     */
    public double[] calculateWheelSpeeds(double linear, double angular, double maxWheelSpeed) {
        // Inverse kinematics for differential drive
        double leftWheelVel = (linear - angular * wheelBase / 2.0) / wheelRadius;
        double rightWheelVel = (linear + angular * wheelBase / 2.0) / wheelRadius;
        
        // Normalize
        double left = leftWheelVel / maxWheelSpeed;
        double right = rightWheelVel / maxWheelSpeed;
        
        // Clamp to valid range
        left = Math.max(-1.0, Math.min(1.0, left));
        right = Math.max(-1.0, Math.min(1.0, right));
        
        return new double[]{left, right};
    }
}
