/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.simulation;

/**
 * Represents the physical properties of a simulated body.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record PhysicsBody(
    /** Unique identifier */
    String id,
    /** Mass in kg */
    double mass,
    /** Position in world coordinates */
    Vector3 position,
    /** Linear velocity in m/s */
    Vector3 velocity,
    /** Orientation in radians (yaw, pitch, roll) */
    Vector3 orientation,
    /** Angular velocity in rad/s */
    Vector3 angularVelocity,
    /** Bounding radius for collision detection */
    double boundingRadius,
    /** If true, body is static (doesn't move) */
    boolean isStatic
) {
    /**
     * Creates a static body (e.g., obstacle).
     */
    public static PhysicsBody staticBody(String id, Vector3 position, double radius) {
        return new PhysicsBody(id, 0, position, Vector3.ZERO, Vector3.ZERO, Vector3.ZERO, radius, true);
    }
    
    /**
     * Creates a dynamic body (e.g., robot).
     */
    public static PhysicsBody dynamicBody(String id, double mass, Vector3 position, double radius) {
        return new PhysicsBody(id, mass, position, Vector3.ZERO, Vector3.ZERO, Vector3.ZERO, radius, false);
    }
    
    /**
     * Returns a copy with updated position.
     */
    public PhysicsBody withPosition(Vector3 newPosition) {
        return new PhysicsBody(id, mass, newPosition, velocity, orientation, angularVelocity, boundingRadius, isStatic);
    }
    
    /**
     * Returns a copy with updated velocity.
     */
    public PhysicsBody withVelocity(Vector3 newVelocity) {
        return new PhysicsBody(id, mass, position, newVelocity, orientation, angularVelocity, boundingRadius, isStatic);
    }
    
    /**
     * Returns a copy with updated orientation.
     */
    public PhysicsBody withOrientation(Vector3 newOrientation) {
        return new PhysicsBody(id, mass, position, velocity, newOrientation, angularVelocity, boundingRadius, isStatic);
    }
    
    /**
     * Checks for collision with another body using sphere-sphere intersection.
     */
    public boolean collidesWith(PhysicsBody other) {
        double distance = position.distanceTo(other.position);
        return distance < (boundingRadius + other.boundingRadius);
    }
}
