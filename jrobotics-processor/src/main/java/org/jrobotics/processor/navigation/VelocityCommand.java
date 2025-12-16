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

/**
 * Represents a velocity command for robot motion.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record VelocityCommand(
    /** Linear velocity in m/s */
    double linear,
    /** Angular velocity in rad/s */
    double angular
) {
    /**
     * Creates a stop command.
     * 
     * @return the stop command
     */
    public static VelocityCommand stop() {
        return new VelocityCommand(0, 0);
    }
    
    /**
     * Creates a forward motion command.
     * 
     * @param speed the forward speed in m/s
     * @return the velocity command
     */
    public static VelocityCommand forward(double speed) {
        return new VelocityCommand(speed, 0);
    }
    
    /**
     * Creates a rotation command.
     * 
     * @param angularSpeed the angular speed in rad/s
     * @return the velocity command
     */
    public static VelocityCommand rotate(double angularSpeed) {
        return new VelocityCommand(0, angularSpeed);
    }
    
    /**
     * Checks if this is a stop command.
     * 
     * @return true if stopped
     */
    public boolean isStopped() {
        return Math.abs(linear) < 0.001 && Math.abs(angular) < 0.001;
    }
}
