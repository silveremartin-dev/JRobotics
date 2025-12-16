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
import org.jrobotics.simulation.visualization.Swing2DVisualizer;

/**
 * Visual demo showing a robot navigating in a simulated environment.
 * 
 * <p>Run this to see the graphical 2D visualization.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class VisualSimulationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== JRobotics Visual Simulation Demo ===\n");
        
        // Create environment with obstacles
        PhysicsWorld world = EnvironmentBuilder.emptyArena(20, 15)
            .addObstacle("obs-1", -5, 3, 1.0)
            .addObstacle("obs-2", 5, -2, 1.5)
            .addObstacle("obs-3", 0, 5, 0.8)
            .addObstacle("obs-4", -3, -4, 1.2)
            .addRobot("robot", -8, 0, 5.0, 0.5)
            .build();
        
        // Create visualizer
        Swing2DVisualizer visualizer = new Swing2DVisualizer(
            "JRobotics - Robot Simulation", 1024, 768);
        visualizer.setScale(35.0);
        visualizer.setShowTrails(true);
        
        // Create simulation runner
        SimulationRunner runner = new SimulationRunner(world, 1.0/60.0, 60);
        runner.setVisualizer(visualizer);
        
        // Get robot body and add simple movement
        final PhysicsBody[] robotRef = new PhysicsBody[1];
        for (PhysicsBody body : world.getBodies()) {
            if (body.id().equals("robot")) {
                robotRef[0] = body;
                break;
            }
        }
        
        // Add a step callback to move the robot in a pattern
        final double[] time = {0};
        runner.setStepCallback(() -> {
            time[0] += 1.0/60.0;
            
            if (robotRef[0] != null) {
                // Circular motion
                double vx = Math.cos(time[0] * 0.5) * 2.0;
                double vy = Math.sin(time[0] * 0.5) * 2.0;
                
                // Update robot velocity
                PhysicsBody robot = robotRef[0];
                PhysicsBody updated = robot.withVelocity(new Vector3(vx, vy, 0));
                
                // Find and update in world
                world.getBodies().remove(robot);
                world.addBody(updated);
                robotRef[0] = updated;
            }
        });
        
        System.out.println("Starting visual simulation...");
        System.out.println("Close the window to exit.\n");
        
        // Run until window is closed
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
