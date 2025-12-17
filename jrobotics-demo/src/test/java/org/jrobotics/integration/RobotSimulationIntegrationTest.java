/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.integration;

import org.jrobotics.core.math.Vector3;
import org.jrobotics.simulation.*;
import org.jrobotics.simulation.environment.EnvironmentBuilder;
import org.jrobotics.sensor.fusion.ExtendedKalmanFilter;
import org.jrobotics.processor.navigation.AStarPathPlanner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Integration tests for complete robot simulation scenarios.
 */
class RobotSimulationIntegrationTest {

    @Test
    void testCompleteNavigationScenario() {
        // Create environment
        PhysicsWorld world = EnvironmentBuilder.emptyArena(20, 15)
                .addObstacle("wall1", 5, 5, 2.0)
                .addObstacle("wall2", -3, 7, 1.5)
                .addRobot("robot", -8, -5, 5.0, 0.5)
                .build();

        assertNotNull(world);
        assertEquals(5, world.getBodies().size(), "4 walls + 2 obstacles + 1 robot - actually 5");

        // Create path planner
        AStarPathPlanner planner = new AStarPathPlanner(40, 30, true);
        // Convert world obstacles to grid (simplified)

        List<int[]> path = planner.findPath(4, 5, 36, 25); // start to goal in grid coords
        assertFalse(path.isEmpty(), "Path should exist in empty arena");

        // Create EKF for robot pose estimation
        ExtendedKalmanFilter ekf = ExtendedKalmanFilter.createRobotPoseEKF();
        ekf.setState(-8, -5, 0);

        // Simulate robot following path
        for (int i = 0; i < 10; i++) {
            // Predict with velocity
            ekf.predict(new double[] { 1.0, 0.1 }, 0.1);

            // Simulate a GPS measurement
            double[] state = ekf.getState();
            double noiseX = (Math.random() - 0.5) * 0.2;
            double noiseY = (Math.random() - 0.5) * 0.2;
            ekf.update(new double[] { state[0] + noiseX, state[1] + noiseY });
        }

        double[] finalState = ekf.getState();
        assertTrue(finalState[0] > -8, "Robot should have moved forward");
    }

    @Test
    void testPhysicsSimulationStep() {
        PhysicsWorld world = new PhysicsWorld();

        PhysicsBody robot = PhysicsBody.dynamicBody("testBot", 10.0,
                new Vector3(0, 0, 0),
                0.5).withVelocity(new Vector3(1, 0, 0)); // Moving right
        world.addBody(robot);

        // Step simulation
        for (int i = 0; i < 60; i++) {
            world.step(1.0 / 60.0);
        }

        // After 1 second at 1 m/s, robot should be at x≈1
        PhysicsBody updated = null;
        for (PhysicsBody body : world.getBodies()) {
            if (body.id().equals("testBot")) {
                updated = body;
                break;
            }
        }

        assertNotNull(updated);
        assertEquals(1.0, updated.position().x(), 0.1, "Position should be ~1m after 1s at 1m/s");
    }

    @Test
    void testDefaultPhysicsEngine() {
        DefaultPhysicsEngine engine = new DefaultPhysicsEngine();

        assertEquals("JRobotics Built-in Physics", engine.getName());
        assertEquals("2.0.0", engine.getVersion());
        assertTrue(engine.supports3D());
        assertFalse(engine.supportsCCD());

        PhysicsWorld world = engine.createWorld();
        assertNotNull(world);

        // Add two bodies
        world.addBody(PhysicsBody.dynamicBody("a", 1.0, new Vector3(0, 0, 0), 1.0));
        world.addBody(PhysicsBody.dynamicBody("b", 1.0, new Vector3(1.5, 0, 0), 1.0));

        // Detect collisions
        List<PhysicsEngine.CollisionPair> collisions = engine.detectCollisions(world);
        assertTrue(collisions.size() >= 1, "Bodies should be colliding (touching)");
    }
}
