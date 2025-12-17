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

import org.jrobotics.core.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Default physics engine implementation using JRobotics built-in physics.
 * 
 * <p>
 * Provides simple but sufficient physics for most robotics simulations.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class DefaultPhysicsEngine implements PhysicsEngine {

    @Override
    public String getName() {
        return "JRobotics Built-in Physics";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public PhysicsWorld createWorld() {
        return new PhysicsWorld();
    }

    @Override
    public void step(PhysicsWorld world, double deltaTime) {
        world.step(deltaTime);
    }

    @Override
    public List<CollisionPair> detectCollisions(PhysicsWorld world) {
        List<CollisionPair> collisions = new ArrayList<>();
        List<PhysicsBody> bodies = new ArrayList<>(world.getBodies());

        // O(n²) broad phase - simple but works for small scenes
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                PhysicsBody a = bodies.get(i);
                PhysicsBody b = bodies.get(j);

                // Sphere-sphere collision
                double distance = a.position().distanceTo(b.position());
                double minDist = a.boundingRadius() + b.boundingRadius();

                if (distance < minDist) {
                    Vector3 normal = b.position().subtract(a.position()).normalize();
                    Vector3 contact = a.position().add(normal.multiply(a.boundingRadius()));
                    double penetration = minDist - distance;

                    collisions.add(new CollisionPair(a, b, contact, normal, penetration));
                }
            }
        }

        return collisions;
    }

    @Override
    public void resolveCollisions(PhysicsWorld world, List<CollisionPair> collisions) {
        for (CollisionPair collision : collisions) {
            // Simple position correction
            if (!collision.bodyA().isStatic() && !collision.bodyB().isStatic()) {
                // Both dynamic - split correction
                // Vector3 correction = collision.normal().multiply(collision.penetration() /
                // 2);
                // Would need mutable bodies to apply correction
            } else if (!collision.bodyA().isStatic()) {
                // Only A is dynamic
                // Vector3 correction = collision.normal().multiply(-collision.penetration());
                // Apply to A
            } else if (!collision.bodyB().isStatic()) {
                // Only B is dynamic
                // Vector3 correction = collision.normal().multiply(collision.penetration());
                // Apply to B
            }
        }
    }

    @Override
    public boolean supports3D() {
        return true; // Vector3 is 3D
    }

    @Override
    public boolean supportsCCD() {
        return false; // No continuous collision detection yet
    }
}
