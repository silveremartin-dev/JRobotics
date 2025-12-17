/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.simulation.environment;

import org.jrobotics.simulation.PhysicsBody;
import org.jrobotics.simulation.PhysicsWorld;
import org.jrobotics.core.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Environment builder for creating simulation scenarios.
 * 
 * <p>
 * Provides factory methods for common environment elements.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class EnvironmentBuilder {

    private final List<PhysicsBody> bodies = new ArrayList<>();
    private Vector3 gravity = new Vector3(0, 0, -9.81);
    private double friction = 0.1;

    /**
     * Sets gravity.
     */
    public EnvironmentBuilder withGravity(double x, double y, double z) {
        this.gravity = new Vector3(x, y, z);
        return this;
    }

    /**
     * Sets 2D mode (no Z gravity).
     */
    public EnvironmentBuilder with2DMode() {
        this.gravity = Vector3.ZERO;
        return this;
    }

    /**
     * Sets friction coefficient.
     */
    public EnvironmentBuilder withFriction(double friction) {
        this.friction = friction;
        return this;
    }

    /**
     * Adds a wall (static obstacle).
     */
    public EnvironmentBuilder addWall(String id, double x, double y, double length, boolean vertical) {
        bodies.add(PhysicsBody.staticBody(id, new Vector3(x, y, 0), length / 2));
        return this;
    }

    /**
     * Adds a circular obstacle.
     */
    public EnvironmentBuilder addObstacle(String id, double x, double y, double radius) {
        bodies.add(PhysicsBody.staticBody(id, new Vector3(x, y, 0), radius));
        return this;
    }

    /**
     * Adds a rectangular arena.
     */
    public EnvironmentBuilder addArena(double width, double height) {
        double hw = width / 2;
        double hh = height / 2;
        addWall("wall-north", 0, hh, width, false);
        addWall("wall-south", 0, -hh, width, false);
        addWall("wall-east", hw, 0, height, true);
        addWall("wall-west", -hw, 0, height, true);
        return this;
    }

    /**
     * Adds random obstacles.
     */
    public EnvironmentBuilder addRandomObstacles(int count, double areaWidth, double areaHeight, double minRadius,
            double maxRadius) {
        for (int i = 0; i < count; i++) {
            double x = (Math.random() - 0.5) * areaWidth * 0.8;
            double y = (Math.random() - 0.5) * areaHeight * 0.8;
            double radius = minRadius + Math.random() * (maxRadius - minRadius);
            addObstacle("obstacle-" + i, x, y, radius);
        }
        return this;
    }

    /**
     * Adds a robot (dynamic body).
     */
    public EnvironmentBuilder addRobot(String id, double x, double y, double mass, double radius) {
        bodies.add(PhysicsBody.dynamicBody(id, mass, new Vector3(x, y, 0), radius));
        return this;
    }

    /**
     * Builds the physics world.
     */
    public PhysicsWorld build() {
        PhysicsWorld world = new PhysicsWorld();
        world.setGravity(gravity);
        world.setFriction(friction);

        for (PhysicsBody body : bodies) {
            world.addBody(body);
        }

        return world;
    }

    /**
     * Creates a simple maze environment.
     */
    public static EnvironmentBuilder maze() {
        return new EnvironmentBuilder()
                .with2DMode()
                .withFriction(0.2)
                .addArena(20, 20)
                .addWall("inner-1", -5, 0, 8, true)
                .addWall("inner-2", 5, 0, 8, true)
                .addWall("inner-3", 0, -3, 6, false);
    }

    /**
     * Creates an empty arena.
     */
    public static EnvironmentBuilder emptyArena(double width, double height) {
        return new EnvironmentBuilder()
                .with2DMode()
                .withFriction(0.1)
                .addArena(width, height);
    }

    /**
     * Creates an obstacle course.
     */
    public static EnvironmentBuilder obstacleCourse() {
        return new EnvironmentBuilder()
                .with2DMode()
                .withFriction(0.15)
                .addArena(30, 20)
                .addRandomObstacles(10, 30, 20, 0.5, 1.5);
    }
}
