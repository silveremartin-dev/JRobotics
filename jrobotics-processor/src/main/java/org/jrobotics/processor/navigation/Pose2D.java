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
 * Represents a 2D pose (position and orientation).
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record Pose2D(
    /** X position in meters */
    double x,
    /** Y position in meters */
    double y,
    /** Heading/orientation in radians */
    double theta
) {
    /**
     * Creates a pose at the origin.
     * 
     * @return the origin pose
     */
    public static Pose2D origin() {
        return new Pose2D(0, 0, 0);
    }
    
    /**
     * Creates a pose at the given position with zero heading.
     * 
     * @param x X position
     * @param y Y position
     * @return the pose
     */
    public static Pose2D at(double x, double y) {
        return new Pose2D(x, y, 0);
    }
    
    /**
     * Calculates the Euclidean distance to another pose.
     * 
     * @param other the other pose
     * @return the distance in meters
     */
    public double distanceTo(Pose2D other) {
        double dx = other.x - x;
        double dy = other.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculates the angle to another pose.
     * 
     * @param other the other pose
     * @return the angle in radians
     */
    public double angleTo(Pose2D other) {
        return Math.atan2(other.y - y, other.x - x);
    }
    
    /**
     * Translates the pose by the given offset.
     * 
     * @param dx X offset
     * @param dy Y offset
     * @return the translated pose
     */
    public Pose2D translate(double dx, double dy) {
        return new Pose2D(x + dx, y + dy, theta);
    }
    
    /**
     * Rotates the pose by the given angle.
     * 
     * @param dtheta angle offset in radians
     * @return the rotated pose
     */
    public Pose2D rotate(double dtheta) {
        double newTheta = theta + dtheta;
        // Normalize to [-π, π]
        while (newTheta > Math.PI) newTheta -= 2 * Math.PI;
        while (newTheta < -Math.PI) newTheta += 2 * Math.PI;
        return new Pose2D(x, y, newTheta);
    }
}
