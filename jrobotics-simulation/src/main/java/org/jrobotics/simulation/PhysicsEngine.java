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
 * Interface for pluggable physics engine implementations.
 * 
 * <p>Allows integration with external physics libraries like dyn4j, JBullet, etc.</p>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Use default implementation
 * PhysicsEngine engine = new DefaultPhysicsEngine();
 * 
 * // Or use external library
 * PhysicsEngine engine = new Dyn4jPhysicsEngine();
 * }</pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface PhysicsEngine {
    
    /**
     * Gets the engine name.
     * 
     * @return the name (e.g., "JRobotics", "dyn4j", "JBullet")
     */
    String getName();
    
    /**
     * Gets the engine version.
     * 
     * @return the version string
     */
    String getVersion();
    
    /**
     * Creates a new physics world.
     * 
     * @return a new world instance
     */
    PhysicsWorld createWorld();
    
    /**
     * Steps the simulation forward.
     * 
     * @param world the world to step
     * @param deltaTime the time step in seconds
     */
    void step(PhysicsWorld world, double deltaTime);
    
    /**
     * Performs collision detection.
     * 
     * @param world the world
     * @return list of collision pairs
     */
    java.util.List<CollisionPair> detectCollisions(PhysicsWorld world);
    
    /**
     * Resolves collisions with impulse response.
     * 
     * @param world the world
     * @param collisions the collisions to resolve
     */
    void resolveCollisions(PhysicsWorld world, java.util.List<CollisionPair> collisions);
    
    /**
     * Checks if the engine supports 3D physics.
     * 
     * @return true if 3D is supported
     */
    boolean supports3D();
    
    /**
     * Checks if the engine supports continuous collision detection.
     * 
     * @return true if CCD is supported
     */
    boolean supportsCCD();
    
    /**
     * Collision pair result.
     */
    record CollisionPair(PhysicsBody bodyA, PhysicsBody bodyB, Vector3 contactPoint, Vector3 normal, double penetration) {}
}
