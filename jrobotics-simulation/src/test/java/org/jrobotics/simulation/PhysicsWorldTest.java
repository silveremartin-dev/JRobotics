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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PhysicsWorld}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class PhysicsWorldTest {

    private PhysicsWorld world;

    @BeforeEach
    void setUp() {
        world = new PhysicsWorld();
        // Disable gravity for simpler testing
        world.setGravity(Vector3.ZERO);
        world.setFriction(0);
    }

    @Test
    void testAddAndGetBody() {
        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, Vector3.ZERO, 0.5);
        world.addBody(body);

        assertEquals(body, world.getBody("robot1"));
        assertEquals(1, world.getBodies().size());
    }

    @Test
    void testRemoveBody() {
        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, Vector3.ZERO, 0.5);
        world.addBody(body);
        world.removeBody("robot1");

        assertNull(world.getBody("robot1"));
        assertEquals(0, world.getBodies().size());
    }

    @Test
    void testVelocityIntegration() {
        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, Vector3.ZERO, 0.5);
        world.addBody(body);

        // Set velocity to 1 m/s in X direction
        world.setVelocity("robot1", new Vector3(1, 0, 0));

        // Step 1 second
        world.step(1.0);

        PhysicsBody updated = world.getBody("robot1");
        assertEquals(1.0, updated.position().x(), 0.01);
    }

    @Test
    void testGravity() {
        world.setGravity(new Vector3(0, 0, -10));

        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, new Vector3(0, 0, 10), 0.5);
        world.addBody(body);

        // Step 1 second
        world.step(1.0);

        PhysicsBody updated = world.getBody("robot1");
        // Velocity should be -10 m/s, position should be ~5 meters (started at 10)
        assertTrue(updated.velocity().z() < 0);
        assertTrue(updated.position().z() < 10);
    }

    @Test
    void testStaticBodyDoesNotMove() {
        PhysicsBody staticBody = PhysicsBody.staticBody("obstacle", new Vector3(5, 5, 0), 1.0);
        world.addBody(staticBody);

        world.step(1.0);

        PhysicsBody updated = world.getBody("obstacle");
        assertEquals(5, updated.position().x(), 1e-10);
        assertEquals(5, updated.position().y(), 1e-10);
    }

    @Test
    void testCollisionDetection() {
        AtomicInteger collisionCount = new AtomicInteger(0);

        world.addCollisionListener((a, b) -> collisionCount.incrementAndGet());

        // Two overlapping bodies
        PhysicsBody body1 = PhysicsBody.dynamicBody("robot1", 10.0, new Vector3(0, 0, 0), 1.0);
        PhysicsBody body2 = PhysicsBody.dynamicBody("robot2", 10.0, new Vector3(1, 0, 0), 1.0);

        world.addBody(body1);
        world.addBody(body2);

        world.step(0.01);

        assertEquals(1, collisionCount.get());
    }

    @Test
    void testNoCollisionWhenFarApart() {
        AtomicInteger collisionCount = new AtomicInteger(0);

        world.addCollisionListener((a, b) -> collisionCount.incrementAndGet());

        PhysicsBody body1 = PhysicsBody.dynamicBody("robot1", 10.0, new Vector3(0, 0, 0), 0.5);
        PhysicsBody body2 = PhysicsBody.dynamicBody("robot2", 10.0, new Vector3(10, 0, 0), 0.5);

        world.addBody(body1);
        world.addBody(body2);

        world.step(0.01);

        assertEquals(0, collisionCount.get());
    }

    @Test
    void testApplyForce() {
        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, Vector3.ZERO, 0.5);
        world.addBody(body);

        // F = 10N, m = 10kg, so a = 1 m/s²
        world.applyForce("robot1", new Vector3(10, 0, 0));

        PhysicsBody updated = world.getBody("robot1");
        assertEquals(1.0, updated.velocity().x(), 0.01);
    }

    @Test
    void testReset() {
        PhysicsBody body = PhysicsBody.dynamicBody("robot1", 10.0, Vector3.ZERO, 0.5);
        world.addBody(body);
        world.step(1.0);

        world.reset();

        assertEquals(0, world.getBodies().size());
        assertEquals(0, world.getStepCount());
    }

    @Test
    void testStepCount() {
        world.step(0.01);
        world.step(0.01);
        world.step(0.01);

        assertEquals(3, world.getStepCount());
    }
}
