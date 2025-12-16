/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.navigation;

import org.jrobotics.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple "go-to-goal" navigation processor.
 * 
 * <p>Implements a proportional controller to navigate the robot to a target pose.
 * This is a basic navigation algorithm suitable for open environments without obstacles.</p>
 * 
 * <p>Reference: Siegwart & Nourbakhsh, "Introduction to Autonomous Mobile Robots", Chapter 6</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class GoToGoalProcessor extends AbstractProcessor<NavigationInput, VelocityCommand> {
    
    private static final Logger logger = LoggerFactory.getLogger(GoToGoalProcessor.class);
    
    // Default control gains
    private static final double DEFAULT_LINEAR_GAIN = 0.5;
    private static final double DEFAULT_ANGULAR_GAIN = 2.0;
    private static final double DEFAULT_GOAL_TOLERANCE = 0.1; // meters
    private static final double DEFAULT_MAX_LINEAR = 0.5;    // m/s
    private static final double DEFAULT_MAX_ANGULAR = 1.0;   // rad/s
    
    /**
     * Constructs a new go-to-goal processor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public GoToGoalProcessor(String id, String name) {
        super(id, name);
        // Set default configuration
        setConfiguration("linear_gain", DEFAULT_LINEAR_GAIN);
        setConfiguration("angular_gain", DEFAULT_ANGULAR_GAIN);
        setConfiguration("goal_tolerance", DEFAULT_GOAL_TOLERANCE);
        setConfiguration("max_linear", DEFAULT_MAX_LINEAR);
        setConfiguration("max_angular", DEFAULT_MAX_ANGULAR);
    }
    
    @Override
    protected VelocityCommand doProcess(NavigationInput input) {
        Pose2D current = input.currentPose();
        Pose2D goal = input.goalPose();
        
        // Calculate distance and angle to goal
        double distance = current.distanceTo(goal);
        double angleToGoal = current.angleTo(goal);
        double headingError = normalizeAngle(angleToGoal - current.theta());
        
        // Check if we've reached the goal
        double goalTolerance = getConfig("goal_tolerance", DEFAULT_GOAL_TOLERANCE);
        if (distance < goalTolerance) {
            logger.debug("[{}] Goal reached at ({}, {})", 
                    System.currentTimeMillis(), goal.x(), goal.y());
            setConfidence(1.0);
            return VelocityCommand.stop();
        }
        
        // Get control gains
        double linearGain = getConfig("linear_gain", DEFAULT_LINEAR_GAIN);
        double angularGain = getConfig("angular_gain", DEFAULT_ANGULAR_GAIN);
        double maxLinear = getConfig("max_linear", DEFAULT_MAX_LINEAR);
        double maxAngular = getConfig("max_angular", DEFAULT_MAX_ANGULAR);
        
        // Calculate control outputs
        double linearVel = linearGain * distance;
        double angularVel = angularGain * headingError;
        
        // Reduce linear velocity when turning
        linearVel *= Math.cos(headingError);
        
        // Clamp to maximum values
        linearVel = clamp(linearVel, -maxLinear, maxLinear);
        angularVel = clamp(angularVel, -maxAngular, maxAngular);
        
        // Set confidence based on heading error
        setConfidence(1.0 - Math.abs(headingError) / Math.PI);
        
        logger.trace("[{}] GoToGoal: dist={}, heading_err={}, vel=({}, {})",
                System.currentTimeMillis(), distance, headingError, linearVel, angularVel);
        
        return new VelocityCommand(linearVel, angularVel);
    }
    
    /**
     * Normalizes an angle to [-π, π].
     * 
     * @param angle the angle in radians
     * @return the normalized angle
     */
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    
    /**
     * Clamps a value to a range.
     * 
     * @param value the value
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
