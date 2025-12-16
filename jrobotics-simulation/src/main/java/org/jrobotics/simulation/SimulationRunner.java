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

import org.jrobotics.simulation.visualization.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulation runner with timing and visualization.
 * 
 * <p>Manages the simulation loop at a fixed timestep.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulationRunner.class);
    
    private final PhysicsWorld world;
    private final double timestep;
    private final long stepIntervalMs;
    private Visualizer visualizer;
    private ScheduledExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Runnable stepCallback;
    
    /**
     * Creates a simulation runner.
     * 
     * @param world the physics world
     * @param timestep the physics timestep in seconds
     * @param stepsPerSecond the simulation rate
     */
    public SimulationRunner(PhysicsWorld world, double timestep, int stepsPerSecond) {
        this.world = world;
        this.timestep = timestep;
        this.stepIntervalMs = 1000 / stepsPerSecond;
    }
    
    /**
     * Creates a runner with default settings (60 Hz).
     */
    public SimulationRunner(PhysicsWorld world) {
        this(world, 1.0 / 60.0, 60);
    }
    
    /**
     * Sets the visualizer.
     */
    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }
    
    /**
     * Sets a callback to run after each step.
     */
    public void setStepCallback(Runnable callback) {
        this.stepCallback = callback;
    }
    
    /**
     * Starts the simulation.
     */
    public void start() {
        if (running.get()) return;
        
        running.set(true);
        
        if (visualizer != null) {
            visualizer.initialize();
        }
        
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SimulationRunner");
            t.setDaemon(true);
            return t;
        });
        
        executor.scheduleAtFixedRate(this::step, 0, stepIntervalMs, TimeUnit.MILLISECONDS);
        
        logger.info("Simulation started at {} Hz", 1000.0 / stepIntervalMs);
    }
    
    /**
     * Executes one simulation step.
     */
    private void step() {
        try {
            world.step(timestep);
            
            if (stepCallback != null) {
                stepCallback.run();
            }
            
            if (visualizer != null && visualizer.isActive()) {
                visualizer.update(world);
                visualizer.render();
            }
        } catch (Exception e) {
            logger.error("Simulation step error", e);
        }
    }
    
    /**
     * Stops the simulation.
     */
    public void stop() {
        running.set(false);
        
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (visualizer != null) {
            visualizer.shutdown();
        }
        
        logger.info("Simulation stopped after {} steps", world.getStepCount());
    }
    
    /**
     * Runs the simulation for a specified duration.
     * 
     * @param durationSeconds the duration
     */
    public void runFor(double durationSeconds) {
        start();
        try {
            Thread.sleep((long) (durationSeconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stop();
    }
    
    /**
     * Runs a specified number of steps synchronously.
     * 
     * @param steps the number of steps
     */
    public void runSteps(int steps) {
        if (visualizer != null) {
            visualizer.initialize();
        }
        
        for (int i = 0; i < steps; i++) {
            step();
        }
        
        if (visualizer != null) {
            visualizer.shutdown();
        }
    }
    
    /**
     * Checks if running.
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Gets the world.
     */
    public PhysicsWorld getWorld() {
        return world;
    }
}
