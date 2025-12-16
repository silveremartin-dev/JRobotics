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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple 2D/3D physics world for robot simulation.
 * 
 * <p>Uses Euler integration for physics stepping. Suitable for
 * educational and prototyping purposes.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class PhysicsWorld {
    
    private static final Logger logger = LoggerFactory.getLogger(PhysicsWorld.class);
    
    private final Map<String, PhysicsBody> bodies = new ConcurrentHashMap<>();
    private final List<CollisionListener> collisionListeners = new ArrayList<>();
    private Vector3 gravity = new Vector3(0, 0, -9.81);
    private double friction = 0.1;
    private double timeScale = 1.0;
    private long stepCount = 0;
    
    /**
     * Adds a body to the world.
     */
    public void addBody(PhysicsBody body) {
        bodies.put(body.id(), body);
        logger.debug("[{}] Added body: {}", System.currentTimeMillis(), body.id());
    }
    
    /**
     * Removes a body from the world.
     */
    public void removeBody(String bodyId) {
        bodies.remove(bodyId);
    }
    
    /**
     * Gets a body by ID.
     */
    public PhysicsBody getBody(String bodyId) {
        return bodies.get(bodyId);
    }
    
    /**
     * Gets all bodies.
     */
    public Collection<PhysicsBody> getBodies() {
        return Collections.unmodifiableCollection(bodies.values());
    }
    
    /**
     * Sets gravity vector.
     */
    public void setGravity(Vector3 gravity) {
        this.gravity = gravity;
    }
    
    /**
     * Sets friction coefficient.
     */
    public void setFriction(double friction) {
        this.friction = Math.max(0, Math.min(1, friction));
    }
    
    /**
     * Sets time scale (1.0 = real-time).
     */
    public void setTimeScale(double timeScale) {
        this.timeScale = Math.max(0.01, Math.min(10, timeScale));
    }
    
    /**
     * Adds a collision listener.
     */
    public void addCollisionListener(CollisionListener listener) {
        collisionListeners.add(listener);
    }
    
    /**
     * Steps the physics simulation.
     * 
     * @param dt time step in seconds
     */
    public void step(double dt) {
        double scaledDt = dt * timeScale;
        stepCount++;
        
        // Update positions for dynamic bodies
        List<PhysicsBody> updatedBodies = new ArrayList<>();
        
        for (PhysicsBody body : bodies.values()) {
            if (body.isStatic()) {
                updatedBodies.add(body);
                continue;
            }
            
            // Apply gravity
            Vector3 acceleration = gravity;
            
            // Update velocity: v = v + a*dt
            Vector3 newVelocity = body.velocity().add(acceleration.multiply(scaledDt));
            
            // Apply friction
            newVelocity = newVelocity.multiply(1.0 - friction * scaledDt);
            
            // Update position: p = p + v*dt
            Vector3 newPosition = body.position().add(newVelocity.multiply(scaledDt));
            
            // Update orientation
            Vector3 newOrientation = body.orientation().add(body.angularVelocity().multiply(scaledDt));
            
            updatedBodies.add(new PhysicsBody(
                body.id(), body.mass(), newPosition, newVelocity,
                newOrientation, body.angularVelocity(), body.boundingRadius(), false
            ));
        }
        
        // Update bodies map
        for (PhysicsBody body : updatedBodies) {
            bodies.put(body.id(), body);
        }
        
        // Check collisions
        detectCollisions();
    }
    
    /**
     * Detects collisions between bodies.
     */
    private void detectCollisions() {
        List<PhysicsBody> bodyList = new ArrayList<>(bodies.values());
        
        for (int i = 0; i < bodyList.size(); i++) {
            for (int j = i + 1; j < bodyList.size(); j++) {
                PhysicsBody a = bodyList.get(i);
                PhysicsBody b = bodyList.get(j);
                
                if (a.collidesWith(b)) {
                    // Notify listeners
                    for (CollisionListener listener : collisionListeners) {
                        listener.onCollision(a, b);
                    }
                }
            }
        }
    }
    
    /**
     * Applies a force to a body.
     * 
     * @param bodyId the body ID
     * @param force the force vector (Newtons)
     */
    public void applyForce(String bodyId, Vector3 force) {
        PhysicsBody body = bodies.get(bodyId);
        if (body == null || body.isStatic()) return;
        
        // F = ma, so a = F/m
        Vector3 acceleration = force.multiply(1.0 / body.mass());
        Vector3 newVelocity = body.velocity().add(acceleration);
        bodies.put(bodyId, body.withVelocity(newVelocity));
    }
    
    /**
     * Sets velocity directly for a body.
     */
    public void setVelocity(String bodyId, Vector3 velocity) {
        PhysicsBody body = bodies.get(bodyId);
        if (body == null || body.isStatic()) return;
        bodies.put(bodyId, body.withVelocity(velocity));
    }
    
    /**
     * Gets the step count.
     */
    public long getStepCount() {
        return stepCount;
    }
    
    /**
     * Resets the world.
     */
    public void reset() {
        bodies.clear();
        stepCount = 0;
    }
    
    /**
     * Collision listener interface.
     */
    @FunctionalInterface
    public interface CollisionListener {
        void onCollision(PhysicsBody a, PhysicsBody b);
    }
}
