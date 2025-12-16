/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.demo;

import org.jrobotics.simulation.*;
import org.jrobotics.simulation.environment.EnvironmentBuilder;
import org.jrobotics.visualization.JMonkey3DVisualizer;

/**
 * 3D Visual demo using JMonkeyEngine.
 * 
 * <p>
 * Run this to see the 3D visualization with lighting and camera control.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Visual3DDemo {

    public static void main(String[] args) {
        System.out.println("=== JRobotics 3D Visualization Demo ===\n");
        System.out.println("Controls:");
        System.out.println("  WASD - Move camera");
        System.out.println("  Mouse - Look around");
        System.out.println("  Q/Z - Up/Down");
        System.out.println();

        // Create environment
        PhysicsWorld world = EnvironmentBuilder.emptyArena(30, 20)
                .addObstacle("obs-1", -8, 5, 1.5)
                .addObstacle("obs-2", 8, -3, 2.0)
                .addObstacle("obs-3", 0, 7, 1.0)
                .addObstacle("obs-4", -4, -6, 1.8)
                .addObstacle("obs-5", 6, 4, 1.2)
                .addRobot("robot", -12, 0, 5.0, 0.8)
                .build();

        // Create 3D visualizer
        // Create 3D visualizer (settings handled internally for now)
        JMonkey3DVisualizer visualizer = new JMonkey3DVisualizer();

        // Create simulation runner
        SimulationRunner runner = new SimulationRunner(world, 1.0 / 60.0, 60);
        runner.setVisualizer(visualizer);

        // Get robot and create movement
        final PhysicsBody[] robotRef = new PhysicsBody[1];
        for (PhysicsBody body : world.getBodies()) {
            if (body.id().equals("robot")) {
                robotRef[0] = body;
                break;
            }
        }

        // Circular + forward motion
        final double[] time = { 0 };
        runner.setStepCallback(() -> {
            time[0] += 1.0 / 60.0;

            if (robotRef[0] != null) {
                double vx = 2.0 + Math.sin(time[0] * 0.3) * 1.5;
                double vy = Math.cos(time[0] * 0.5) * 3.0;

                PhysicsBody robot = robotRef[0];
                PhysicsBody updated = robot.withVelocity(new Vector3(vx, vy, 0));

                world.getBodies().remove(robot);
                world.addBody(updated);
                robotRef[0] = updated;
            }
        });

        System.out.println("Starting 3D simulation...");

        runner.start();

        while (runner.isRunning() && visualizer.isActive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        runner.stop();
        System.out.println("\nSimulation ended after " + world.getStepCount() + " steps.");
    }
}
