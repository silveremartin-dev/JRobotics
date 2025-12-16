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
 * Fused pose estimate from sensor fusion.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record FusedPose(
    /** X position in meters */
    double x,
    /** Y position in meters */
    double y,
    /** Z position in meters */
    double z,
    /** Roll angle in radians */
    double roll,
    /** Pitch angle in radians */
    double pitch,
    /** Yaw angle in radians */
    double yaw,
    /** X velocity in m/s */
    double vx,
    /** Y velocity in m/s */
    double vy,
    /** Z velocity in m/s */
    double vz,
    /** Timestamp in milliseconds */
    long timestamp
) {
    /**
     * Creates a 2D pose (x, y, yaw).
     */
    public static FusedPose pose2D(double x, double y, double yaw) {
        return new FusedPose(x, y, 0, 0, 0, yaw, 0, 0, 0, System.currentTimeMillis());
    }
    
    /**
     * Gets position as array [x, y, z].
     */
    public double[] getPosition() {
        return new double[] { x, y, z };
    }
    
    /**
     * Gets orientation as array [roll, pitch, yaw].
     */
    public double[] getOrientation() {
        return new double[] { roll, pitch, yaw };
    }
    
    /**
     * Gets velocity as array [vx, vy, vz].
     */
    public double[] getVelocity() {
        return new double[] { vx, vy, vz };
    }
}
